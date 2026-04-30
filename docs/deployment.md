# Deployment

## Local Docker run

1. Copy `.env.example` to `.env`.
2. Change `DB_PASSWORD` in `.env`.
3. Start the stack:

```bash
docker compose up --build
```

The app opens at `http://localhost:8080`.

Useful endpoints:

- App UI: `http://localhost:8080`
- API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Healthcheck: `http://localhost:8080/actuator/health`

## Environment variables

The backend reads database settings from environment variables:

- `PORT`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `SPRING_JPA_SHOW_SQL`

`SPRING_DATASOURCE_URL` is optional locally. If it is not set, the app builds a JDBC URL from `DB_HOST`, `DB_PORT`, and `DB_NAME`.

## Render deploy

The repository includes `render.yaml`, so Render can create both services from a Blueprint:

- Docker web service: `gym-tracker`
- PostgreSQL database: `gym-tracker-db`

Steps:

1. Push this repository to GitHub.
2. Create a Render account.
3. In Render, choose **New** -> **Blueprint**.
4. Connect the GitHub repository.
5. Select this repo and apply the `render.yaml` Blueprint.
6. Wait for the first deploy.
7. Open the generated `https://...onrender.com` URL.

Free Render Postgres is suitable for demos, but it expires after 30 days.

## GitHub CI/CD

The workflow in `.github/workflows/ci.yml` does this:

- builds the frontend;
- runs backend checks and tests with Maven;
- builds the Docker image;
- deploys to Render on pushes to `main` or `master`;
- checks `/actuator/health` after deploy.

For deploy and healthcheck, configure these in GitHub:

1. Repository **Settings** -> **Secrets and variables** -> **Actions**.
2. Add secret `RENDER_DEPLOY_HOOK_URL`.
   - Render service -> **Settings** -> copy the Deploy Hook URL.
3. Add repository variable `RENDER_SERVICE_URL`.
   - Example: `https://gym-tracker.onrender.com`

If these values are missing, the workflow still builds and tests, but deploy/healthcheck are skipped.
