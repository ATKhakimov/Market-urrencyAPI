# MarketCurrencyAPI

Проект состоит из двух независимых Spring Boot сервисов, которые общаются по JSON-RPC 2.0 поверх HTTP.

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

Конфигурируется через [rate-printer/src/main/resources/application.yml](rate-printer/src/main/resources/application.yml) (URL и таймауты).

Пример лога:

```
2026-02-09T15:15:00Z USDRUB=91.2345
```

## Как запустить

В двух отдельных терминалах:

```bash
cd currency-rate-provider
./gradlew bootRun
```

```bash
cd rate-printer
./gradlew bootRun
```

По умолчанию provider слушает http://localhost:8080/rpc.

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

Единый запуск тестов обоих сервисов под Java 25:

```powershell
./run-tests-java25.ps1
```

```
