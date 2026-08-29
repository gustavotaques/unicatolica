# Arquitetura de camadas e organização de pastas do projeto

Este documento explica o padrão de camadas usado no backend (Resource → Service → Repository) e a forma de organizar pastas por módulo (Package by Feature), comparando com o MVC tradicional (Controller/Service/Repository/Domain agrupados por tipo).

## 1. MVC clássico

```
Controller  →  Model  →  (persistência embutida no Model / ou Service opcional)
                ↓
              View
```

- **Controller**: recebe a requisição HTTP, decide o que fazer, devolve resposta.
- **Model**: representa o dado e, na maioria das implementações reais, também carrega regra de negócio e acesso a dados juntos.
- **View**: renderiza a resposta (HTML, template). Em APIs REST puras a View quase desaparece (vira serialização JSON).

O ponto fraco do MVC "de livro" é não separar bem **regra de negócio** de **acesso a dados** — o Model normalmente acumula os dois papéis.

## 2. Resource → Service → Repository (usado neste projeto)

```
Resource  →  Service  →  Repository
(web)        (aplicacao)   (dominio)
```

| Camada | Equivalente em MVC | Responsabilidade |
|---|---|---|
| **Resource** (`web/`) | Controller | Recebe HTTP, valida entrada, chama o Service, devolve resposta. Sem regra de negócio. |
| **Service** (`aplicacao/`) | Não existe isolado no MVC clássico — geralmente fica misturado no Model/Controller | Orquestra a regra de negócio: valida invariantes, decide fluxo, chama um ou mais Repositories, lança exceções de domínio. |
| **Repository** (`dominio/`) | Parte do Model (a fatia de acesso a dado) | Só sabe ler/gravar entidade no banco (JPA/Panache). Sem regra de negócio. |

Diferença essencial: no MVC o **Model** é uma coisa só (dado + regra + persistência misturados). Aqui isso foi partido em duas camadas explícitas — Service (regra) e Repository (persistência) — e não existe "View" porque é uma API REST (o "view" vira só o DTO serializado em JSON, ex.: `LoginResponse`).

Exemplo real no projeto (módulo `identidade`):
```
identidade/
  web/         AuthResource.java       -> recebe POST /auth/login
  aplicacao/   AuthService.java        -> valida credenciais, gera JWT
  dominio/     Usuario.java, UsuarioRepository.java -> entidade + acesso a dado
```

Cada camada só conhece a camada logo abaixo (Resource não fala com Repository direto, sempre passa pelo Service). Isso facilita testar a regra de negócio isolada (mock do Repository) e é o que possibilita a regra AD-3 do projeto (nenhum módulo acessa o Repository de outro módulo — só o dono do módulo pode).

Não é "hexagonal"/"clean architecture" completo (não tem portas/interfaces de domínio separadas de infraestrutura formalmente) — é uma versão mais simples, de 3 camadas, que dá a separação básica que o MVC clássico não força.

## 3. Package by Layer vs. Package by Feature

Essa é uma dimensão diferente: não é "quais camadas existem", é "como agrupar as pastas".

### Package by Layer (agrupar por tipo técnico — o MVC "tradicional" com controller/service/repository/domain globais)

```
controller/
  UsuarioController.java
  PerfilController.java
  ComunidadeController.java
service/
  UsuarioService.java
  PerfilService.java
  ComunidadeService.java
repository/
  UsuarioRepository.java
  PerfilRepository.java
domain/
  Usuario.java
  Perfil.java
```

Tudo agrupado **por tipo técnico** primeiro. Um recurso (ex.: "Perfil") fica espalhado em 4 pastas diferentes.

### Package by Feature / Vertical Slice (usado neste projeto)

```
identidade/
  web/         (era "controller")
  aplicacao/   (era "service")
  dominio/     (era "repository" + "domain")
perfil/
  web/ aplicacao/ dominio/
comunidades/
  web/ aplicacao/ dominio/
...
```

Agrupado **por módulo de negócio** primeiro, e dentro de cada módulo repete-se a mesma sub-estrutura de camadas. Reduz o número de arquivos por pasta: não se abre `controller/` e vê 12 controllers juntos — abre-se `identidade/web/` e vê só o que é de identidade.

## 4. Nome popular dessas técnicas

- **Package by Feature** (também chamado "Package by Module" ou "Package by Component"): é o termo mais usado na comunidade Java/Spring para a organização de pastas por módulo de negócio em vez de por tipo técnico. Referências clássicas: o post "The One True Layout" de Ted Neward, e o livro *Get Your Hands Dirty on Clean Architecture*.
- **Modular Monolith** (monólito modular): quando o Package by Feature é aplicado dentro de um único deploy, com regra de isolamento entre os módulos (neste projeto, a AD-3 proíbe um módulo acessar o Repository de outro), o conjunto todo ganha esse nome. É o termo usado em `docs/unicatolica-architecture-spine.md` (`paradigm: 'Modular Monolith'`).

## 5. Resumo em dois eixos

- **Camadas internas** (Resource → Service → Repository): arquitetura em camadas (layered), parecida com o MVC tradicional mas sem View.
- **Organização de pastas** (`identidade/`, `perfil/`, `comunidades/`, ...): **Package by Feature**, dentro de um **Modular Monolith**.
