#!/usr/bin/env bash
# Prepara o ambiente local para rodar backend e frontend direto no host (sem docker-compose).
# Ver docs/decisoes/2026-09-24-identidade-desacoplada.md. Idempotente: pode rodar de novo.
#
#   1. cria .env a partir de .env.example, se não existir
#   2. gera o par de chaves JWT uma única vez (tokens sobrevivem a reinícios do Quarkus)
#   3. liga backend/.env ao .env da raiz (o Quarkus lê o .env do diretório onde roda)
#   4. confere Docker, JDK 21 e Node 22
set -euo pipefail

raiz="$(cd "$(dirname "$0")/.." && pwd)"
cd "$raiz"

if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "criado .env a partir de .env.example"
fi

valor_de() { grep -E "^$1=" .env | head -1 | cut -d= -f2- || true; }

if [[ -z "$(valor_de JWT_PRIVATE_KEY)" || -z "$(valor_de JWT_PUBLIC_KEY)" ]]; then
  tmp="$(mktemp -d)"
  trap 'rm -rf "$tmp"' EXIT
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$tmp/private.pem" 2>/dev/null
  openssl rsa -pubout -in "$tmp/private.pem" -out "$tmp/public.pem" 2>/dev/null
  privada="$(awk '/BEGIN/{next} /END/{next} {printf "%s", $0}' "$tmp/private.pem")"
  publica="$(awk '/BEGIN/{next} /END/{next} {printf "%s", $0}' "$tmp/public.pem")"
  sed -i.bak -e "s|^JWT_PRIVATE_KEY=.*|JWT_PRIVATE_KEY=$privada|" -e "s|^JWT_PUBLIC_KEY=.*|JWT_PUBLIC_KEY=$publica|" .env
  rm -f .env.bak
  echo "gerado par de chaves JWT local no .env"
fi

if [[ ! -e backend/.env ]]; then
  ln -s ../.env backend/.env
  echo "criado link backend/.env -> ../.env"
fi

if ! docker info >/dev/null 2>&1; then
  echo "AVISO: Docker não está acessível. O Quarkus Dev Services precisa dele para subir o Postgres."
fi
# O mvnw usa o JAVA_HOME quando definido; só cai no java do PATH sem ele.
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if ! "$java_bin" -version 2>&1 | grep -qE 'version "21'; then
  echo "AVISO: o Java usado pelo mvnw não é o 21 (o CI usa 21). Ex.: export JAVA_HOME=/caminho/do/jdk-21"
fi
if ! node -v 2>/dev/null | grep -qE '^v(2[4-9]|[3-9][0-9])\.'; then
  echo "AVISO: o Angular 22 precisa do Node 24 (o mesmo do CI). Com nvm: cd frontend && nvm install && nvm use"
fi

cat <<'FIM'

Pronto. Para desenvolver:
  cd backend  && ./mvnw quarkus:dev     # Postgres sobe sozinho; API em :8080, debug em :5005
  cd frontend && npm ci && npm start    # SPA em :4200
FIM
exit 0
