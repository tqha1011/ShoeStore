
# Shoe Store App

**A project for SE114.Q22 - UIT-VNUHCM**


![.NET Core](https://img.shields.io/badge/.NET%20Core-512BD4?style=for-the-badge&logo=dotnet&logoColor=white)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

![SQL Server](https://img.shields.io/badge/SQL%20Server-CC2927?style=for-the-badge&logo=microsoft-sql-server&logoColor=white)

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

## 👥 Authors

- [Trần Quang Hạ](https://github.com/tqha1011)
- [Phan Cao Minh Hiếu](https://github.com/hieupcm03)
- [Lê Hữu Việt Hoàng](https://github.com/Hoang44444)
- [Nguyễn Lê Hoàng Hảo](https://github.com/hoanghaoz)


## 🛠️ Tech Stack

**Frontend:** Java, Kotlin

**Backend:** ASP.NET Core WebApi

**Database:** SQL Server


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

[Backend Architecture](docs/backend-architecture.md)
[Frontend Architecture](docs/frontend-architecture.md)
[CI/CD Pipeline Guildlines](docs/infrastructure/ci-cd.md)

