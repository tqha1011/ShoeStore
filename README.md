
<div align="center">
  
# 👟 Shoe Store App
**A Modern Full-Stack E-Commerce Mobile Application**

*Capstone Project for SE114.Q22 - UIT-VNUHCM*

![.NET](https://img.shields.io/badge/.NET_10-512BD4?style=for-the-badge&logo=dotnet&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

</div>

---

## 📖 Brief Summary

**Shoe Store App** is a comprehensive mobile e-commerce solution designed to deliver a seamless and highly responsive shoe-shopping experience. 

Built with scalability and performance in mind, the project leverages a reactive and modern Android UI using **Kotlin Jetpack Compose**, backed by a robust, high-performance **ASP.NET Core Web API**. The architecture is heavily optimized and fortified with enterprise-level features, ensuring that whether you are an administrator managing the product catalog or a customer browsing the latest sneakers, the journey is fast, secure, and intuitive.

---

## ✨ Core Features & Technical Highlights

* 🛡️ **Advanced Security:** Implemented **JWT Authentication** and sophisticated **Rate Limiting** algorithms. We utilize *Token Bucket* for authenticated users and *Fixed Window* (partitioned by IP) for anonymous traffic to prevent DDoS and Brute-force attacks.
* ☁️ **Cloud Media Storage:** Integrated with **Cloudinary** for scalable, auto-optimized image hosting, avoiding heavy local server loads.
* 📱 **Modern UI & MVVM:** Frontend Android application strictly follows the **MVVM (Model-View-ViewModel)** architecture pattern combined with **Jetpack Compose**, ensuring a highly reactive, testable, and robust state-driven user interface.
* 🏗️ **Clean Architecture:** Backend structured with maintainability in mind, utilizing Result Pattern, FluentValidation, and robust Dependency Injection.
* 🔄 **Agile Workflow:** Project managed entirely via **ClickUp** using 2-week Sprints, Time Tracking, and strictly adhering to Conventional Commits.

---

## 👥 Authors & Contributors

| Name | GitHub Profile |
| :--- | :--- |
| **Trần Quang Hạ** | [@tqha1011](https://github.com/tqha1011) |
| **Phan Cao Minh Hiếu** | [@hieupcm03](https://github.com/hieupcm03) |
| **Lê Hữu Việt Hoàng** | [@Hoang44444](https://github.com/Hoang44444) |
| **Nguyễn Lê Hoàng Hảo** | [@hoanghaoz](https://github.com/hoanghaoz) |

---

## 🛠️ Tech Stack

**Frontend Mobile (Android)**
* Language: Kotlin
* UI Framework: Jetpack Compose
* Network: Retrofit
* Image Loading: Coil / Glide

**Backend API**
* Framework: ASP.NET Core Web API (.NET)
* ORM: Entity Framework Core
* Database: PostgreSQL
* API Documentation: Scalar / OpenAPI

**Infrastructure & Management**
* Containerization: Docker & Docker Compose
* Task Management: ClickUp
* Version Control: GitHub


## Deployment
### 1. 🐳 Deploy Backend with Docker (ASP.NET Core API)
If you don't have `Docker`, get it from [official website](https://www.docker.com/) to install `Docker`

Open your terminal and navigate to `Backend` folder

Run the below command:
```bash
  docker compose up -d --build
```
Navigate to `http://localhost:<port>/scalar/v1` to see the **API Docs**

### 2. Use deploy link service with Azure App Service
- We have deployed the backend API to Azure App Service, you can access it via this link:
[API Docs](https://deploy-service-h6acgba9dkc0gvcw.eastasia-01.azurewebsites.net/scalar/v1)

### 3. 📱 Deploy Frontend (Android App)
**Requirement:** Install [Android Studio](https://developer.android.com/studio) && `JDK 17`

#### Step 1: 
- Open **Android Studio** and open folder **Frontend**
- Wait for `gradle sync`

#### Step 2: Configure the API connection
- Open `app/src/main/res/values/strings.xml` to change the backend's IP Address
- If you run emulator, simply change the `localhost` to `http://10.0.2.2:<port>`

## Performance Testing
- We have implemented load testing for the backend API performance using **[k6](https://github.com/grafana/k6)**.
- The load test script is written in `JavaScript` and is located in the `Backend/test/ShoeStore.Tests/LoadTesting` folder.
- **Test Scenario:** Unlike a simple ping test, this script simulates a realistic e-commerce user funnel (User Journey). Multiple virtual users (VUs) will concurrently perform:
    1. Authenticating and acquiring JWT Tokens.
    2. Searching for products randomly.
    3. Viewing specific product details.
    4. Placing orders (Checkout) to heavily test the PostgreSQL database's transactional integrity and concurrency handling (Row-level locks, stock deduction).
### 🛠️ Configuration & Execution

By default, the script targets the local environment (`http://localhost:8080`).

**Testing against the Deployed Environment (Azure/Cloud):**
If you want to run the load test against the live server, you must open the `loadtest.js` file and replace all instances of `http://localhost:8080` with the actual deployed base URL (e.g., `https://deploy-service-h6acgba9dkc0gvcw.eastasia-01.azurewebsites.net`).

**Configure Test Parameters:**
- The script is designed to run for a total duration of 1m15s with a ramp-up, peak load and ramp-down phase. You can adjust the `stages` in the `options` object to simulate different load patterns (e.g., longer peak duration, higher user count, etc.).
- Do not change the data setup in the script (e.g., user credentials, product IDs,...) unless you have a specific reason to do so, as it is tailored to the existing database state.
``` javascript
export const options = {
  stages: [
    { duration: '15s', target: 50 },  // Up to 50 users in 15 seconds (warm-up)
    { duration: '45s', target: 300 }, // (peak load) Modify target as needed to simulate more users
    { duration: '15s', target: 0 },   // back-down: decrease to 0 users in 15 seconds (cool-down)
  ],
};
```
**Run the Load Test:**
- Navigate to `Backend/test/ShoeStore.Tests/LoadTesting` folder and run the below command to execute the load test. You must provide the testing password via environment variables (contact the repository owner if you don't have this)
```bash
  k6 run -e TEST_PASSWORD="contact_me_tranquangha2006@gmail.com" loadtest.js
```

## Documentation

[Backend Architecture](docs/architecture/backend-architecture.md)

[Frontend Architecture](docs/architecture/frontend-architecture.md)

[Database Design](docs/architecture/database-schema.md)

[CI/CD Pipeline Guidelines](docs/infrastructure/ci-cd.md)

[Conventional Commit Guidelines](docs/guildlines/conventional-commit.md)

[Support FE](docs/api-guildlines/checkout-flow.md)

[Test Coverage](docs/testing/unit-testing-strategy-test-matrix.md)

