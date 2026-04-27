# MarketCurrencyAPI

Два Spring Boot сервиса, общаются по JSON-RPC 2.0 / HTTP. Service discovery — ZooKeeper, балансировка — client-side round-robin.

- `currency-rate-provider` — отдаёт курс пары USDRUB по `POST /rpc`
- `rate-printer` — раз в 5с опрашивает provider и печатает курс

## Запуск (dev)

```bash
docker compose up -d zookeeper prometheus grafana
cd currency-rate-provider && ./gradlew bootRun
cd currency-rate-provider && ./gradlew bootRun --args='--server.port=8082'
cd rate-printer && ./gradlew bootRun
```

## Запуск (prod, всё в docker)

```bash
docker compose up -d --build zookeeper provider-1 provider-2 rate-printer prometheus grafana
```

Сервисы стартуют с `SPRING_PROFILES_ACTIVE=prod`, конфигурация — через env.

## Endpoints

- Provider: `http://localhost:8080/rpc` (dev) / `:18080`, `:18082` (prod)
- Client actuator: `http://localhost:8081/actuator` (dev) / `:18081` (prod)
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin) — дашборды **JVM (Micrometer)** и **RPC Metrics**
- Pact Broker: `http://localhost:9292`

## Тесты и Pact

```bash
cd currency-rate-provider && ./gradlew test
cd rate-printer && ./gradlew test publishPactsToBroker
./run-pact-e2e.ps1
```

Под Java 25: `./run-tests-java25.ps1`

## 12-Factor compliance

| # | Фактор | Статус | Реализация |
|---|--------|--------|-----------|
| I | Codebase | OK | Один git-репозиторий, два модуля (`currency-rate-provider`, `rate-printer`), shared-кода вне репо нет |
| II | Dependencies | OK | Зависимости явно объявлены в `build.gradle` обоих модулей, версии зафиксированы через Spring Boot BOM |
| III | Config | OK | Все внешние адреса/параметры через env: `ZOOKEEPER_CONNECT_STRING`, `SPRING_PROFILES_ACTIVE`, `SERVER_PORT`, `PACT_BROKER_BASE_URL`. В main-коде hardcoded адресов нет |
| IV | Backing services | OK | ZooKeeper и Pact Broker подключаются как attached resources через URL из env |
| V | Build / Release / Run | **Исправлено** | Multistage [Dockerfile](currency-rate-provider/Dockerfile) собирает `bootJar` отдельным stage'ом, run-стадия только запускает артефакт. Никакого `gradle bootRun` |
| VI | Processes | OK | Stateless: нет файлового/сессионного состояния. In-memory счётчики (`idSequence`, `lastKnownInstances`) пересоздаются при рестарте |
| VII | Port binding | OK | Embedded Tomcat, сервисы экспортируются через привязку портов без внешнего app-сервера |
| VIII | Concurrency | OK | Provider горизонтально масштабируется (в compose два инстанса `provider-1`/`provider-2`), client-side round-robin через discovery |
| IX | Disposability | **Исправлено** | `server.shutdown: graceful` + `spring.lifecycle.timeout-per-shutdown-phase: 30s`; на client'е — `spring.task.scheduling.shutdown.await-termination: true`; `stop_grace_period: 30s` в docker-compose |
| X | Dev / Prod parity | **Исправлено** | Spring-профили `dev` / `prod` ([application-dev.yml](currency-rate-provider/src/main/resources/application-dev.yml), [application-prod.yml](currency-rate-provider/src/main/resources/application-prod.yml)). Один артефакт, переключение через `SPRING_PROFILES_ACTIVE` |
| XI | Logs | **Исправлено** | Логи только в stdout, file-appender'ов нет (проверено grep'ом). Docker/k8s забирает как event stream |
| XII | Admin processes | OK | Разовые задачи (`run-pact-e2e.ps1`, gradle-таски) лежат в том же репо рядом с прикладным кодом |
