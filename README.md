# Gym Tracker API

REST API для трекинга тренировок на **Spring Boot + JPA + PostgreSQL**.

---

## Что умеет проект

- CRUD для сущностей:
  - Users
  - Categories
  - Exercises
  - Workouts
  - Workout Sets
- Поиск пользователей по `username` через `@RequestParam`.
- Пагинируемый поиск сетов тренировок:
  - JPQL: `GET /api/v1/sets/search/jpql`
  - Native SQL: `GET /api/v1/sets/search/native`
- In-memory cache для повторных поисковых запросов (`WorkoutSetService`) с:
  - составным ключом,
  - корректными `equals/hashCode`,
  - инвалидацией после `create/update/delete`.
- Checkstyle в Maven lifecycle (`validate`).

---

## Технологии

- Java 17
- Spring Boot 3.2.3
- Spring Web
- Spring Data JPA
- PostgreSQL
- Lombok
- Maven

---

## Запуск

### 1) Настрой БД

Параметры подключения находятся в:

- `src/main/resources/application.properties`

По умолчанию:

- URL: `jdbc:postgresql://localhost:5433/gym_db`
- user: `postgres`
- password: `90802700`

> Рекомендуется вынести креды в переменные окружения для реального окружения.

### 2) Запуск приложения

```bash
mvn spring-boot:run
```

или

```bash
./mvnw spring-boot:run
```

---

## Примеры ключевых endpoint'ов

### Users

- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `GET /api/v1/users/search?username=user1`
- `POST /api/v1/users`
- `PUT /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`

### Workout Sets (новые поисковые)

- `GET /api/v1/sets/search/jpql?username=user&exerciseName=жим&page=0&size=10`
- `GET /api/v1/sets/search/native?username=user&exerciseName=жим&page=0&size=10`

---

## Postman

Коллекция лежит в:

- `postman/GymTracker_FULL_CRUD.postman_collection.json`

Импортируй её в Postman и запускай запросы по папкам.

---

## Структура проекта (символами)

```text
gym-tracker/
├── pom.xml
├── checkstyle.xml
├── README.md
├── postman/
│   └── GymTracker_FULL_CRUD.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/gym/gymtracker/
    │   │   ├── GymTrackerApplication.java
    │   │   ├── controller/
    │   │   │   ├── UserController.java
    │   │   │   ├── CategoryController.java
    │   │   │   ├── ExerciseController.java
    │   │   │   ├── WorkoutController.java
    │   │   │   └── WorkoutSetController.java
    │   │   ├── service/
    │   │   │   ├── UserService.java
    │   │   │   ├── CategoryService.java
    │   │   │   ├── ExerciseService.java
    │   │   │   ├── WorkoutService.java
    │   │   │   └── WorkoutSetService.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── CategoryRepository.java
    │   │   │   ├── ExerciseRepository.java
    │   │   │   ├── WorkoutRepository.java
    │   │   │   └── WorkoutSetRepository.java
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── Category.java
    │   │   │   ├── Exercise.java
    │   │   │   ├── Workout.java
    │   │   │   └── WorkoutSet.java
    │   │   ├── dto/
    │   │   │   ├── UserDto.java
    │   │   │   ├── CategoryDto.java
    │   │   │   ├── ExerciseDto.java
    │   │   │   ├── WorkoutDto.java
    │   │   │   └── WorkoutSetDto.java
    │   │   └── mapper/
    │   │       ├── UserMapper.java
    │   │       ├── CategoryMapper.java
    │   │       ├── ExerciseMapper.java
    │   │       ├── WorkoutMapper.java
    │   │       └── WorkoutSetMapper.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/gym/gymtracker/
            └── GymTrackerApplicationTests.java
```

---

## Проверка качества кода

```bash
mvn validate
```

Checkstyle конфиг:

- `checkstyle.xml`

---

## Примечания

- Для демонстрации кэша включи DEBUG-логи для `WorkoutSetService`:

```properties
logging.level.com.gym.gymtracker.service.WorkoutSetService=DEBUG
```

Тогда в логах увидишь `cache MISS/HIT` и инвалидацию кэша.
