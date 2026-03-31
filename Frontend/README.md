# 👟 ShoeStore App - Nike Shoe Shopping Application

## 📱 Ứng Dụng Bán Giày Nike Trên Di Động

**ShoeStore** là một ứng dụng di động Android cho phép người dùng duyệt, tìm kiếm, và mua giày Nike trực tuyến.

### 🏗️ Built With
- **Kotlin** - Ngôn ngữ lập trình
- **Jetpack Compose** - Modern UI framework
- **MVVM Architecture** - Kiến trúc ứng dụng
- **Android Jetpack** - Essential libraries
- **Coroutines & Flow** - Async programming

---

## 🎯 Màn Hình Chính (Completed)

### ✅ Authentication Screens
- Welcome Screen
- Sign In / Sign Up
- Password Recovery

### ✅ Product Screens
- **Product List Screen** - Danh sách sản phẩm
- **Product Detail Screen** - Chi tiết sản phẩm (JUST ADDED)

### 🔄 Coming Soon
- Cart Screen
- Favorites Screen
- Checkout Screen
- User Profile

---

## 🚀 Quick Start

### **1. Requirements**
- Android Studio Arctic Fox hoặc newer
- JDK 11+
- Android SDK 29+
- Emulator hoặc physical device

### **2. Clone & Open**
```bash
git clone <repository-url>
cd ShoeStore/Frontend
# Mở bằng Android Studio
```

### **3. Run**
```bash
# Build project
./gradlew build

# Run trên emulator
./gradlew installDebug
```

### **4. Test ProductDetailScreen**
```kotlin
// MainActivity.kt
startDestination = "product_list"  // Xem danh sách
// Click vào ProductCard → Navigate to ProductDetailScreen
```

---

## 📂 Project Structure

```
Frontend/
├── app/
│   ├── src/
│   │   └── main/
│   │       └── java/com/example/shoestoreapp/
│   │           ├── features/
│   │           │   ├── auth/           # Authentication
│   │           │   ├── product/        # Product feature
│   │           │   │   ├── data/       # Data layer
│   │           │   │   ├── ui/         # UI layer
│   │           │   │   └── viewmodel/  # Logic layer
│   │           │   └── cart/           # Cart feature (TODO)
│   │           └── MainActivity.kt     # Entry point
│   └── build.gradle.kts
│
├── QUICK_START.md                      # Bắt đầu nhanh
├── DOCUMENTATION_INDEX.md              # Index tài liệu
├── PRODUCT_DETAIL_GUIDE.md             # ProductDetailScreen guide
├── COMPONENTS_DETAIL.md                # Component documentation
├── FILES_EXPLANATION.md                # File organization
├── DESIGN_TO_CODE.md                   # Design mapping
├── PROJECT_SUMMARY.md                  # Project overview
└── COMPLETION_SUMMARY.md               # Hoàn thành summary
```

---

## 📖 Documentation

Bạn có thể tìm tài liệu chi tiết tại các file dưới đây:

| Document | Mục Đích | Đọc |
|----------|---------|-----|
| **QUICK_START.md** | Bắt đầu nhanh | 5 min ⚡ |
| **DOCUMENTATION_INDEX.md** | Navigation tài liệu | 2 min 📚 |
| **PRODUCT_DETAIL_GUIDE.md** | Chi tiết ProductDetailScreen | 15 min 📖 |
| **COMPONENTS_DETAIL.md** | Từng component | 20 min 🧩 |
| **DESIGN_TO_CODE.md** | Design vs Code | 10 min 🎨 |
| **PROJECT_SUMMARY.md** | Overview project | 5 min 📊 |

👉 **Bắt đầu bằng**: QUICK_START.md

---

## ✨ Tính Năng ProductDetailScreen

### 🖼️ Display
- [x] Ảnh sản phẩm full-width
- [x] Thông tin tên, giá, đánh giá
- [x] Loại sản phẩm
- [x] Số lượng reviews

### 🛍️ Interaction
- [x] Chọn kích thước (7-11)
- [x] Thêm vào giỏ hàng
- [x] Đánh dấu yêu thích
- [x] Mở rộng thông tin

### 📱 Navigation
- [x] Quay lại danh sách
- [x] Xem shopping bag
- [x] Menu navigation

### 💾 State Management
- [x] Product loading
- [x] Size selection
- [x] Favorite toggle
- [x] Loading states

---

## 🔧 Technology Stack

### UI Framework
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material Design 3** - Material components

### Architecture
- **MVVM** - Model-View-ViewModel pattern
- **Coroutines** - Async programming
- **Flow/StateFlow** - Reactive streams

### Libraries
- **Coil** - Image loading
- **Jetpack Navigation** - Navigation components
- **Jetpack Lifecycle** - Lifecycle awareness

---

## 🎨 Design System

### Colors
- **Primary**: Black (#000000)
- **Secondary**: White (#FFFFFF)
- **Accent**: Gray (#808080)

### Typography
- **Headlines**: 28sp Bold
- **Body**: 14sp Regular
- **Labels**: 12sp Bold

### Spacing
- **Small**: 8dp
- **Medium**: 16dp
- **Large**: 24dp
- **XL**: 32dp

---

## 🏛️ Architecture Overview

### MVVM Pattern
```
View (UI)                          
  ↓ (collect state)
ViewModel (Logic)
  ↓ (get/update data)
Repository (Data)
  ↓ (mock data / API)
Model (Data class)
```

### Data Flow
```
User Interaction
  ↓
Composable Callback
  ↓
ViewModel Function
  ↓
Update State (Flow)
  ↓
UI Recompose (collectAsState)
```

---

## 📋 Development Checklist

### Setup
- [x] Android Studio configured
- [x] Gradle synced
- [x] Project compiles
- [x] Emulator ready

### Development
- [x] ProductDetailScreen created
- [x] All components implemented
- [x] ViewModel logic added
- [x] Navigation setup
- [x] Tests passing

### Documentation
- [x] Quick start guide
- [x] Architecture guide
- [x] Component documentation
- [x] Code examples
- [x] Troubleshooting guide

---

## 🐛 Troubleshooting

### App Crash
```bash
# Check logcat
adb logcat | grep ERROR

# Common causes:
# 1. ViewModel not initialized
# 2. State not collected
# 3. Callback not defined
# 4. Navigation route not setup
```

### Build Error
```bash
# Clean & rebuild
./gradlew clean
./gradlew build

# Sync gradle files
./gradlew --refresh-dependencies
```

### UI Not Showing
```kotlin
// Check in ProductDetailScreen:
if (productDetail == null) {
    println("Product not loaded!")
}
if (isLoading) {
    println("Still loading...")
}
```

Untuk bantuan lebih lanjut, lihat **QUICK_START.md** → Troubleshooting section.

---

## 📞 Support

### Dokumentasi
Lihat folder root untuk file `.md`:
- 📖 QUICK_START.md - Mulai cepat
- 📖 DOCUMENTATION_INDEX.md - Index semua docs
- 📖 Lainnya lihat DOCUMENTATION_INDEX.md

### Resources
- [Android Developers](https://developer.android.com)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [MVVM Architecture](https://developer.android.com/topic/architecture)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)

---

## 📈 Project Status

### Completed ✅
- [x] ProductDetailScreen
- [x] 5 UI Components
- [x] ViewModel with logic
- [x] Navigation setup
- [x] Comprehensive documentation

### In Progress 🔄
- [ ] Cart Screen
- [ ] Favorites Screen
- [ ] API Integration

### Planned 🔮
- [ ] Checkout Flow
- [ ] User Authentication
- [ ] Order History
- [ ] Notifications

---

## 👨‍💻 Contributing

### Code Style
- Follow Kotlin conventions
- Use meaningful variable names
- Add comments for complex logic
- Format code properly

### Adding Features
1. Create branch: `git checkout -b feature/your-feature`
2. Implement feature (follow MVVM)
3. Add tests
4. Create pull request

### Documentation
Setiap fitur harus didokumentasikan:
- Code comments
- Function documentation
- Usage examples

---

## 📄 License

ShoeStore App - Educational Project

---

## 🎓 Learning Resources

### Untuk Pemula
1. QUICK_START.md (5 min)
2. PRODUCT_DETAIL_GUIDE.md (15 min)
3. Coba ubah 1 color (5 min)

### Untuk Intermediate
1. COMPONENTS_DETAIL.md (20 min)
2. FILES_EXPLANATION.md (10 min)
3. Coba tambah 1 component

### Untuk Advanced
1. DESIGN_TO_CODE.md (15 min)
2. Architecture review (20 min)
3. Implementasi feature baru

---

## 🎉 Getting Started

```
1. Clone project
   git clone <url>

2. Open in Android Studio
   File → Open → ShoeStore/Frontend

3. Sync Gradle
   Wait for sync to complete

4. Read QUICK_START.md
   5 minutes reading

5. Run on emulator
   Click Green Play button

6. Test ProductDetailScreen
   Click ProductCard → See detail screen

7. Read more documentation
   Start with DOCUMENTATION_INDEX.md

8. Start coding!
   Happy coding! 💻✨
```

---

## 📊 Statistics

| Metric | Value |
|--------|-------|
| Total Code Files | 9 |
| Lines of Code | ~1,000+ |
| Components | 5 |
| Screens | 2 |
| Documentation Files | 8 |
| Documentation Pages | ~25 |
| Code Examples | 50+ |

---

## 🙏 Credits

Dibuat dengan ❤️ untuk:
- **Peserta**: THIEU DOAN
- **Course**: Nhập Môn Ứng Dụng Di Động
- **Assignment**: Quản Lý Cửa Hàng Bán Giày

---

## 📝 Version History

### v1.0 (March 25, 2026)
- ✅ ProductDetailScreen complete
- ✅ 5 UI components
- ✅ Full documentation
- ✅ Build successful

---

## 🚀 Next Milestone

**Cart Screen Implementation**
- Target: Tuần depan
- Files: CartScreen.kt, CartViewModel.kt, CartItem component
- Features: View cart items, adjust quantity, checkout

---

**Let's build amazing things! 🚀💻✨**

---

📍 **Project Root**: `Frontend/`
📍 **Documentation**: See files in root directory
📍 **Build**: `./gradlew build`
📍 **Run**: Green Play button in Android Studio
📍 **Status**: ✅ READY TO DEVELOP

**Happy Coding! 🎉**

