# 🏛️ System Architecture

This project implements the **Clean Architecture** pattern, divided into 4 distinct layers:

1. **🧱 Domain Layer (`ShoeStore.Domain`)**
- Contains core entities and enums.
- Does not depend on any other layers.

2. **💼 Application Layer (`ShoeStore.Application`)**
- Depends only on the **Domain Layer**.
- Contains `Interfaces`, `Services`, and `DTOs`.
- Implements core business logic.

3. **🛠️ Infrastructure Layer (`ShoeStore.Infrastructure`)**
- Depends on the **Application** and **Domain** layers.
- Implements **Repository Interfaces**.
- Connects to SQL Server through **Entity Framework Core**.

4. **🌐 API Layer (`ShoeStore.Api`)**
- Depends on the **Infrastructure** and **Application** layers.
- Contains `Controllers` and `Middleware`.
- Registers Dependency Injection in `Program.cs`.
- Integrates API Docs (**Scalar**).

```text
.
└── 📂 ShoeStore.Backend/
    └── src/
        ├── ShoeStore.sln
        ├── 🐳 compose.yaml
        ├── 📂 ShoeStore.Api/
        │   ├── 📂 Properties/
        │   ├── 📂 Controller
        │   ├── 📂 Middleware
        │   ├── 🐳 Dockerfile
        │   ├── 📄 Program.cs
        │   └── ⚙️ appsetting.json
        ├── ShoeStore.Domain/
        │   ├── 📂 Entities
        │   └── 📂 Common
        ├── ShoeStore.Application/
        │   ├── 📂 DependencyInjection
        │   ├── 📂 Interface
        │   ├── 📂 DTOs
        │   ├── 📂 Services
        │   ├── 📂 Mapping
        │   └── 📂 Validations
        └── ShoeStore.Infrastructure/
            ├── 📂 Data/
            │   ├── 📂 Configurations
            │   └── 📄 AppDbContext.cs
            ├── 📂 Repositories
            ├── 📂 Migrations
            └── 📂 DependencyInjection/
                └── 📄 DependencyInjection.cs
```
