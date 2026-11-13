# 🏨 StayHaven – Full-Stack Hotel Booking Portal

[![Watch Full Demo Video on YouTube](https://img.shields.io/badge/YouTube-Watch%20Demo%20Video-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://youtu.be/rni0sTSGE2I)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-v4-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

> [!IMPORTANT]
> ### 🎬 Live Project Demonstration & Video Walkthrough
> **Watch the full application walkthrough on YouTube:**
>
> [![Watch StayHaven Demo Video on YouTube](https://img.shields.io/badge/▶%EF%B8%8F%20Click%20Here%20to%20Watch%20Demo%20on%20YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://youtu.be/rni0sTSGE2I)
>
> 🔗 **Direct URL**: [https://youtu.be/rni0sTSGE2I](https://youtu.be/rni0sTSGE2I)
>
> *Demonstrating interactive hotel search & date filtering, real-time availability counts, 10% Smart Stay long-stay discounts, pessimistic concurrency locking (`SELECT ... FOR UPDATE`), and instantaneous booking cancellations.*

A modern, production-grade full-stack hotel booking platform built with **Java 17+ / Spring Boot 3**, **React 19 / Vite**, **Tailwind CSS**, and **MySQL**.

Developed as a technical assessment demonstrating full-stack engineering, clean layered architecture, transactional concurrency control, database design, and end-to-end automated testing.

---

## 🌟 Key Capabilities & Highlights

- 🔍 **Interactive Hotel Discovery**: Filter hotels by destination and stay dates with live room inventory calculation.
- ⚡ **Concurrency & Race Condition Protection**: Implements **Pessimistic Write Locking (`SELECT ... FOR UPDATE`)** inside transactional boundaries, guaranteeing zero double-bookings when multiple users book the last available room simultaneously.
- 🎁 **Surprise Feature – "Smart Stay Pricing & Demand Insights"**:
  - Automatically calculates a **10% Long-Stay Discount** on the backend for reservations of 4 or more nights.
  - Real-time demand scarcity badges: **"High Demand: Only 1 room left!"** (when 1 room remains), **"Sold Out"** (when capacity is 0), or available room counts.
- 📅 **Strict Temporal Guardrails**: UI and backend validation preventing past dates (`checkIn >= today`) and invalid checkout intervals (`checkOut > checkIn`).
- 🔐 **Privacy-First Booking Management & Cancellation**: Look up reservations case-insensitively by booking reference and email. Cancellations accept email in the encrypted JSON request body, immediately restoring room capacity to the booking pool.
- 🛡️ **Consistent Error Handling**: Global exception interceptor (`@RestControllerAdvice`) returning standardized JSON error payloads with field-level validation breakdowns.
- 🧪 **Comprehensive Automated Test Coverage**: 33 automated tests covering pricing rules, long-stay discounts, date validation failures, concurrency locking, and room capacity lifecycles.

---

## 🏗️ Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 23 (compatible with 17+), Spring Boot 3.3.5, Spring Data JPA, Hibernate, Jakarta Bean Validation, Lombok |
| **Frontend** | React 19, Vite 8, Tailwind CSS v4, React Router 7, Lucide React |
| **Database** | MySQL 8.0+ (InnoDB, UTF-8mb4), with idempotent self-seeding `DataInitializer` |
| **DevOps & Containers** | Docker, Docker Compose, Multi-Stage Container Builds, Nginx (Alpine reverse proxy) |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, MockMvc, AssertJ, H2 Database (in-memory MySQL mode) |

---

## 📋 Prerequisites

Before running the application locally, ensure you have installed:
1. **Java Development Kit (JDK 17 or higher)**:
   ```bash
   java -version
   ```
2. **Node.js (v18 or higher) & npm**:
   ```bash
   node -v
   npm -v
   ```
3. **MySQL Server 8.0+**: Running locally on port `3306`.

---

## 🐳 One-Click Docker Execution (Fastest & Recommended)

The entire full-stack ecosystem is fully containerized with Docker and Docker Compose. A single command provisions the MySQL 8.0 database, initializes the schema and seed data, compiles and packages the Spring Boot backend, and builds the React 19 frontend served via an Nginx reverse proxy.

```bash
docker compose up --build
```

### 🌐 Accessing the Running Services:
- **Frontend Portal**: [**`http://localhost:3000`**](http://localhost:3000) (also mapped to [**`http://localhost:5173`**](http://localhost:5173))
- **Backend API & Health**: [**`http://localhost:8080/`**](http://localhost:8080/)
- **Hotel Discovery API**: [**`http://localhost:8080/api/hotels`**](http://localhost:8080/api/hotels)
- **MySQL Database**: `localhost:3306` (Credentials: `root` / `root`, DB: `hotel_booking`)

To stop all services:
```bash
docker compose down
```
To purge the database volume and re-seed from scratch:
```bash
docker compose down -v
```

---

## 🚀 Running the Application Manually (Local Dev Mode)

### Step 1: Start the Spring Boot Backend
1. Ensure your local MySQL server is running on port `3306`.
2. Open a terminal and navigate to `/backend`:
   ```bash
   cd backend
   ```
3. Run the application using the included Maven Wrapper:
   - **Windows**:
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```
   - **macOS / Linux**:
     ```bash
     ./mvnw spring-boot:run
     ```
4. The backend will start on **`http://localhost:8080`**.
   > **Note**: Database creation (`hotel_booking`) and sample hotel data seeding are handled **100% automatically** on first startup via `createDatabaseIfNotExist=true` and `DataInitializer.java`. Zero manual SQL execution is needed.
5. Visiting `http://localhost:8080/` in your browser will display an API health index and available endpoints.

---

### Step 2: Start the React Frontend
1. Open a second terminal and navigate to `/frontend`:
   ```bash
   cd frontend
   ```
2. Install npm dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
4. Open **`http://localhost:5173`** in your browser.

---

### ⚙️ Optional: Custom Database Credentials & Raw SQL

By default, the backend connects automatically using standard local defaults (`root` / `root`) with `createDatabaseIfNotExist=true`. **Zero manual database configuration is needed.**

If your local MySQL installation uses a custom password or port, you can customize it via environment variables:

- **Windows (PowerShell)**:
  ```powershell
  $env:DB_PASSWORD="your_mysql_password"
  ```
- **macOS / Linux**:
  ```bash
  export DB_PASSWORD="your_mysql_password"
  ```

*(Optional)* The raw SQL scripts are also available for reference or inspection:
- DDL Schema: [`database/schema.sql`](file:///d:/hotel_booking/database/schema.sql)
- Sample Seed Data: [`database/seed.sql`](file:///d:/hotel_booking/database/seed.sql)

---

## 🧪 Running Automated Tests

The backend includes 33 unit, controller slice, and integration tests verifying pricing calculations, date validations, concurrency locks, and room availability lifecycles.

To execute all tests:
```bash
cd backend
./mvnw test        # macOS / Linux
.\mvnw.cmd test    # Windows
```

### Test Suite Summary:
- **`BookingLifecycleIntegrationTest`** (8 tests): End-to-end stay lifecycle testing 10% long-stay discounts, short-stay base pricing, date validation edge cases, capacity exhaustion, and cancellation freeing inventory.
- **`BookingServiceTest`** (7 tests): Mockito unit tests verifying booking creation, sold-out errors, case-insensitive lookups, and cancellation email mismatch guards.
- **`HotelServiceTest`** (7 tests): Unit tests verifying the Smart Stay 10% discount algorithm and demand status calculation (`HIGH_DEMAND`, `AVAILABLE`, `SOLD_OUT`).
- **`BookingControllerTest`** (7 tests): WebMvcTests validating HTTP status codes (201 Created, 400 Bad Request with `fieldErrors`, 409 Conflict) and JSON serialization.
- **`HotelControllerTest`** (2 tests): WebMvcTests validating hotel search and details endpoints.
- **`RepositoryConcurrencyTest`** (2 tests): JPA tests verifying `findByIdWithLock` (`SELECT ... FOR UPDATE`) and the overlapping date count JPQL query.

---

## 📊 Sample Test Data

The application comes pre-populated with 6 hotels across India with inventory sizes between 3 and 5 rooms:

| ID | Hotel Name | Location | Price / Night | Total Rooms | Highlights |
| :---: | :--- | :--- | :---: | :---: | :--- |
| **1** | The Grand Heritage Palace | Udaipur | ₹7,500 | 4 | Royal retreat on Lake Pichola, Infinity Pool, Royal Spa |
| **2** | Taj Vista Coastal Resort | Goa | ₹5,200 | 5 | Luxury beachfront resort, Ayurvedic Spa, Seafood Bistro |
| **3** | The Royal Amber Haveli | Jaipur | ₹4,500 | 3 | Authentic Rajasthani mansion in the Pink City |
| **4** | Snow Peak Alpine Retreat | Manali | ₹3,800 | 4 | Cedar sanctuary overlooking snow-capped Himalayan peaks |
| **5** | Marine Bay Boulevard Suites | Mumbai | ₹6,800 | 5 | Art deco luxury suites overlooking Marine Drive |
| **6** | TechCity Silicon Boutique Hotel | Bengaluru | ₹4,200 | 3 | Modern eco-luxe hotel in Indiranagar |

### Suggested Walkthrough Scenarios:
1. **Regular Stay (< 4 nights)**:
   - Select *Goa* (Taj Vista Coastal Resort) for 3 nights.
   - Price calculation: $3 \times ₹5,200 = ₹15,600$. Discount: ₹0.00.
2. **Smart Stay Long-Stay Discount (≥ 4 nights)**:
   - Select *Udaipur* (The Grand Heritage Palace) for 5 nights.
   - Base Price: $5 \times ₹7,500 = ₹37,500$.
   - Automatic 10% Long-Stay Discount: $-₹3,750$.
   - Total Payable Amount: **₹33,750**.
3. **Scarcity & Sold-Out Guard**:
   - The Royal Amber Haveli (Jaipur) has only 3 total rooms.
   - Booking 2 rooms triggers the `"High Demand: Only 1 room left!"` badge.
   - Booking the 3rd room marks the hotel `"Sold Out"` and blocks further reservations for those dates.
4. **Instant Capacity Release**:
   - Cancel any confirmed reservation from `/manage-booking`.
   - The room is immediately restored to availability for subsequent searches.

---

## ⚖️ Assessment Disclosures

- **Approximate Time Spent**: ~14 hours across 9 sequential architectural milestones.
- **Assistance Disclosed**: Built in collaboration with Google DeepMind's Antigravity AI agent, following strict incremental milestones, and code review verification at each phase.
- **Architecture Documentation**: For an in-depth review of concurrency locking, database constraints, overlapping stay mathematics, and trade-offs, please read [**`DESIGN.md`**](file:///d:/hotel_booking/DESIGN.md).
