
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
### 1. 🐳 Deploy Backend (ASP.NET Core API)
If you don't have `Docker`, get it from [official website](https://www.docker.com/) to install `Docker`

Open your terminal and navigate to `Backend` folder

Run the below command:
```bash
  docker compose up -d --build
```
Navigate to `http://localhost:<port>/scalar/v1` to see the **API Docs**

### 2. 📱 Deploy Frontend (Android App)
**Requirement:** Install [Android Studio](https://developer.android.com/studio) && `JDK 17`

#### Step 1: 
- Open **Android Studio** and open folder **Frontend**
- Wait for `gradle sync`

#### Step 2: Configure the API connection
- Open `app/src/main/res/values/strings.xml` to change the backend's IP Address
- If you run emulator, simply change the `localhost` to `http://10.0.2.2:<port>`



## Documentation

[Backend Architecture](docs/architecture/backend-architecture.md)

[Frontend Architecture](docs/architecture/frontend-architecture.md)

[Database Design](docs/architecture/database-schema.md)

[CI/CD Pipeline Guidelines](docs/infrastructure/ci-cd.md)

[Conventional Commit Guidelines](docs/guildlines/conventional-commit.md)

