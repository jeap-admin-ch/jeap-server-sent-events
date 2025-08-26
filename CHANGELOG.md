# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.6.0] - 2025-08-26

### Changed

- Update parent from 5.12.1 to 5.13.0

## [1.5.0] - 2025-08-14

### Changed
- Update parent from 5.12.0 to 5.12.1
- update jeap-starter from 17.40.1 to 17.41.0
- update jeap-spring-boot-vault-starter from 17.40.1 to 17.41.0
- update jeap-spring-boot-roles-anywhere-starter from 1.1.1 to 1.2.0
  
- update jeap-messaging from 8.53.1 to 8.54.0
- update jeap-crypto from 3.25.1 to 3.26.0

## [1.4.2] - 2025-08-08
### Changed
- update jeap-messaging from 8.53.0 to 8.53.1
- update jeap-crypto from 3.25.0 to 3.25.1
- update jeap-spring-boot-vault-starter from 17.40.0 to 17.40.1
- Make feature-policy header configurable in jeap-spring-boot-web-config-starter


## [1.4.1] - 2025-08-08
### Changed
- update jeap-starter from 17.40.0 to 17.40.1
- Make feature-policy header configurable in jeap-spring-boot-web-config-starter


## [1.4.0] - 2025-08-05

### Changed
- Update parent from 5.11.0 to 5.12.0
- updated springdoc-openapi from 2.8.6 to 2.8.9
- updated wiremock from 3.12.1 to 3.13.1
- update jeap-starter from 17.39.3 to 17.40.0
- updated logstash from 8.0 to 8.1
- update jeap-spring-boot-roles-anywhere-starter from 1.0.0 to 1.1.1
- update commons-compress from 1.27.1 to 1.28.0
- update jeap-crypto from 3.24.3 to 3.25.0
- update jeap-spring-boot-vault-starter from 17.39.3 to 17.40.0
- update jeap-messaging from 8.52.0 to 8.53.0
  

## [1.3.2] - 2025-07-30
### Changed
- new version to build a release, because build failed with 1.3.1 

## [1.3.1] - 2025-07-29
### Changed
- set web.emitter.timeoutInMs to a reasonable default (10 minutes)

## [1.3.0] - 2025-07-24
### Changed
- update jeap-messaging from 8.51.3 to 8.52.0
- Added jeap-spring-boot-roles-anywhere-starter support for aws msk


## [1.2.5] - 2025-07-09
### Changed
- update jeap-messaging from 8.51.2 to 8.51.3
- update jeap-crypto from 3.24.2 to 3.24.3
- update jeap-spring-boot-vault-starter from 17.39.2 to 17.39.3
- switch from deprecated org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration to org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration


## [1.2.4] - 2025-07-09
### Changed
- update jeap-messaging from 8.51.1 to 8.51.2
- update jeap-crypto from 3.24.1 to 3.24.2
- update jeap-spring-boot-vault-starter from 17.39.1 to 17.39.2
- ServletRequestSecurityTracer now properly handles non-REST requests (e.g., SOAP) by falling back to the request URI when the REST HandlerMapping pattern is not available.
- switch from deprecated org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration to org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration
- update jeap-starter from 17.39.2 to 17.39.3

## [1.2.3] - 2025-07-09
### Changed
- update jeap-starter from 17.39.1 to 17.39.2
- ServletRequestSecurityTracer now properly handles non-REST requests (e.g., SOAP) by falling back to the request URI when the REST HandlerMapping pattern is not available.


## [1.2.2] - 2025-07-07
### Changed
- update jeap-messaging from 8.51.0 to 8.51.1
- update jeap-crypto from 3.24.0 to 3.24.1
- update jeap-spring-boot-vault-starter from 17.39.0 to 17.39.1
- Make sure JeapPostgreSQLAWSDataSourceAutoConfig is evaluated before Spring's DataSourceAutoConfiguration to avoid
  DataSource bean conflicts.


## [1.2.1] - 2025-07-07
### Changed
- update jeap-starter from 17.39.0 to 17.39.1
- Make sure JeapPostgreSQLAWSDataSourceAutoConfig is evaluated before Spring's DataSourceAutoConfiguration to avoid
  DataSource bean conflicts.


## [1.2.0] - 2025-07-04

### Changed
- Update parent from 5.10.2 to 5.11.0
- update jeap-starter from 17.38.0 to 17.39.0
- update jeap-spring-boot-vault-starter from 17.38.0 to 17.39.0
- update protobuf-java from 4.30.2 to 4.31.1
- update maven.api from 3.9.9 to 3.9.10
- update testcontainers from 1.21.0 to 1.21.3
- update jeap-crypto from 3.23.0 to 3.24.0
- update org.eclipse.jgit from 7.2.0.202503040940-r to 7.3.0.202506031305-r
- update jeap-messaging from 8.49.1 to 8.51.0
- update guava-testlib from 31.1-jre to 33.4.8-jre
- update schema-registry-serde from 1.1.23 to 1.1.24
- update avro-serializer from 7.9.0 to 7.9.2
- Removed tomcat-embed-core as managed dependency

## [1.1.1] - 2025-06-30
### Changed
- update jeap-messaging from 8.49.0 to 8.49.1
- The logging in the MessageTypeRegistryVerifierMojo now respects the Maven logging configuration.


## [1.1.0] - 2025-06-30
### Changed
- update jeap-messaging from 8.47.1 to 8.49.0
- Support for privileged producer in message signature validation (for mirrormaker)


## [1.0.0] - 2025-06-24

### Added

- Initial release of jEAP Server-Sent Events (SSE) library.
