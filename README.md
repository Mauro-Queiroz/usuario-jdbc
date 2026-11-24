# Usuario JDBC — API REST com Java e Spring Boot

API REST desenvolvida em Java 17 e Spring Boot para gerenciamento de funcionários, contatos e módulos organizacionais. O projeto utiliza JDBC para acesso ao banco de dados MySQL, Flyway para versionamento do esquema e Docker para conteinerização da aplicação.

## Objetivo

Desenvolver uma aplicação Back-end que demonstre a implementação de operações CRUD, organização em camadas, persistência com JDBC, relacionamentos entre entidades, consultas SQL, migrações de banco de dados e disponibilização de endpoints REST.

## Tecnologias utilizadas

| Tecnologia               | Versão / utilização          |
| ------------------------ | ---------------------------- |
| Java                     | 17                           |
| Spring Boot              | 4.1.1                        |
| Maven                    | Gerenciamento e build        |
| Spring Web MVC           | API REST                     |
| Spring JDBC              | Acesso ao banco de dados     |
| MySQL                    | 8.4.11                       |
| Flyway                   | 12.4.0 — migrações do banco  |
| MySQL Connector/J        | Driver JDBC                  |
| Lombok                   | Redução de código repetitivo |
| Datafaker                | Geração de dados de teste    |
| Docker                   | Conteinerização              |
| Docker Compose           | Orquestração dos serviços    |
| JUnit / Spring Boot Test | Testes automatizados         |

## Arquitetura e organização

O projeto está organizado em camadas, separando as responsabilidades da aplicação.

```
src/
├── main/
│   ├── java/jdbc/usuario/
│   │   ├── Application.java
│   │   ├── Main.java
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── persistence/
│   │   │   ├── entity/
│   │   └── service/
│   │
│   └── resources/
│       ├── application.properties
│       └── db/migration/
│
└── test/
    └── java/jdbc/usuario/
        ├── ApplicationTests.java
        ├── controller/
        └── service/
```

### Responsabilidades das camadas

- **Controller:** disponibiliza os endpoints REST e recebe as requisições HTTP.
- **Service:** concentra a lógica de negócio e intermedia as operações entre controllers e DAOs.
- **Persistence / DAO:** executa operações SQL e gerencia o acesso aos dados por JDBC.
- **Entity:** representa as estruturas de dados utilizadas na persistência.
- **DTO:** define os objetos utilizados na comunicação da API.
- **Flyway:** controla a evolução do esquema do banco de dados.

## Funcionalidades

- CRUD de funcionários.
- Consulta de funcionários por identificador.
- Consulta paginada de funcionários.
- Cadastro e atualização de funcionários.
- Exclusão de funcionários.
- Consulta de contatos associados aos funcionários.
- Associação de funcionários a módulos.
- Consulta dos módulos e dos respectivos funcionários.
- Tratamento centralizado de exceções da API.
- Auditoria de operações sobre funcionários.
- Utilização de procedure, view e triggers no MySQL.

## Banco de dados e Flyway

O projeto utiliza o banco de dados MySQL `usuario_jdbc`.

As migrações SQL estão localizadas em:

```
src/main/resources/db/migration/
```

### Migrações implementadas

| Migração | Descrição                                           |
| -------- | --------------------------------------------------- |
| V001     | Criação da tabela employees                         |
| V002     | Criação da tabela modules                           |
| V003     | Inserção dos módulos                                |
| V004     | Criação da tabela accesses                          |
| V005     | Criação da tabela contacts                          |
| V006     | Criação da tabela employee_audit                    |
| V007     | Criação de trigger para exclusão de funcionários    |
| V008     | Alteração da tabela accesses                        |
| V009     | Criação de trigger para inserção de funcionários    |
| V010     | Criação de trigger para atualização de funcionários |
| V011     | Criação da view de auditoria                        |
| V012     | Criação da procedure de inserção de funcionários    |

O Flyway valida e aplica as migrações automaticamente durante a inicialização da aplicação.

## Endpoints REST

A API disponibiliza os seguintes endpoints:

| Método | Endpoint          | Descrição                               |
| ------ | ----------------- | --------------------------------------- |
| GET    | `/employees`      | Lista funcionários com paginação        |
| GET    | `/employees/{id}` | Consulta funcionário por ID             |
| POST   | `/employees`      | Cadastra funcionário                    |
| PUT    | `/employees/{id}` | Atualiza funcionário                    |
| DELETE | `/employees/{id}` | Exclui funcionário                      |
| GET    | `/modules`        | Lista módulos e funcionários associados |

### Paginação de funcionários

O endpoint `/employees` utiliza os parâmetros `page` e `size`.

Exemplo:

```
GET /employees?page=0&size=10
```

Parâmetros:

- `page`: número da página, iniciando em zero.
- `size`: quantidade de registros por página, limitada a 100.

A resposta contém os registros da página e informações de paginação, como total de elementos e total de páginas.

### Exemplos de consulta

Listar funcionários:

```
curl http://localhost:8080/employees
```

Consultar funcionário:

```
curl http://localhost:8080/employees/10016
```

Consultar módulos:

```
curl http://localhost:8080/modules
```

### Códigos HTTP utilizados

- `200 OK`: consulta ou atualização realizada.
- `201 Created`: funcionário cadastrado.
- `204 No Content`: funcionário excluído.
- `400 Bad Request`: requisição inválida.
- `404 Not Found`: registro não encontrado.

## Testes automatizados

Os testes estão organizados em:

```
src/test/java/jdbc/usuario/
```

Classes de teste:

- `ApplicationTests`
- `EmployeeControllerTest`
- `EmployeeServiceImplTest`

Para executar os testes:

```
mvn test
```

A suíte foi executada com sucesso, totalizando 18 testes aprovados, sem falhas ou erros.

## Docker e Docker Compose

O projeto possui um `Dockerfile` baseado na imagem:

```
FROM eclipse-temurin:17-jre
```

O Docker Compose configura dois serviços:

- **mysql:** banco de dados MySQL 8.4.11.
- **api:** aplicação Spring Boot na porta 8080.

A persistência do MySQL é realizada por meio de um volume Docker.

### Configuração do ambiente

Crie o arquivo `.env` a partir do exemplo:

```
cp .env.example .env
```

Edite o arquivo `.env` e configure suas próprias credenciais:

```
MYSQL_DATABASE=usuario_jdbc
MYSQL_USER=springuser
MYSQL_PASSWORD=ALTERE_PARA_SUA_SENHA
MYSQL_ROOT_PASSWORD=ALTERE_PARA_SUA_SENHA
```

Não publique o arquivo `.env` no GitHub.

### Compilar o projeto

```
mvn clean package -DskipTests
```

### Construir e iniciar os containers

```
docker compose up -d --build
```

### Verificar os containers

```
docker compose ps
```

### Consultar os logs da API

```
docker logs --tail 80 usuario-jdbc-api
```

A API estará disponível em:

```
http://localhost:8080
```

## Imagem Docker no Docker Hub

A imagem da aplicação está publicada no Docker Hub:

**Repositório:**

[https://hub.docker.com/r/maurolqz/usuario-jdbc](https://hub.docker.com/r/maurolqz/usuario-jdbc)

**Imagem e tag:**

```
maurolqz/usuario-jdbc:0.0.1
```

Para baixar a imagem:

```
docker pull maurolqz/usuario-jdbc:0.0.1
```

## Execução da API

Após configurar o ambiente e iniciar os serviços, os endpoints podem ser acessados por ferramentas como:

- cURL
- Postman
- Navegador
- DBeaver (para administração e consulta do banco de dados)

## Autor

Projeto desenvolvido como estudo prático de desenvolvimento Java Back-end, com foco em Spring Boot, JDBC, MySQL, Flyway, APIs REST e Docker.
