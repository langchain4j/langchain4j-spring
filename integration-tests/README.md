# Integration tests

Each module here is a small Spring Boot application, assembled with the dependencies a user would add, and tested
end to end against a stubbed model provider (WireMock). They test what the tests of a single module cannot: how the
starters behave with a given set of dependencies on the classpath, in particular with and without optional
dependencies, which are always present in the tests of the module that declares them.

| Module | Application |
|---|---|
| `spring-boot3-mvc-without-webflux` | Spring MVC, Spring Boot 3, no `spring-webflux` |
| `spring-boot3-mvc-with-webflux` | Spring MVC, Spring Boot 3, `spring-boot-starter-webflux` |
| `spring-boot3-non-web-with-webflux` | no web layer, Spring Boot 3, `spring-webflux` |
| `spring-boot4-webmvc-without-webclient` | Spring MVC, Spring Boot 4, no `WebClient` |
| `spring-boot4-webmvc-with-webclient` | Spring MVC, Spring Boot 4, `spring-boot-starter-webclient` |
| `spring-boot4-non-web-with-webclient` | no web layer, Spring Boot 4, `spring-boot-starter-webclient` |

These modules are never published. When adding one, also add its `artifactId` to `excludeArtifacts` of the
`central-publishing-maven-plugin` in the root `pom.xml` (`IntegrationTestsAreNotPublishedTest` checks this).
