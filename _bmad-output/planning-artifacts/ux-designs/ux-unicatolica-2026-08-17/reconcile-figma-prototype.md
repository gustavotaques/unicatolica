# Reconcile — Protótipo Figma (import)

**Fonte:** Figma `UniCatolica` (fileKey `gFPD2WHOVO5uP4sCVccryT`), inspecionado via MCP em 2026-08-17. Screenshot salvo em `imports/figma-prototype/login-screen.png` (node 20:303, tela de Login).

## O que foi adotado

- **Paleta institucional como referência de partida.** O bordô (~`#7A1F2B`) e o laranja (~`#EA6A2E`–`#EB6A2E`) observados na tela de Login (fundo em gradiente vermelho/bordô, CTA laranja "ACESSAR PORTAL", logo "Católica de Santa Catarina") foram carregados para a direção escolhida (Campus Clean), mas com papel invertido: no protótipo o bordô é fundo dominante; na direção escolhida vira traço/ícone mínimo, e o laranja passa a ser o único acento de ação.
- **Forma geral de "portal institucional com card central"** (logo, título em caixa alta, formulário) inspirou a manutenção de um cabeçalho de marca simples nas novas telas — mas sem as formas orgânicas desfocadas de fundo (ver gap abaixo).

## O que foi deliberadamente divergido

- **Densidade de informação e registro "portal de notícias" (G1).** O protótipo original (telas `Home - Protótipo(2)`, com ticker de notícias, nav de categorias, múltiplos blocos empilhados) foi explicitamente rejeitado como base de densidade — decisão do usuário (memlog: tom "leve, atual... clean, baixa poluição visual e de informação"). Nenhuma das 3 direções renderizadas herdou o ticker ou a nav de categorias densa.
- **`tela-adm-relatorios` fica fora do escopo desta UX.** Existe no protótipo como uma das 4 telas administrativas, mas o usuário confirmou que o admin do MVP cobre só o que o PRD já especifica (pré-criar comunidades de curso, publicar avisos institucionais) — relatórios não é uma necessidade do PRD atual.

## Gaps — não inspecionados nesta conversa, não inventar

- **`TelaOficial - Modelo`** (seção top-level do Figma) nunca foi aberta nem tratada nesta conversa. Não há como saber se é um modelo de referência genérico, uma tela funcional, ou material de apoio interno da equipe de design. **Sinalizar para o usuário antes do Finalize fechar** — pode conter decisão relevante não capturada.
- **As formas orgânicas desfocadas no fundo do login** (blobs vermelhos/pretos semi-transparentes vistos no screenshot) não foram discutidas como motivo visual a manter ou descartar. A direção Campus Clean não as reutilizou (fundo liso `#FAFAF8`/`#FFFFFF`). Isso é consistente com "baixa poluição visual", mas nunca foi uma decisão explícita — é uma omissão por divergência de densidade, não uma rejeição deliberada e nomeada. Marcado como `[ASSUMPTION]` no DESIGN.md.
- **Telas administrativas restantes do Figma** (`tela-adm-login`, `tela-adm-dashboard`, `tela-adm-moderacao`) só tiveam os *nomes* inventariados via metadata — nenhuma foi aberta como screenshot. O reuso de `tela-adm-dashboard` como base para o admin no MVP (registrado no memlog) é uma intenção, não uma inspeção visual confirmada.

## 5 telas do PRD ausentes no Figma — confirmadas para desenho do zero

Busca, Mensagens, Notificações, Criar enquete, Denúncias/Solicitações de fixação (fila do moderador) — nenhuma existe como seção no Figma atual. `EXPERIENCE.md` define IA e padrões de componente para essas telas sem referência visual herdada, coerente com a decisão já registrada no memlog.

## Recomendação

Antes de marcar as spines como `status: final`, vale uma passada rápida do usuário por `TelaOficial - Modelo` no Figma — é o único node do protótipo com conteúdo totalmente desconhecido nesta conversa.
