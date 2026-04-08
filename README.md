# mcloud-orders

Учебный сервис заказов на Spring Boot + Kafka. Приложение принимает заказ по HTTP, публикует `OrderPlacedEvent` в Kafka, затем consumer читает событие из одного из трёх priority-топиков и сохраняет заказ в PostgreSQL.

## Что реализовано

- producer API `POST /api/v1/orders`
- Kafka listener `OrderEventListener` на топиках:
  - `orders.priority.high`
  - `orders.priority.normal`
  - `orders.priority.low`
- ручной ack через `AckMode.MANUAL_IMMEDIATE`
- входной порт `ProcessOrderEventPort`
- выходной порт `OrderPersistencePort`
- маппинг JSON -> доменная команда в `OrderEventMapper`
- валидация обязательных полей `orderId`, `priority`, `region`
- idempotency по `eventId` и `kafkaOffset`
- сохранение заказа в PostgreSQL через Spring Data JPA
- consumer-метрики по приоритетам и регионам
- endpoint метрик чтения: `GET /api/v1/orders/consumers/metrics`

## Стек

- Java 21
- Spring Boot 3
- Spring Kafka
- PostgreSQL
- Liquibase
- AKHQ
- Docker Compose

## Топики

- `orders.priority.high`
- `orders.priority.normal`
- `orders.priority.low`

Каждый топик создаётся с `3` partition. Consumer listener запускается с `concurrency = 3`, поэтому group может читать partitions параллельно.

## Локальный запуск

### 1. Поднять инфраструктуру

```powershell
docker compose up -d
```

Будут доступны:

- Kafka: `localhost:9092`
- PostgreSQL: `localhost:5434`
- AKHQ: `http://localhost:8080/ui/`

Если хочешь переопределить cluster id Kafka, можно задать переменную окружения `KAFKA_CLUSTER_ID`, но для локального старта в `docker-compose.yml` уже есть дефолтное значение.

### 2. Запустить приложение

```powershell
gradlew.bat :app:bootRun --args="--spring.profiles.active=local"
```

### 3. Прогнать тесты

```powershell
./gradlew test
```

## Проверка отправки заказа

Пример запроса:

```powershell
$body = @{
  customerId = [guid]::NewGuid().ToString()
  region = 'EU'
  priority = 'HIGH'
  amount = 123.45
  lines = @(
    @{
      productId = [guid]::NewGuid().ToString()
      quantity = 2
      price = 61.73
    }
  )
} | ConvertTo-Json -Depth 5

Invoke-RestMethod `
  -Method Post `
  -Uri 'http://localhost:8081/api/v1/orders' `
  -ContentType 'application/json' `
  -Body $body
```

Пример ответа:

```json
{
  "orderId": "315113dd-bee5-4e0e-9344-a941fa047223",
  "status": "QUEUED",
  "dispatchedAt": "2026-04-08T18:24:13.7942956+03:00"
}
```

## Проверка чтения

### 1. Проверить consumer-метрики

```powershell
Invoke-RestMethod -Uri 'http://localhost:8081/api/v1/orders/consumers/metrics'
```

Пример ответа после одного успешно обработанного события:

```json
{
  "totals": {
    "processed": 1,
    "rejected": 0
  },
  "priorities": {
    "HIGH": 1
  },
  "regions": {
    "EU": 1
  }
}
```

### 2. Проверить запись в PostgreSQL

Подключение к БД:

```powershell
docker exec -it mcloud-orders-postgres-1 psql -U orders -d orders
```

Полезные запросы:

```sql
select order_id, event_id, kafka_offset, region, amount, priority, status
from orders
order by created_at desc
limit 10;
```

```sql
select count(*) from orders;
```

Пример строки после обработки события:

```text
315113dd-bee5-4e0e-9344-a941fa047223|6240e912-942d-404b-949a-4390921bdc24|orders.priority.high:0:15|EU|123.45|HIGH|NEW
```

### 3. Проверить lag и consumer group в Kafka

```powershell
docker exec mcloud-orders-kafka-1 kafka-consumer-groups `
  --bootstrap-server localhost:9092 `
  --describe `
  --group orders-processor-group
```

В рабочем состоянии у group должен быть:

- `LAG = 0`
- назначенные partitions
- активные `CONSUMER-ID`

### 4. Проверить AKHQ

AKHQ доступен по адресу:

```text
http://localhost:8080/ui/
```

Что нужно увидеть:

- у топиков `orders.priority.*` есть consumer group `orders-processor-group`
- lag равен `0`
- на странице группы видны активные members и назначенные partitions

Скриншот списка топиков с `lag = 0`:

![AKHQ topics lag zero](docs/images/akhq-home.png)

Скриншот consumer group с активными members:

![AKHQ consumer group members](docs/images/akhq-group-members.png)

## Конфигурация consumer'а

В `application-local.yml` настроено:

- `group-id: orders-processor-group`
- `StringDeserializer` для key/value
- `enable-auto-commit: false`
- `ack-mode: manual_immediate`
- `max-poll-records: 5`
- `max.poll.interval.ms: 120000`

## Что сохраняется в БД

После обработки события создаётся запись заказа с полями:

- `order_id`
- `event_id`
- `kafka_offset`
- `region`
- `amount`
- `priority`
- `status`
- `created_at`

Идемпотентность обеспечивается проверкой:

- по `event_id`
- по `kafka_offset`

## Тесты

В проекте есть:

- unit-тест доменного сервиса `OrderConsumerUseCase`
- интеграционный тест listener'а с `@SpringBootTest` и `EmbeddedKafka`

Запуск:

```powershell
./gradlew test
```
