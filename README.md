![MyCVHub](https://mycvhub.ch/mycvhub_icon.png) **MyCVHub**

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

| Component      | Technologies                        |
|----------------|-------------------------------------|
| Backend        | Java 17, Kotlin, Spring Boot 3      |
| PDF generation | typst                               |
| Frontend       | Node.js 22, Vue.js 3, Vuetify, yarn |
| Database       | PostgreSQL                          |
| Hosting        | Docker, DigitalOcean app platform   |

# Getting Started

## Prerequisites
- Java 17+
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