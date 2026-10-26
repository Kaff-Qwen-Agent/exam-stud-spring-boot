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

По умолчанию создаются пользователи:

- `user` / `password` — роль `USER` (без доступа к API)
- `student` / `student123` — роль `STUDENT` (просмотр данных о студентах)
- `teacher` / `teacher123` — роль `TEACHER` (просмотр данных о группах и статистики)
- `admin` / `admin123` — роль `ADMIN` (полный доступ)

## Роли

- `STUDENT`: просмотр студентов — `GET /api/student` (в т.ч. с диапазоном id) и `GET /api/student/{id}`
- `TEACHER`: просмотр групп — `GET /api/group`, `GET /api/group/{id}`, `GET /api/group/statistics`
- `ADMIN`: создание/изменение/удаление групп и студентов, добавление студентов в группу

## Дополнительные эндпоинты

Отфильтрованный список студентов с id в диапазоне, с пагинацией и сортировкой (роль STUDENT или ADMIN):

```bash
curl -H "Authorization: Bearer <jwt-token>" \
  "http://localhost:8080/api/student?from=10100&to=10200&page=0&size=10&sort=familiya,asc"
```

Параметры пагинации: `page` (с 0), `size`, `sort` (опционально `свойство,asc|desc`). Ответ — JSON-страница Spring (`content`, `totalElements`, `totalPages`, ...).

Статистика: количество студентов каждого статуса в каждой группе (роль TEACHER или ADMIN):

```bash
curl -H "Authorization: Bearer <jwt-token>" http://localhost:8080/api/group/statistics
```

Дубликаты возвращают `409 Conflict`: повторное название группы при создании/переименовании, повторный номер зачётки студента.

## Swagger

Интерактивная документация API (без авторизации):

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

В Swagger UI нажмите «Authorize» и введите JWT-токен (без слова `Bearer`), чтобы выполнять запросы из интерфейса.

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
