# 📱 Frontend Architecture

The Android application follows a **Feature-Based MVVM (Model-View-ViewModel)** architecture. This approach groups code by features rather than by technical layers, ensuring high scalability and maintainability.

## 🏗️ Core Principles
Each feature acts as a mini-module containing its own MVVM layers:
- **📊 Model (Data):** API calls, local databases, and data models specific to the feature.
- **🧠 ViewModel:** Handles business logic, state management, and interacts with the Model.
- **🎨 View (UI):** Activities, Fragments, and UI components (XML/Jetpack Compose) that observe the ViewModel.

## 📂 Project Structure Example
```text
📱 app/src/main/java/com/shoestore/
├── 📦 core/                # Global utilities, base classes, network config (Retrofit)
├── 🚀 features/            # Feature modules
│   ├── 🔑 auth/            # Authentication Feature
│   │   ├── 📊 data/        # AuthRepository, LoginRequest, UserDTO
│   │   ├── 🧠 viewmodel/   # AuthViewModel
│   │   └── 🎨 ui/          # LoginActivity, RegisterFragment
│   ├── 👟 products/        # Product Display Feature
│   │   ├── 📊 data/
│   │   ├── 🧠 viewmodel/
│   │   └── 🎨 ui/          # ProductListFragment, ProductDetailFragment
│   └── 🛒 cart/            # Shopping Cart Feature
└── 📄 MainActivity.java    # Entry point of the application
```
