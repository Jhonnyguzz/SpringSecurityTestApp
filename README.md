# SongBox – Spring Security Test App

Small Spring Boot app to experiment with Spring Security: form login, registration,
OAuth2 login (Google and GitHub) unified into a single user, and adding songs.

## Requirements

- JDK 25 (the Gradle toolchain requires it).
- No Gradle installation needed: use the included wrapper (`./gradlew`, or `gradlew.bat` on Windows).

## Build

```bash
./gradlew build          # compile, run tests and package
./gradlew test           # run tests only
./gradlew build -x test  # build without tests
```

The executable jar is generated at `build/libs/SpringSecurityTestApp-0.0.1-SNAPSHOT.jar`.

## Run

```bash
./gradlew bootRun
# or
java -jar build/libs/SpringSecurityTestApp-0.0.1-SNAPSHOT.jar
```

Open http://localhost:8080. To use another port:

```bash
./gradlew bootRun --args='--server.port=8081'
```

The app uses an in-memory H2 database recreated on every start and seeded from
`src/main/resources/data.sql` with sample songs and this user:

| Email | Password | Role |
|-------|----------|------|
| `admin@example.com` | `admin` | `ADMIN` |

New users can be created at `/register`.

### OAuth2 login (optional)

Form login works without extra configuration. To enable Google and GitHub, create
OAuth apps and export their credentials before starting the app:

```bash
export GOOGLE_CLIENT_ID=...
export GOOGLE_CLIENT_SECRET=...
export GITHUB_CLIENT_ID=...
export GITHUB_CLIENT_SECRET=...
./gradlew bootRun
```

Redirect URIs to register in each provider:

- Google: `http://localhost:8080/login/oauth2/code/google`
- GitHub: `http://localhost:8080/login/oauth2/code/github`

See [docs/account-linking.md](docs/account-linking.md) for how accounts from different
login methods are linked.

# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.2/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.2/gradle-plugin/packaging-oci-image.html)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/4.1.2/specification/configuration-metadata/annotation-processor.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.2/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Rest Repositories](https://docs.spring.io/spring-boot/4.1.2/how-to/data-access.html#howto.data-access.exposing-spring-data-repositories-as-rest)
* [OAuth2 Client](https://docs.spring.io/spring-boot/4.1.2/reference/web/spring-security.html#web.security.oauth2.client)
* [Spring Security](https://docs.spring.io/spring-boot/4.1.2/reference/web/spring-security.html)
* [Thymeleaf](https://docs.spring.io/spring-boot/4.1.2/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
* [Spring Web MVC](https://docs.spring.io/spring-boot/4.1.2/reference/web/servlet.html)

### Guides

The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Accessing JPA Data with REST](https://spring.io/guides/gs/accessing-data-rest/)
* [Accessing Neo4j Data with REST](https://spring.io/guides/gs/accessing-neo4j-data-rest/)
* [Accessing MongoDB Data with REST](https://spring.io/guides/gs/accessing-mongodb-data-rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)

### Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

