<div style="display: flex; width: 100%; gap: 10px; align-items: center;">
<img src="https://mycvhub.ch/mycvhub_icon.png" width="60" height="60" alt="MyCVHub Logo" />
<p style="font-weight: bold; font-size: 25px">MyCVHub</p>
</div>

[![Build](https://github.com/streckeisen-dev/MyCVHub/actions/workflows/build.yaml/badge.svg)](https://github.com/streckeisen-dev/MyCVHub/actions/workflows/build.yaml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=streckeisen-dev_MyCVHub&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=streckeisen-dev_MyCVHub)

MyCVHub is an open-source platform for managing CVs.

# Overview

MyCVHub allows users to enter their CV data, generate CV PDFs and have a public online CV.

Current features include:

- CV Management: Managing personal information, work experiences, education entries, projects and skills
- Public Profile: Users can choose to display their CV data in a public profile
- PDF CVs: There are different PDF CV styles (currently 2) available

Planned features:

- Application management: Tracking sent-out applications to keep an overview of all open applications
- PDF Cover Letters: Writing cover letters in based on different PDF templates

# Technologies

| Component      | Technologies                      |
|----------------|-----------------------------------|
| Backend        | Java 26, Kotlin, Spring Boot 4    |
| PDF generation | typst                             |
| Frontend       | Node.js 24, React, HeroUI, yarn   |
| Database       | PostgreSQL                        |
| Hosting        | Docker, DigitalOcean app platform |

# Getting Started

## Prerequisites
- Java 21+
- Node.js 24+
- yarn 4+ (run `corepack enable` if not yet enabled)
- Docker (for local testing)
- Cloudinary account (for profile picture storage)
- GitHub OAuth app (for GitHub 3rd party login)
- Mailgun Account (for email notifications)

## Local Development

MyCVHub requires a PostgreSQL database, which you can conveniently start by running:
```bash
docker compose up postgres
```

To run the backend individually, you can use your IDE's spring boot run configuration (you will need to set up all required env variables, a list of which can be found in the `.env.example` file).

To run the frontend individually, go to the `frontend` directory and run:
```bash
yarn dev
```

You can also run all MyCVHub components at once by using Docker compose.
Before you can run the backend with docker, you need to create a `.env` containing the required environment variables.
```bash
docker compose up
```

## Admin Setup

The Admin UI is separate from regular applicant authentication. Admin users sign in at:

```text
/admin/login
```

There is no public admin signup. The first `SUPER_ADMIN` account is initialized in two steps:

1. Configure the seed username before starting the backend:

   ```bash
   ADMIN_SEED_USERNAME=admin@example.com
   ```

   On startup, the backend ensures this admin row exists, but it does not create a usable password.

2. Run the explicit bootstrap command in an environment that can access the target database:

   ```bash
   java -jar /app/app.jar \
     --spring.profiles.active=prod,admin-bootstrap \
     --spring.main.web-application-type=none \
     --my-cv.admin.bootstrap.username=admin@example.com
   ```

   The command generates a temporary password, prints it once to the interactive operator session, stores only the password hash, marks the admin active, and forces a password change on first login.

   With Docker Compose, set `ADMIN_SEED_USERNAME` and either set `ADMIN_BOOTSTRAP_USERNAME` or let it default to the seed username, then run:

   ```bash
   docker compose --profile admin-bootstrap run --rm admin-bootstrap
   ```

If the temporary password expires or is lost, rerun the same command to generate a new one. To reset an already-active admin, pass the explicit reset flag:

```bash
java -jar /app/app.jar \
  --spring.profiles.active=prod,admin-bootstrap \
  --spring.main.web-application-type=none \
  --my-cv.admin.bootstrap.username=admin@example.com \
  --my-cv.admin.bootstrap.reset-existing=true
```

With Docker Compose, set `ADMIN_BOOTSTRAP_RESET_EXISTING=true` and rerun:

```bash
docker compose --profile admin-bootstrap run --rm admin-bootstrap
```

Admin login has a database-backed per-username lockout shared by all app instances. Configure it with:

```bash
ADMIN_LOGIN_RATE_LIMIT_MAX_FAILED_ATTEMPTS=5
ADMIN_LOGIN_RATE_LIMIT_LOCKOUT_MINUTES=15
```

The normal unlock path is waiting for the lockout window to expire. If the password itself must be reset, rerun the documented bootstrap reset command.

## Frontend Testing
The frontend is tested with both unit tests and Cypress component tests.
To run all frontend unit tests, execute:
```bash
yarn test
```

To run the Cypress component tests, execute:
```bash
yarn cypress
```

# Contributing
If you would like to contribute to the project, you are welcome to do so by:
- Creating GitHub issues with bug reports or feature suggestions
- Adding new features or fixing bugs in your own fork and create pull requests

# License
MyCVHub is licensed under [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.en.html)
