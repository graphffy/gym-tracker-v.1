# Запуск из IntelliJ IDEA

## Backend

В списке Run Configurations выберите `Gym Tracker Backend` и нажмите Run.

Конфигурация уже содержит шаг `Make` перед запуском, поэтому вручную делать Rebuild перед каждым стартом не нужно. Если IDEA не увидела конфигурацию сразу, нажмите `File -> Reload All from Disk` или переоткройте проект.

Проверьте настройки IDEA:

- `Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors`: включено `Enable annotation processing`.
- Project SDK: Java 17.

## Frontend

1. Установите Node.js LTS, если IDEA пишет, что `node` или `npm` не найден.
2. Откройте терминал IDEA:

```powershell
cd frontend
npm install
```

3. В Run Configurations выберите `GTracker Frontend` и нажмите Run.
4. Откройте `http://localhost:5173`.

Vite проксирует `/api` на `http://localhost:8080`, поэтому backend должен быть запущен рядом через `Gym Tracker Backend`.
