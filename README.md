Here’s a **clean, minimal, and smartly formatted README** using only your given content 👇

---

# 🩺 Doctor Management Service

Doctor/Auth microservice for managing doctor directory, availability, prescriptions, care plans, and doctor-side appointment workflows.

---

## 📦 Prerequisites

* Java 17
* Maven
* Docker Desktop

---

## ⚙️ Local Setup

### Build & Run

```bash
mvnw.cmd clean package -DskipTests
mvnw.cmd spring-boot:run
```

---

## 🌐 Default Runtime

* **Service URL:** [http://localhost:8082](http://localhost:8082)

---

## 📡 Endpoints

* `POST /api/auth/login`
* `GET /api/doctors`
* `GET /api/doctors/{id}`
* `GET /api/doctors/me`
* `PUT /api/doctors/me`
* `GET /api/availability/user/{userId}`

---

## 🔐 Environment Variables

```
SPRING_DATA_MONGODB_URI
JWT_SECRET
JWT_EXPIRATION_MS
PATIENT_SERVICE_URL
APPOINTMENT_SERVICE_URL
NOTIFICATION_SERVICE_URL
```

---

## 🐳 Docker

```bash
mvnw.cmd clean package -DskipTests
docker build -t doctor-management-service .
docker run --rm -p 8082:8082 doctor-management-service
```

### Stop Container

```bash
docker stop <container_id>
```

---

## 📝 Notes

* Authentication is delegated to patient-management-service
* Default Patient Service URL: [http://localhost:8081](http://localhost:8081)

---

