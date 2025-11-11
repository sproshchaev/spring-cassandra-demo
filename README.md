# spring-cassandra-demo
# Пример работы из spring-boot с 

### Запуск кластера Cassandra и сервиса spring-cassandra-demo 
1. Запустить Docker

2. Развернуть кластер из [docker-compose.yaml](docker-compose.yaml)

3. Дождаться в логах конейнера cassandra-db сообщения 
```angular2html
INFO [main] 2025-11-11 14:26:24,902 CassandraDaemon.java:776 - Startup complete
```

4. Запуск утилиты из Docker
```bash
docker exec -it cassandra-db cqlsh
```
5. Команды в консольной утилите cqlsh:

`5.1` Просмотр всех ключевых пространств (keyspaces - аналог базы данных в реляционных СУБД)
```text
DESCRIBE KEYSPACES;
```

`5.2` Пример создания keyspaces:  
```text
CREATE KEYSPACE userkeyspace
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};
```

`5.3` Посмотреть все таблицы которые есть в keyspaces
```text
DESCRIBE TABLES;
```

`5.3.1` Просмотреть все структуры (таблицы, типы, функции и т.д.)
```text
DESCRIBE userkeyspace;
```

`5.4` Пример создания таблицы person в userkeyspace
```text
CREATE TABLE IF NOT EXISTS userkeyspace.person (
    user_id UUID,
    name TEXT,
    email TEXT,
    created_at TIMESTAMP,
    status TEXT,
    PRIMARY KEY (user_id, created_at)
);
```

`5.5` Добавить запись (INSERT):
```text
INSERT INTO userkeyspace.person (user_id, created_at, email, name, status)
VALUES (uuid(), toTimestamp(now()), 'john@example.com', 'John Doe', 'active');
```

`5.6` Найти запись по name (SELECT) c ALLOW FILTERING (Плохо для производительности)
```text
SELECT * FROM userkeyspace.person WHERE name = 'John Doe' ALLOW FILTERING;
```

`5.7` Создать индекс по email
```text
CREATE INDEX ON userkeyspace.person (email);
```

`5.8` Найти запись по email (индекс есть, ALLOW FILTERING не требуется) 
```text
SELECT * FROM userkeyspace.person WHERE email = 'john@example.com';
```

6. Запустить [SpringCassandraDemoApp.java](src/main/java/com/prosoft/SpringCassandraDemoApp.java)  


7. Получить всех пользователей
```bash
curl -X GET http://localhost:8080/users
```

8. Создать нового пользователя
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Иван Иванов",
    "email": "ivan@example.com",
    "age": 30
  }'
```

9. Получить пользователя по ID
```bash
curl -X GET http://localhost:8080/users/{id}
```

10. Удалить пользователя по ID
```bash
curl -X DELETE http://localhost:8080/users/{id}
```

---

# Apache Cassandra: теоретическая часть

## Содержание

- [Введение](#введение)
- [Архитектура](#архитектура)
- [Модель данных](#модель-данных)
- [Ключевые особенности](#ключевые-особенности)
- [Применение](#применение)
- [Сравнение с реляционными БД](#сравнение-с-реляционными-бд)
- [Консистентность и доступность (CAP)](#консистентность-и-доступность-cap)
- [Примеры использования](#примеры-использования)

---

## Введение

**Apache Cassandra** — это **распределённая**, **масштабируемая**, **NoSQL** база данных, разработанная для **обработки больших объёмов данных** на **множестве серверов**, без **единой точки отказа**.

- **Открытое ПО**, изначально разработанное в **Facebook**, позже передано **Apache Software Foundation**.
- Используется в **крупных компаниях**, таких как **Netflix**, **Instagram**, **Apple**, **eBay** и др.

---

## Архитектура

### Узлы и кольцо

- Cassandra использует **peer-to-peer архитектуру**, где **все узлы равны** (no master/slave).
- Данные распределяются по **кольцу узлов**, используя **консистентный хэш**.
- Каждый узел отвечает за **определенную часть данных** (сегмент кольца).

### Репликация

- Данные **реплицируются** на **несколько узлов** (настраивается через `replication_factor`).
- Репликация обеспечивает **надёжность** и **доступность**.

### Partitioning

- Данные **разбиваются на партиции** по **partition key**.
- Каждая партиция хранится на определённом узле, согласно **хэш-функции** от `partition key`.

---

## Модель данных

### Ключевые понятия

- **Keyspace** — аналог **базы данных** в реляционных СУБД.
- **Table** — коллекция строк.
- **Row** — набор колонок, идентифицируется **partition key**.
- **Partition Key** — определяет, на каком узле будет храниться строка.
- **Clustering Columns** — определяют **сортировку** данных внутри партиции.
- **Composite Key** — сочетание `partition key` и `clustering columns`.

### Пример структуры

```cql
CREATE TABLE users (
    user_id UUID,
    name TEXT,
    email TEXT,
    created_at TIMESTAMP,
    PRIMARY KEY (user_id, created_at)
);
```

- `user_id` — **partition key**.
- `created_at` — **clustering column** (сортировка по времени).

---

## Ключевые особенности

### 1. **Масштабируемость**

- **Горизонтальное масштабирование** — легко добавлять новые узлы.
- **Без остановки системы**.

### 2. **Высокая доступность**

- Нет **единой точки отказа**.
- Репликация данных обеспечивает **доступность** даже при падении узлов.

### 3. **Низкая задержка**

- Подходит для **OLTP** систем.
- **Оптимизирована для быстрых записей и чтений**.

### 4. **Распределённость**

- Работает **в кластере**.
- Поддерживает **множественные датацентры**.

### 5. **CQL (Cassandra Query Language)**

- Язык запросов, **похожий на SQL**, но **ограниченный**.
- Поддерживает `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE`, `ALTER`.

---

## Применение

- **Хранение временных данных** (например, логи, метрики).
- **Интернет-магазины**, **социальные сети**, **IoT-данные**.
- **Реал-тайм аналитика**.
- **Системы с высокой доступностью** и **масштабируемостью**.

---

## Сравнение с реляционными БД

| Характеристика | Cassandra | Реляционная БД |
|----------------|-----------|----------------|
| Модель данных | Wide-column store | Табличная (строки/колонки) |
| Транзакции | Нет (не ACID) | Полная поддержка ACID |
| Масштабируемость | Горизонтальная | В основном вертикальная |
| Язык запросов | CQL | SQL |
| Консистентность | Настройка (eventual, strong) | Строгая |
| Сложность репликации | Простая | Требует настройки |

---

## Консистентность и доступность (CAP)

- **Cassandra** — **AP** система по теореме **CAP**.
- **Доступна (Available)** и **разделяема (Partition-tolerant)**.
- **Консистентность (Consistency)** — **настраивается**:
    - `ONE` — минимальная задержка, возможна несогласованность.
    - `QUORUM` — баланс между консистентностью и доступностью.
    - `ALL` — максимальная консистентность, но медленнее.

---

## Примеры использования

- **Netflix** — хранение метаданных, рекомендательные системы.
- **Instagram** — хранение изображений и активности пользователей.
- **Apple** — iCloud.
- **eBay** — аналитика и хранение транзакций.

---
