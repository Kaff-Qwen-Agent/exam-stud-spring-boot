# exam-stud-spring-boot

Приложение Spring Boot с H2 и JWT-аутентификацией.

## Запуск

1. Откройте терминал в корне проекта.
2. Запустите приложение:
   ```bash
   ./mvnw spring-boot:run
   ```

Приложение стартует на `http://localhost:8080`.

## Настройки

Файл конфигурации: `src/main/resources/application.properties`

- H2 в памяти: `spring.datasource.url=jdbc:h2:mem:gr;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- DDL авто-создание: `spring.jpa.hibernate.ddl-auto=none`
- Инициализация схемы и данных: `spring.datasource.schema=classpath:Grops_Studs_2021.sql`
- JWT секрет: `jwt.secret`
- Время жизни токена: `jwt.expiration-ms`

## Инициализация данных

`src/main/resources/Grops_Studs_2021.sql` создаёт таблицы и заполняет базу при старте.

По умолчанию создаётся пользователь:

- username: `user`
- password: `password`
- роль: `USER`

## Авторизация

Для получения токена:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'
```

Ответ:

```json
{
  "token": "<jwt-token>",
  "tokenType": "Bearer"
}
```

## Доступ к защищённым эндпоинтам

Пример запроса к `GET /api/group`:

```bash
curl -H "Authorization: Bearer <jwt-token>" http://localhost:8080/api/group
```

Без токена запрос возвращает `403 Forbidden`.

## H2 консоль

Доступна по адресу:

```text
http://localhost:8080/h2-console
```

## Проверка

1. Запустить приложение.
2. Выполнить логин и получить JWT.
3. Сделать запрос к защищённому эндпоинту с `Authorization: Bearer <token>`.
