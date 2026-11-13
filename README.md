# Conecta Alimento API

API RESTful em **Java 21 + Spring Boot** com banco **Oracle (FIAP)** que conecta estabelecimentos com
excedentes alimentares (padarias, mercados, restaurantes, hortifrútis) a instituições sociais
(ONGs, cozinhas comunitárias, abrigos), com piloto nos bairros **Bom Retiro, Pari e Brás**, em São Paulo.

Global Solution 2026.2 · Domain Driven Design - Java · FIAP

| Integrante | RM |
|---|---|
| Guilherme Barone Milani | RM562114 |
| Guilherme Frangione Ferreira Acácio | RM562475 |
| André Dominicis Monteiro | RM562397 |
| Luigi Escudero Grigoletto | RM562505 |
| Luis Gustavo Mattos da Silva | RM561672 |

## Links

- **Repositório GitHub:** https://github.com/Guilherme12376/GS-Java
- **Deploy:** não publicado; a API roda localmente (veja abaixo). O projeto está pronto para o Render (`Dockerfile` e `render.yaml`).
- **Documentação interativa (Swagger):** `http://localhost:8080/swagger-ui.html` com a API em execução

> No plano gratuito do Render o serviço "dorme" após 15 minutos sem acesso; a primeira requisição pode
> levar cerca de 1 minuto para acordá-lo.

## Tecnologias e versões

| Ferramenta | Versão |
|---|---|
| Java (JDK) | 21 |
| Spring Boot | 3.5.16 (Web, JDBC, Validation) |
| Banco de dados | Oracle Database 19c (servidor da FIAP `oracle.fiap.com.br:1521:ORCL`) |
| Driver | Oracle JDBC `ojdbc11` |
| Documentação | springdoc-openapi (Swagger UI) 2.8.17 |
| Build | Maven 3.9 |
| IDE | IntelliJ IDEA |
| Deploy | Render.com (Docker) |

## Arquitetura (camadas)

```
br.com.fiap.conectaalimento
├── model        entidades do domínio (Entidade, Usuario, EstabelecimentoDoador, InstituicaoReceptora,
│                Alimento, AlimentoPerecivel, AlimentoNaoPerecivel, Solicitacao, Retirada, Endereco) e enums
├── dao          acesso ao Oracle com JDBC (BaseDAO genérico + um DAO por tabela)
├── bo           regras de negócio (validações, fluxo de status, cálculo de distância e indicadores)
├── controller   endpoints REST (GET, POST, PUT, DELETE)
├── dto          corpos de requisição/resposta com Bean Validation
├── exception    exceções da aplicação + GlobalExceptionHandler (respostas de erro padronizadas)
└── config       CORS, Swagger e inicialização do banco
```

Fluxo de uma requisição: **Controller → BO → DAO → Oracle**. Erros em qualquer camada viram uma resposta
JSON padronizada (`timestamp`, `status`, `erro`, `mensagem`, `caminho`, `detalhes`).

## Banco de dados

Tabelas (prefixo `T_CA_`): `T_CA_ESTABELECIMENTO`, `T_CA_INSTITUICAO`, `T_CA_ALIMENTO`, `T_CA_SOLICITACAO`
e `T_CA_RETIRADA`.

- Na primeira execução a API **cria as tabelas automaticamente** (script `src/main/resources/db/schema-oracle.sql`)
  e, com o banco vazio, carrega **dados de demonstração** fictícios.
- Para recriar tudo manualmente no SQL Developer, use `database/script-oracle.sql`.

## Como executar no IntelliJ (Oracle da FIAP)

1. Instale o **JDK 21** e o **IntelliJ IDEA**.
2. Clone o repositório: `git clone https://github.com/<usuario>/conecta-alimento-api.git`
3. No IntelliJ: **File > Open** e selecione a pasta do projeto (o `pom.xml` é reconhecido e o Maven baixa as dependências).
4. Em **File > Project Structure > Project**, selecione o **SDK 21**.
5. Abra `ConectaAlimentoApplication` e clique no ▶ ao lado do `main` para criar a configuração de execução.
6. Em **Run > Edit Configurations > Environment variables**, informe as credenciais do Oracle:
   ```
   DB_USER=rmXXXXX;DB_PASSWORD=sua_senha
   ```
   (opcional: `DB_URL`, por padrão `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`)
7. Execute. A API sobe em `http://localhost:8080`:
   - `http://localhost:8080/swagger-ui.html` → testar todos os endpoints;
   - `http://localhost:8080/api/status` → confirma a conexão com o banco (`"banco": "UP"`).
8. Opcional: o arquivo `requisicoes.http` tem exemplos de todas as chamadas para o HTTP Client do IntelliJ.

**Sem acesso ao Oracle?** Rode com o perfil `h2` (banco em memória, os dados somem ao reiniciar):
em **Edit Configurations > Active profiles** informe `h2`, ou pela linha de comando:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### Pela linha de comando

```bash
mvn clean package
java -jar target/conecta-alimento-api.jar
```

(as variáveis `DB_USER` e `DB_PASSWORD` precisam estar definidas no terminal)

## Deploy no Render (passo a passo)

1. Suba este projeto para um repositório no GitHub (a raiz do repositório deve conter o `pom.xml` e o `Dockerfile`).
2. Em [render.com](https://render.com), **New > Web Service** e conecte o repositório.
3. **Language/Runtime: Docker** (o Render usa o `Dockerfile` do projeto; build com Maven + JDK 21 e execução com JRE 21).
4. **Instance type: Free**.
5. Em **Environment Variables** adicione:
   - `DB_URL` = `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`
   - `DB_USER` = seu usuário do Oracle da FIAP
   - `DB_PASSWORD` = sua senha
6. **Advanced > Health Check Path:** `/`
7. Clique em **Create Web Service** e aguarde o build. A URL pública fica no topo da página do serviço.
8. Teste: `<url>/api/status` e `<url>/swagger-ui.html`.

> Alternativa: **New > Blueprint** usando o arquivo `render.yaml` (pede `DB_USER` e `DB_PASSWORD` na criação).

## Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/` | Informações da API |
| GET | `/api/status` | Status da API e do banco |
| POST | `/api/login` | Autentica estabelecimento ou instituição |
| GET | `/api/indicadores` | Indicadores de impacto (doações, kg, % retiradas, tempo médio) |
| GET | `/api/estabelecimentos` | Lista estabelecimentos |
| GET | `/api/estabelecimentos/{id}` | Busca estabelecimento |
| GET | `/api/estabelecimentos/{id}/alimentos` | Alimentos publicados pelo estabelecimento |
| POST | `/api/estabelecimentos` | Cadastra estabelecimento |
| PUT | `/api/estabelecimentos/{id}` | Atualiza estabelecimento |
| DELETE | `/api/estabelecimentos/{id}` | Remove (somente sem alimentos cadastrados) |
| GET | `/api/instituicoes` | Lista instituições |
| GET | `/api/instituicoes/{id}` | Busca instituição |
| GET | `/api/instituicoes/{id}/solicitacoes` | Solicitações da instituição |
| GET | `/api/instituicoes/{id}/alimentos-proximos` | Alimentos disponíveis ordenados por distância |
| POST | `/api/instituicoes` | Cadastra instituição |
| PUT | `/api/instituicoes/{id}` | Atualiza instituição |
| DELETE | `/api/instituicoes/{id}` | Remove (somente sem solicitações) |
| GET | `/api/alimentos?status=` | Lista alimentos (filtro opcional por status) |
| GET | `/api/alimentos/disponiveis?categoria=&bairro=&prazoDias=&instituicaoId=` | Busca com filtros e distância |
| GET | `/api/alimentos/orientacoes` | Orientações de segurança alimentar |
| GET | `/api/alimentos/{id}` | Busca alimento |
| POST | `/api/alimentos` | Publica alimento (perecível ou não perecível) |
| PUT | `/api/alimentos/{id}` | Edita alimento (enquanto disponível/solicitado) |
| DELETE | `/api/alimentos/{id}` | Remoção lógica (status REMOVIDO) |
| GET | `/api/solicitacoes?status=&instituicaoId=&estabelecimentoId=` | Lista solicitações |
| GET | `/api/solicitacoes/{id}` | Busca solicitação |
| POST | `/api/solicitacoes` | Instituição solicita um alimento |
| PUT | `/api/solicitacoes/{id}` | Altera a quantidade (pendente) |
| PUT | `/api/solicitacoes/{id}/aceitar` | Estabelecimento aceita |
| PUT | `/api/solicitacoes/{id}/recusar` | Estabelecimento recusa (motivo opcional) |
| DELETE | `/api/solicitacoes/{id}` | Instituição cancela |
| GET | `/api/retiradas` | Lista retiradas |
| GET | `/api/retiradas/{id}` | Busca retirada |
| POST | `/api/retiradas` | Agenda a retirada |
| PUT | `/api/retiradas/{id}` | Reagenda |
| PUT | `/api/retiradas/{id}/confirmar-entrega` | Estabelecimento confirma a entrega |
| PUT | `/api/retiradas/{id}/confirmar-recebimento` | Instituição confirma o recebimento |
| DELETE | `/api/retiradas/{id}` | Cancela o agendamento |

### Códigos de resposta

| Código | Quando |
|---|---|
| 200 / 201 / 204 | Sucesso (consulta/alteração, criação com `Location`, remoção) |
| 400 | JSON inválido, campo obrigatório ausente, formato incorreto |
| 401 | Login com e-mail ou senha incorretos |
| 404 | Registro ou endpoint inexistente |
| 405 | Método HTTP não suportado |
| 409 | Conflito (e-mail/CNPJ já cadastrado, solicitação duplicada, remoção com histórico) |
| 422 | Regra de negócio violada (segurança alimentar, quantidade, status, janela de retirada) |
| 500 / 503 | Falha interna ou banco indisponível |

## Fluxo principal

1. O estabelecimento publica um alimento (`POST /api/alimentos`) → `DISPONIVEL`.
2. A instituição busca (`GET /api/alimentos/disponiveis`) e solicita (`POST /api/solicitacoes`) → alimento `SOLICITADO`.
3. O estabelecimento aceita (`PUT /api/solicitacoes/{id}/aceitar`) → alimento `RESERVADO`; outras solicitações pendentes ficam `NAO_ATENDIDA`.
4. A retirada é agendada (`POST /api/retiradas`) → `RETIRADA_AGENDADA`.
5. Entrega e recebimento confirmados (`PUT .../confirmar-entrega` e `.../confirmar-recebimento`) → `CONCLUIDO`.
6. Os indicadores (`GET /api/indicadores`) são atualizados.

## Dados de demonstração

Criados automaticamente com o banco vazio (todos fictícios, senha `123456`):

| Perfil | E-mail |
|---|---|
| Estabelecimento (padaria, Pari) | `padaria@demo.com` |
| Estabelecimento (mercado, Brás) | `mercado@demo.com` |
| Estabelecimento (hortifrúti, Bom Retiro) | `hortifruti@demo.com` |
| Instituição (cozinha comunitária, Bom Retiro) | `cozinha@demo.com` |
| Instituição (ONG, Brás) | `ong@demo.com` |

Para desligar: variável `DB_DADOS_EXEMPLO=false`.
