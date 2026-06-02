# 印刷行业综合管理系统 (Print IMS)

A comprehensive management system for the printing industry, covering orders, customers, knife molds management, and data statistics.

## Tech Stack
- **Backend**: Java 17, Spring Boot 3, MyBatis-Plus, MySQL
- **Frontend**: Vue 3, TypeScript, Vite, Element Plus

## Quick Start

### 1. Start Database
```bash
docker-compose up -d
```

### 2. Start Backend
```bash
cd backend
mvn spring-boot:run
```
Backend runs on `http://localhost:8080`. API documentation available at `http://localhost:8080/doc.html`.

### 3. Start Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:5173`.
