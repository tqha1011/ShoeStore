# 📦 ShoeStore App - Tóm Tắt Tổng Quát

## 🎯 Project Overview

**ShoeStore App** là ứng dụng mobile bán giày Nike, được xây dựng trên:
- **Kiến Trúc**: MVVM (Model-View-ViewModel)
- **Ngôn Ngữ**: Kotlin + Jetpack Compose
- **Framework**: Android Jetpack + Coroutines

---

## 📱 Màn Hình Đã Hoàn Thành

### ✅ **1. Auth Screens** (Xác Thực)
- Welcome Screen
- Sign In Screen
- Sign Up Screen
- Forgot Password Screen
- Create New Password Screen

### ✅ **2. Product List Screen** (Danh Sách Sản Phẩm)
- Hiển thị danh sách sản phẩm dưới dạng grid
- Search bar tìm kiếm
- Filter chips (All, Air Max, Dunk, Pegasus, Jordan)
- Product cards với ảnh, tên, giá, favorite button
- Bottom navigation bar

### ✅ **3. Product Detail Screen** (Chi Tiết Sản Phẩm) - VỪA TẠOML
- Ảnh sản phẩm full-width
- Thông tin tên, giá, rating, reviews
- Chọn kích thước
- Nút "Add to Bag" và "Favorite"
- Expandable sections (Shipping, Description)
- Bottom navigation bar

---

## 🗂️ Cấu Trúc Thư Mục Toàn Project

```
com/example/shoestoreapp/
│
├── core/                          # Shared utilities
│
├── features/                      # Các tính năng chính
│   │
│   ├── auth/                      # Feature xác thực
│   │   ├── ui/
│   │   │   ├── welcome/
│   │   │   ├── sign_in/
│   │   │   ├── sign_up/
│   │   │   ├── reset_password/
│   │   │   └── components/
│   │   └── viewmodel/
│   │
│   ├── product/                   # Feature sản phẩm (VỪA LÀM)
│   │   ├── data/
│   │   │   ├── models/
│   │   │   │   └── Product.kt      # Data model
│   │   │   └── repositories/
│   │   │       └── ProductRepository.kt
│   │   │
│   │   ├── ui/
│   │   │   ├── components/
│   │   │   │   ├── ProductCard.kt
│   │   │   │   ├── ProductHeroImage.kt        # NEW
│   │   │   │   ├── ProductHeaderInfo.kt       # NEW
│   │   │   │   ├── SizeSelector.kt            # NEW
│   │   │   │   ├── ActionButtonsSection.kt    # NEW
│   │   │   │   ├── ExpandableSection.kt       # NEW
│   │   │   │   ├── SearchBar.kt
│   │   │   │   ├── FilterChips.kt
│   │   │   │   ├── TopAppBar.kt
│   │   │   │   └── BottomNavBar.kt
│   │   │   │
│   │   │   ├── product_list/
│   │   │   │   └── ProductListScreen.kt       # UPDATED
│   │   │   │
│   │   │   └── product_detail/
│   │   │       └── ProductDetailScreen.kt     # NEW
│   │   │
│   │   └── viewmodel/
│   │       ├── ProductListViewModel.kt
│   │       └── ProductDetailViewModel.kt      # NEW
│   │
│   └── cart/                      # Feature giỏ hàng (TODO)
│
└── MainActivity.kt                # Entry point app (UPDATED)
```

---

## 📊 MVVM Architecture Pattern

### **Layer 1: Presentation (UI)**
```
ProductDetailScreen.kt
├── State: productDetail, selectedSize, isLoading
├── Compose: Render UI components
└── Callback: onBackClick, onAddToCartClick, onFavoriteClick
```

### **Layer 2: ViewModel**
```
ProductDetailViewModel.kt
├── State Management: MutableStateFlow
├── Business Logic: loadProductDetail, selectSize, addToCart
└── Connect UI ↔ Data
```

### **Layer 3: Data (Repository)**
```
ProductRepository.kt
├── Mock Data: List<Product>
├── Data Operations: getAllProducts, toggleFavorite
└── Future: Connect to API/Database
```

### **Layer 4: Model**
```
Product.kt
└── Data class: id, name, price, rating, etc.
```

---

## 🔄 Luồng Dữ Liệu

### **Hiển Thị Danh Sách Sản Phẩm**
```
MainActivity
    ↓
ProductListScreen (Collect: productList from ViewModel)
    ↓
ProductListViewModel (Get: productList from Repository)
    ↓
ProductRepository (Return: mockProducts)
    ↓
UI: Render ProductCard × n
```

### **Click Vào Sản Phẩm**
```
User Click ProductCard
    ↓
onProductClick(productId) callback
    ↓
navController.navigate("product_detail/$productId")
    ↓
MainActivity route handler
    ↓
ProductDetailScreen(productId = 1)
    ↓
LaunchedEffect: loadProductDetail(1)
    ↓
ViewModel + Repository: Lấy data
    ↓
UI: Render ProductDetailScreen
```

### **Thêm Vào Yêu Thích**
```
User Click ❤️ Button
    ↓
onFavoriteClick(productId) callback
    ↓
viewModel.toggleFavorite(productId)
    ↓
productRepository.toggleFavorite(productId)
    ↓
_productList được update
    ↓
UI: Icon ❤️ đổi màu
```

---

## 🔧 Technology Stack

| Layer | Tech |
|-------|------|
| UI | Jetpack Compose |
| State Management | Flow, StateFlow, remember |
| Async | Coroutines, viewModelScope |
| Navigation | Jetpack Navigation Compose |
| Networking | Coil (image loading) |
| Architecture | MVVM |

---

## 📝 File Mới Được Tạo

### **ViewModel**
- ✅ `ProductDetailViewModel.kt` - Quản lý logic chi tiết sản phẩm

### **UI Components**
- ✅ `ProductHeroImage.kt` - Ảnh sản phẩm
- ✅ `ProductHeaderInfo.kt` - Thông tin tên/giá/rating
- ✅ `SizeSelector.kt` - Chọn kích thước
- ✅ `ActionButtonsSection.kt` - Nút Add/Favorite
- ✅ `ExpandableSection.kt` - Phần mở rộng

### **Screen**
- ✅ `ProductDetailScreen.kt` - Màn hình chi tiết

### **Documentation**
- ✅ `PRODUCT_DETAIL_GUIDE.md` - Hướng dẫn chi tiết
- ✅ `COMPONENTS_DETAIL.md` - Chi tiết từng component

---

## 🚀 Cách Chạy Ứng Dụng

### **1. Chuẩn Bị**
```bash
# Clone project
git clone <repo-url>
cd ShoeStore/Frontend

# Build gradle
./gradlew build
```

### **2. Chạy Trên Emulator**
```bash
# Start Android Emulator trước
# Sau đó chạy app
./gradlew installDebug
adb shell am start -n com.example.shoestoreapp/.MainActivity
```

### **3. View Preview Trong Android Studio**
- Mở file ProductDetailScreen.kt
- Click "Preview" tab
- Hoặc Ctrl+Shift+P

---

## 📋 Checklist Hoàn Thành

### **ProductDetailScreen**
- [x] Layout cơ bản
- [x] TopAppBar (back, logo, shopping bag)
- [x] ProductHeroImage (ảnh sản phẩm)
- [x] ProductHeaderInfo (tên, giá, rating)
- [x] SizeSelector (chọn kích thước)
- [x] ActionButtonsSection (Add to Bag, Favorite)
- [x] ExpandableSection (Shipping, Description)
- [x] Bottom Navigation Bar
- [x] Navigation route
- [x] State management (ViewModel)
- [x] Callback handling

### **ViewModel**
- [x] Load product detail
- [x] Select size
- [x] Toggle favorite
- [x] Add to cart
- [x] Reset state
- [x] Loading state

### **Components**
- [x] Tất cả components hoạt động đúng
- [x] Styling theo design
- [x] Callback được gọi đúng

---

## 🎨 Design Notes

### **Color Scheme**
- Primary: Black (#000000)
- Background: White (#FFFFFF)
- Text Secondary: Gray (#808080)
- Border: Light Gray (#E0E0E0)

### **Typography**
- Header: 28sp, Bold, Black
- Body: 14sp, Regular, Gray
- Button: 13sp, Bold, All-caps

### **Layout**
- Max width: 720px (mobile)
- Padding: 16dp, 24dp (vary by section)
- Border radius: 8dp, 12dp

---

## 🔮 Bước Tiếp Theo

### **Features Cần Làm**
1. **Cart Screen** - Hiển thị giỏ hàng
2. **Favorites Screen** - Hiển thị sản phẩm yêu thích
3. **API Integration** - Kết nối backend thực
4. **User Profile** - Thông tin user
5. **Checkout** - Thanh toán

### **Improvements**
1. Add animations (Transition, Scale, Fade)
2. Add error handling (Try-catch, Toast)
3. Add loading states
4. Add image caching
5. Add unit tests
6. Add integration tests

---

## 👥 Phân Công Team

| Thành Viên | Trách Nhiệm |
|-----------|------------|
| Frontend | UI/UX (Compose) |
| Backend | API, Database |
| QA | Testing, Bug fix |

---

## 📞 Support

Nếu có lỗi hoặc câu hỏi:
1. Kiểm tra file `PRODUCT_DETAIL_GUIDE.md`
2. Kiểm tra file `COMPONENTS_DETAIL.md`
3. Chạy `./gradlew compileDebugKotlin` để check lỗi Kotlin
4. Xem logcat để debug

---

## 📚 Tài Liệu Tham Khảo

- [Jetpack Compose Docs](https://developer.android.com/develop/ui/compose)
- [MVVM Architecture](https://developer.android.com/topic/architecture)
- [Coroutines & Flow](https://kotlinlang.org/docs/flow.html)
- [Coil Image Loading](https://coil-kt.github.io/coil/compose/)

---

**Last Updated**: March 25, 2026
**Status**: ✅ ProductDetailScreen Complete
**Next Task**: Cart Screen Implementation

