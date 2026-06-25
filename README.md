<div align="center">

# 🧠 DeskMind
### AI-Powered Enterprise Helpdesk Platform

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![MySQL](https://img.shields.io/badge/MySQL_8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

DeskMind is a full-stack, role-based helpdesk solution that automates ticket triage using AI, dynamically balances agent workloads, and provides enterprise-grade SLA compliance tracking.

</div>

---

## ✨ Core Features

- **AI-Driven Ticket Triage** — Integrates the Groq LLM API (Llama 3 / Mixtral) to classify incoming tickets by category and priority in real time, eliminating manual triage overhead
- **Capacity-Aware Load Balancing** — A dynamic routing engine built with Spring Data JPA assigns tickets based on real-time agent availability and active workload metrics
- **Automated Lifecycle Management** — Enforces a strict 5-stage ticket state machine backed by a Spring Boot `@Scheduled` cron job that auto-archives inactive resolved tickets
- **Enterprise Compliance & Audit** — Dynamically configurable SLA policies via the Admin dashboard, with an asynchronous append-only audit ledger capturing 100% of state transitions and routing events

---

## 🏗️ System Architecture

```
React Frontend  ──HTTPS/JWT──►  Spring Security API Gateway
                                        │
                              REST Controllers
                                        │
                              Service Layer
                               ┌────────┴────────┐
                          Groq AI Engine     MySQL Database
                                        │
                          Auto-Close Cron Job (@Scheduled)
```

---

## 🔄 Ticket Lifecycle

DeskMind enforces a strict lifecycle ensuring no customer request is dropped or ignored.

```
Customer Submission
       │
       ▼
    [ OPEN ]
       │
       │  AI Triage & Agent Assignment
       ▼
[ IN_PROGRESS ] ◄──────────────────┐
       │                           │
       │  Agent Requests Info      │  Customer Responds
       ▼                           │
  [ WAITING ] ─────────────────────┘
       │
       │  Solution Provided
       ▼
  [ RESOLVED ]
       │
       │  Cron Job (72h Inactivity)       OR      Customer Reopens
       ▼                                                  │
  [ CLOSED ]                                    back to [ IN_PROGRESS ]
```

---

## 🛠️ Technology Stack

### Backend
| Technology | Purpose |
|-----------|---------|
| Java 21 + Spring Boot 3.x | Core framework |
| Spring Security + JWT | Stateless authentication |
| Spring Data JPA + Hibernate | Data persistence |
| MySQL 8 | Relational database |
| Groq Cloud API | LLM integration (Llama 3 / Mixtral) |

### Frontend
| Technology | Purpose |
|-----------|---------|
| React.js + Vite | UI framework |
| Tailwind CSS | Styling |
| React Router DOM | Client-side routing |
| Recharts | Analytics dashboard charts |
| Axios | HTTP client with JWT interceptor |

---

## 👥 Role-Based Workspaces (RBAC)

DeskMind provides four distinct secured interfaces:

| Role | Access |
|------|--------|
| **Customer** | Submit tickets, track status, communicate with agents |
| **Agent** | Real-time queue management, AI-generated draft responses, presence toggling (Online/Offline) |
| **Manager** | Queue reallocation, team metric monitoring, full ticket oversight |
| **Admin** | Global system configuration, SLA rule management, audit ledger review |

---

## 🚀 Local Setup

### Prerequisites
- Java 21
- Node.js 18+
- MySQL 8
- Maven

### 1. Clone the Repository

```bash
git clone https://github.com/rishanth11/deskmind.git
cd deskmind
```

### 2. Database Configuration

Make sure MySQL is running, then create the database:

```sql
CREATE DATABASE deskmind;
```

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/deskmind
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 3. AI Configuration

Get a free API key from [Groq Cloud](https://console.groq.com) and add it to your properties file:

```properties
groq.api.key=your_groq_api_key_here
```

> ⚠️ Never commit your API key to GitHub. Use environment variables in production.

### 4. Run the Backend

```bash
./mvnw spring-boot:run
```

Backend starts at `http://localhost:8080`

### 5. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts at `http://localhost:5173`

---

## 📁 Project Structure

```
deskmind/
├── src/main/java/com/deskmind/
│   ├── config/          # Security, CORS, async config
│   ├── controller/      # REST API controllers
│   ├── service/         # Business logic
│   ├── repository/      # JPA repositories
│   ├── model/           # JPA entities
│   ├── dto/             # Request/response DTOs
│   ├── exception/       # Global exception handling
│   └── util/            # JWT utility, helpers
├── src/main/resources/
│   └── application.properties
├── frontend/
│   ├── src/
│   │   ├── api/         # Axios instance + API calls
│   │   ├── context/     # AuthContext
│   │   ├── pages/       # Route-level components
│   │   ├── components/  # Reusable UI components
│   │   └── hooks/       # Custom React hooks
│   └── package.json
└── pom.xml
```

---

## 🔌 API Overview

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login and receive JWT |
| POST | `/api/tickets` | Customer | Submit a new ticket |
| GET | `/api/tickets` | Agent, Manager | Get all tickets |
| GET | `/api/tickets/my` | Customer | Get own tickets |
| PUT | `/api/tickets/{id}/status` | Agent, Manager | Update ticket status |
| PUT | `/api/tickets/{id}/assign` | Manager, Admin | Assign ticket to agent |
| POST | `/api/tickets/{id}/responses` | All | Add reply or internal note |
| GET | `/api/analytics/summary` | Manager, Admin | Get analytics data |

---

## 📊 SLA Policy

| Priority | Response Deadline | Typical Use Case |
|----------|------------------|-----------------|
| P1 | 1 hour | System down, critical outage |
| P2 | 4 hours | Major feature broken |
| P3 | 24 hours | Standard support request |
| P4 | 72 hours | Minor issue, general inquiry |

---

<div align="center">

Architected and developed by **Rishanth S**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-rishanth11-0077B5?style=for-the-badge&logo=linkedin)](https://linkedin.com/in/rishanth11)
[![GitHub](https://img.shields.io/badge/GitHub-rishanth11-181717?style=for-the-badge&logo=github)](https://github.com/rishanth11)

</div>
