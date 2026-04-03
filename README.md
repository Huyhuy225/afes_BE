# 🛡️ AFES Backend - API Service

This repository contains the Backend service for the **Advanced Fire Emergency System (AFES)**. It provides a robust RESTful API built with **Java Spring Boot** to manage IoT sensor data (temperature, smoke), process safety statuses, and serve data to the Frontend dashboard.

---

## 🛠️ Tech Stack

* **Framework:** Java 17, Spring Boot 3.x
* **Database & ORM:** Spring Data JPA, Hibernate (MySQL / PostgreSQL)
* **API Documentation:** Swagger / OpenAPI 3.0
* **Containerization:** Docker
* **Deployment:** Azure Container Apps

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed on your local machine:
* **Java Development Kit (JDK) 17** or higher.
* **Maven** (or use the included Maven wrapper `mvnw`).
* **Docker** (optional, for containerized database or running the app).
* A relational database (e.g., MySQL or PostgreSQL) running locally.

---

## ⚙️ Environment Variables

To run this project locally, you need to configure the following environment variables (or update them in your `application-dev.properties` / `application.yml` file):

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `DB_URL` | Database connection URL | `jdbc:mysql://localhost:3306/afes_db` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `password` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend URLs | `http://localhost,https://huyhuy225.github.io` |

---

## 🚀 Running Locally

### Option 1: Using Maven (Local Machine)
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/Huyhuy225/afes-backend.git](https://github.com/Huyhuy225/afes-backend.git)
   cd afes-backend
