# User Management API

A Spring Boot REST API for managing users and project groups, containerised with Docker and deployed to Kubernetes.

---

## Tech Stack

- **Java 21** + **Spring Boot 3.2**
- **H2** in-memory database
- **Docker** (multi-stage build)
- **Kubernetes** (Docker Desktop)

---

## Project Structure

```
user-group-api/
├── src/main/java/com/api/usermanagement/
│   ├── controller/        # REST controllers
│   ├── service/           # Business logic
│   ├── repository/        # JPA repositories
│   ├── model/             # JPA entities
│   ├── dto/               # Request/Response DTOs
│   └── exception/         # Exception handling
├── k8s/
│   ├── 00-namespace.yaml  # development namespace
│   ├── 01-deployment.yaml # Deployment (2 replicas)
│   └── 02-service.yaml    # NodePort service
├── Dockerfile             # Multi-stage build
├── deploy.sh              # One-command deploy script
└── pom.xml
```

---

## Quick Start

### Prerequisites
- Docker Desktop with Kubernetes enabled
- Java 21+ (for local dev only)
- Maven 3.9+ (for local dev only)

### Distroless image - nonroot user

### Deploy to Kubernetes (one command)

```bash
chmod +x deploy.sh
./deploy.sh
```

The API will be available at: **http://localhost:8080**

---

## API Endpoints

### Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/users` | Create a new user |
| `GET` | `/api/v1/users` | List all users |
| `GET` | `/api/v1/users/{id}` | Get user by ID |
| `POST` | `/api/v1/users/{userId}/groups/{groupId}` | Add user to group |
| `DELETE` | `/api/v1/users/{userId}/groups/{groupId}` | Remove user from group |
| `DELETE` | `/api/v1/users/{id}` | Delete a user |

### Groups

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/groups` | Create a new group |
| `GET` | `/api/v1/groups` | List all groups |
| `GET` | `/api/v1/groups/{id}` | Get group by ID |
| `DELETE` | `/api/v1/groups/{id}` | Delete a group |

### Health

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/actuator/health` | Health check |

---

## Example Usage

### 1. Create a User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "username": "johndoe"
  }'
```

### 2. Create a Group
```bash
curl -X POST http://localhost:8080/api/v1/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Backend Team",
    "description": "Backend engineering team"
  }'
```

### 3. Add User to Group
```bash
curl -X POST http://localhost:8080/api/v1/users/1/groups/1
```

### 4. Get All Users
```bash
curl http://localhost:8080/api/v1/users
```

### 5. Get All Groups (with members)
```bash
curl http://localhost:8080/api/v1/groups
```

---

## Kubernetes

### Namespace
All resources run in the `development` namespace.

### Architecture
- **Deployment**: 2 replicas with rolling update strategy
- **Service**: NodePort on port `30080` (accessible via localhost on Docker Desktop)
- **Image pull policy**: `Never` — uses the locally built Docker image

### Useful Commands

```bash
# View pods
kubectl get pods -n development

# View services
kubectl get services -n development

# Stream logs
kubectl logs -f deployment/user-management-api -n development

# Describe deployment
kubectl describe deployment user-management-api -n development

# Scale replicas
kubectl scale deployment user-management-api --replicas=3 -n development

# Teardown
kubectl delete namespace development
```

---

## Manual Build Steps (without deploy.sh)

```bash
# Build Docker image
docker build -t user-management-api:1.0.0 .

# Apply Kubernetes manifests
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-deployment.yaml
kubectl apply -f k8s/02-service.yaml

# Watch rollout
kubectl rollout status deployment/user-management-api -n development
```
# curl to create user
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "username": "johndoe"
  }' | jq .

### Run load tests
# Standard run (full scenario — ramp 0→10→30→0 VUs over ~3.5 min)
k6 run load-test.js

# Override the base URL if needed
k6 run -e BASE_URL=http://localhost:8081 load-test.js

# Quick smoke test (1 VU, 10 iterations, no stages)
k6 run --vus 1 --iterations 10 load-test.js

# Run against local Spring Boot (mvn spring-boot:run on 8080)
k6 run -e BASE_URL=http://localhost:8080 load-test.js

# Save results to JSON for later analysis
k6 run --out json=results.json load-test.js


# Roll back to the previous revision (one step back)
helm rollback user-management-api -n development

# Roll back to a specific revision number
helm rollback user-management-api 5 -n development   # goes back to 1.0.8

# Roll back and wait for pods to be ready
helm rollback user-management-api 5 -n development --wait --timeout 90s

# Dry-run — see what would change without applying
helm rollback user-management-api 5 -n development --dry-run

# Check history after rollback
helm history user-management-api -n development




