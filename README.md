Here's the README:

---

```markdown
<div align="center">

# FlowJob

**The hiring platform built for modern teams.**
Post jobs, manage pipelines, and hire faster — without the enterprise bloat.

[![Build](https://img.shields.io/github/actions/workflow/status/yourorg/flowjob/ci.yml?branch=main&style=flat-square)](https://github.com/yourorg/flowjob/actions)
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0--beta-orange?style=flat-square)]()
[![Java](https://img.shields.io/badge/Java-21-red?style=flat-square)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?style=flat-square)]()

[Live Demo](https://flowjob.dev) · [API Docs](https://api.flowjob.dev/swagger-ui.html) · [Changelog](CHANGELOG.md) · [Report a Bug](https://github.com/yourorg/flowjob/issues)

</div>

---

## What is FlowJob?

FlowJob is a multi-tenant SaaS job board and applicant tracking system. Employers post roles, manage candidates through a hiring pipeline, and collaborate with their team — all through a clean REST API. Candidates discover jobs, apply, and track their applications in real time.

It is self-hostable, API-first, and designed for teams that want to own their hiring data.

---

## Features

### For employers
- Multi-tenant workspaces — each company gets an isolated environment
- Job posting with rich metadata: skills, salary range, location, remote policy
- Applicant pipeline with configurable stages (Applied → Reviewed → Interview → Offer → Hired)
- Team collaboration with role-based access (Admin, Hiring Manager, Interviewer)
- Bulk job import via CSV
- Automated email notifications at every pipeline stage
- Exportable reports (PDF / CSV) for hiring analytics

### For candidates
- Full-text job search with filters: location, salary, job type, skills, remote
- One-click apply with resume upload
- Real-time application status tracking
- Saved jobs and personalized job alerts

### Platform
- JWT authentication with refresh token rotation
- Google OAuth2 login
- Rate limiting per tenant and per endpoint
- Webhook delivery for pipeline events
- OpenAPI 3.1 spec for every endpoint
- Audit log of every action in the system

---

## Architecture

FlowJob is composed of four independently deployable services behind an Nginx reverse proxy.

```
                        ┌─────────────────────────────┐
                        │        Nginx (TLS)           │
                        │   rate limiting · routing    │
                        └────────────┬────────────────┘
                                     │
          ┌──────────────┬───────────┼────────────┬──────────────┐
          ▼              ▼           ▼            ▼              ▼
    ┌───────────┐ ┌───────────┐ ┌────────┐ ┌──────────┐ ┌────────────┐
    │   Auth    │ │    Job    │ │  File  │ │ Notify   │ │  Gateway   │
    │  Service  │ │  Service  │ │Service │ │ Service  │ │  Service   │
    └─────┬─────┘ └─────┬─────┘ └───┬────┘ └────┬─────┘ └────────────┘
          │              │           │            │
          └──────────────┴─────┬─────┴────────────┘
                               │
          ┌──────────┬─────────┼──────────┬──────────┐
          ▼          ▼         ▼          ▼          ▼
      PostgreSQL   Redis    RabbitMQ   Kafka      MinIO
```

| Service | Responsibility |
|---|---|
| `auth-service` | Registration, login, JWT issuance, OAuth2, RBAC |
| `job-service` | Jobs, applications, pipeline, search, batch import |
| `file-service` | Resume uploads, report generation, MinIO integration |
| `notification-service` | Email delivery, webhooks, async event consumers |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 3.3 |
| Security | Spring Security 6, JWT, OAuth2 |
| Persistence | PostgreSQL 16, Spring Data JPA, Hibernate, Flyway |
| Cache | Redis 7, Spring Cache, Caffeine (L1) |
| Messaging | RabbitMQ 3.13, Apache Kafka 3.7 |
| Object storage | MinIO (S3-compatible) |
| Search | Elasticsearch 8 |
| API docs | OpenAPI 3.1, Springdoc |
| Observability | Prometheus, Grafana, Loki, Jaeger, OpenTelemetry |
| Secrets | HashiCorp Vault |
| Proxy | Nginx |
| CI/CD | GitHub Actions, Jenkins, Ansible |
| Containers | Docker, Docker Compose |

---

## Getting Started

### Prerequisites

- Docker 24+ and Docker Compose v2
- Java 21 (for local development without Docker)
- Make

### Run locally with Docker Compose

```bash
git clone https://github.com/yourorg/flowjob.git
cd flowjob
cp .env.example .env
make up
```

This starts the full stack: all four services, PostgreSQL, Redis, RabbitMQ, Kafka, MinIO, and the observability stack.

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| RabbitMQ Management | http://localhost:15672 |
| MinIO Console | http://localhost:9001 |
| Grafana | http://localhost:3000 |
| Jaeger UI | http://localhost:16686 |

### Seed demo data

```bash
make seed
```

Creates a demo employer account, 500 sample jobs, and 50 candidate accounts.

```
Employer  →  demo@employer.com  /  password: Demo1234!
Candidate →  demo@candidate.com /  password: Demo1234!
Admin     →  admin@flowjob.dev  /  password: Admin1234!
```

---

## API Quick Reference

### Authentication

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"Secret123!","role":"EMPLOYER"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"Secret123!"}'
```

### Jobs

```bash
# Search jobs (paginated + filtered)
curl "http://localhost:8080/api/v1/jobs?location=Remote&minSalary=80000&skills=Java,Spring&page=0&size=20" \
  -H "Authorization: Bearer <token>"

# Post a job
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Senior Backend Engineer",
    "location": "Remote",
    "type": "FULL_TIME",
    "salaryMin": 90000,
    "salaryMax": 130000,
    "skills": ["Java", "Spring Boot", "PostgreSQL"]
  }'

# Apply to a job
curl -X POST http://localhost:8080/api/v1/jobs/{jobId}/apply \
  -H "Authorization: Bearer <token>" \
  -F "resume=@resume.pdf" \
  -F "coverLetter=I am excited to apply..."
```

Full API reference: [api.flowjob.dev/swagger-ui.html](https://api.flowjob.dev/swagger-ui.html)

---

## Configuration

All configuration is environment-based. Copy `.env.example` to `.env` and edit.

Key variables:

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=flowjob
DB_USER=flowjob
DB_PASSWORD=changeme

# JWT
JWT_SECRET=your-256-bit-secret
JWT_ACCESS_EXPIRY_MS=900000
JWT_REFRESH_EXPIRY_MS=604800000

# OAuth2 (Google)
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=changeme

# Mail
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=postmaster@mg.flowjob.dev
MAIL_PASSWORD=changeme
```

For production, secrets are managed via HashiCorp Vault. See [docs/vault-setup.md](docs/vault-setup.md).

---

## Deployment

### Production (Debian VPS)

Provision a fresh server and deploy everything with a single Ansible command:

```bash
cd infra/ansible
ansible-playbook -i inventory/production playbooks/provision.yml
ansible-playbook -i inventory/production playbooks/deploy.yml
```

This installs Docker, configures Nginx with SSL (via Certbot), pulls images from the registry, and starts all services.

### Blue-green deployment

```bash
# Deploy new version to green slot
make deploy-green VERSION=1.2.0

# Verify green is healthy
make health-check-green

# Switch Nginx traffic to green
make switch-green

# Roll back to blue if anything is wrong
make rollback
```

### CI/CD pipeline

Every push to `main`:

1. GitHub Actions builds and tests all services
2. Docker images are built and pushed to the self-hosted registry
3. Jenkins pulls the new images and triggers the Ansible deploy playbook
4. Slack notification on success or failure

See [.github/workflows/ci.yml](.github/workflows/ci.yml) and [Jenkinsfile](Jenkinsfile).

---

## Observability

### Metrics and dashboards

Grafana is pre-configured with four dashboards:

- **Platform overview** — requests/sec, error rate, p95 latency per service
- **Business metrics** — jobs posted per day, applications per hour, conversion funnel
- **Infrastructure** — CPU, memory, disk per container
- **Messaging** — RabbitMQ queue depth, Kafka consumer lag

Access Grafana at `http://your-server:3000` (default credentials in `.env.example`).

### Distributed tracing

Every request gets a trace ID propagated across all services. View full request waterfalls in Jaeger at `http://your-server:16686`.

### Structured logging

All services emit JSON logs shipped to Loki via Promtail. Query them in Grafana's Explore view correlated against traces.

---

## Project Structure

```
flowjob/
├── auth-service/          # Authentication, JWT, OAuth2, RBAC
├── job-service/           # Jobs, applications, pipeline, batch
├── file-service/          # Resume uploads, exports, MinIO
├── notification-service/  # Email, webhooks, event consumers
├── infra/
│   ├── ansible/           # Server provisioning and deployment
│   ├── docker/            # Dockerfiles and Compose files
│   ├── nginx/             # Nginx configs (reverse proxy, TLS)
│   └── monitoring/        # Prometheus, Grafana, Loki configs
├── .github/workflows/     # GitHub Actions CI pipeline
├── Jenkinsfile            # Jenkins deployment pipeline
├── docs/
│   ├── adr/               # Architecture Decision Records
│   ├── api/               # OpenAPI specs
│   └── runbooks/          # Incident runbooks per alert
└── Makefile               # Dev, build, deploy commands
```

---

## Roadmap

- [ ] Kanban-style hiring board UI (React)
- [ ] AI-assisted resume screening
- [ ] LinkedIn job import integration
- [ ] Multi-language support (i18n)
- [ ] SSO via SAML 2.0
- [ ] Kubernetes (k3s) deployment option
- [ ] Mobile API (GraphQL)

---

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) first.

```bash
# Run tests
make test

# Run integration tests (requires Docker)
make test-integration

# Lint
make lint
```

All services have unit tests (JUnit 5 + Mockito) and integration tests (Testcontainers). CI must pass before any PR is merged.

---

## License

MIT License. See [LICENSE](LICENSE) for details.

---

<div align="center">
Built with Spring Boot · Deployed on Linux · Monitored with Grafana
</div>
```
