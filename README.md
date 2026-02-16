# MarketCurrencyAPI

Проект состоит из двух независимых Spring Boot сервисов, которые общаются по JSON-RPC 2.0 поверх HTTP.

Service registry реализован через ZooKeeper:
- `currency-rate-provider` автоматически регистрирует свои instance-ы при старте;
- `rate-printer` получает актуальный список instance-ов через discovery;
- выбор instance-а выполняется client-side балансировкой (round-robin).

## Сервисы

### currency-rate-provider

Сервер, который предоставляет текущий курс валютной пары USDRUB через endpoint POST /rpc.

JSON-RPC запрос:

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "getRate",
  "params": { "pair": "USDRUB" }
}
```

JSON-RPC ответ:

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "pair": "USDRUB",
    "rate": 91.2345,
    "timestampMs": 1739123456789
  }
}
```

При ошибках возвращается стандартный JSON-RPC error объект (Invalid Request, Method not found, Invalid params, Internal error).

Параметры генерации курса задаются в [currency-rate-provider/src/main/resources/application.yml](currency-rate-provider/src/main/resources/application.yml).

### rate-printer

Клиент, который каждые 5 секунд вызывает provider и печатает курс в лог.

Конфигурируется через [rate-printer/src/main/resources/application.yml](rate-printer/src/main/resources/application.yml) (service-id, RPC path и таймауты).

Пример лога:

```
2026-02-09T15:15:00Z USDRUB=91.2345
```

## Как запустить

1. Поднять ZooKeeper:

```bash
docker compose up -d zookeeper
```

2. Запустить несколько instance provider.

Первый instance:

```bash
cd currency-rate-provider
./gradlew bootRun
```

Второй instance:

```bash
cd currency-rate-provider
./gradlew bootRun --args='--server.port=8082'
```

3. Запустить consumer:

```bash
cd rate-printer
./gradlew bootRun
```

По умолчанию provider слушает `http://localhost:8080/rpc`.

```bash
ZOOKEEPER_CONNECT_STRING=<host:port>
```

## Наблюдаемость

Логируются ключевые события:
- регистрация provider instance в ZooKeeper;
- обновление списка доступных provider instance у consumer;
- выбор instance балансировщиком для каждого вызова;
- ошибки вызова provider.

Базовые метрики доступны через Actuator (`/actuator/metrics`):
- `provider.instances.available` — число доступных provider instance;
- `provider.calls.total{result=success|failure}` — успешность вызовов;
- `provider.call.latency` — latency вызовов provider.

## Тесты

Запуск тестов для каждого сервиса:

```bash
cd currency-rate-provider
./gradlew test
```

```bash
cd rate-printer
./gradlew test
```
