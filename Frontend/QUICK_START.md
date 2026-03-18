# 🚀 Quick Start - Hướng Dẫn Bắt Đầu Nhanh

## ⚡ 5 Phút Đầu Tiên

### **1. Mở Android Studio**
```
File → Open → ShoeStore/Frontend
Đợi Gradle sync hoàn thành
```

### **2. Test ProductDetailScreen**
```kotlin
// Mở file: MainActivity.kt
// Thay đổi startDestination:

// TRƯỚC:
startDestination = "welcome"

// SAU:
startDestination = "product_list"
```

### **3. Run App**
```
Chọn emulator hoặc device
Click Green Play button (▶️)
Đợi app build & install
```

### **4. Test Navigation**
```
1. Trên màn hình danh sách sản phẩm
2. Click vào một sản phẩm (ProductCard)
3. Verify: Điều hướng sang ProductDetailScreen
4. Thử chọn size, click Favorite, Add to Bag
```

---

## 📂 File Cần Biết

### **ViewModel - Quản Lý Logic**
📍 `features/product/viewmodel/ProductDetailViewModel.kt`
- Quản lý state (productDetail, selectedSize, isLoading)
- Xử lý logic (load, select, toggle, add)

### **Màn Hình Chính**
📍 `features/product/ui/product_detail/ProductDetailScreen.kt`
- Kết hợp tất cả components
- Collect state từ ViewModel
- Handle callbacks

### **Components**
📍 `features/product/ui/components/`
- `ProductHeroImage.kt` - Ảnh sản phẩm
- `ProductHeaderInfo.kt` - Tên, giá, rating
- `SizeSelector.kt` - Chọn size
- `ActionButtonsSection.kt` - Nút Add/Favorite
- `ExpandableSection.kt` - Phần mở rộng

### **Navigation**
📍 `MainActivity.kt`
- Route cho ProductDetailScreen
- Callback handling

---

## 🔍 Cách Debug

### **Lỗi Compile**
```bash
./gradlew compileDebugKotlin
# Xem error ở console
# Sửa lỗi → compile lại
```

### **Lỗi Runtime**
```bash
# Xem Logcat (Android Studio dưới cùng)
# Tìm Exception hoặc Error
# Debug từ đó
```

### **Lỗi UI Tidak Hiển Thị**
```kotlin
// Kiểm tra trong ProductDetailScreen:
if (productDetail == null) {
    println("Product not loaded!")
}
if (isLoading) {
    println("Still loading...")
}

// Kiểm tra ViewModel có nhận productId không
viewModel.loadProductDetail(productId)
```

---

## ✅ Checklist Test

- [ ] App start thành công
- [ ] ProductListScreen hiển thị danh sách
- [ ] Click vào ProductCard
- [ ] ProductDetailScreen load đúng sản phẩm
- [ ] Ảnh sản phẩm hiển thị
- [ ] Tên, giá, rating hiển thị đúng
- [ ] Chọn size → button highlight
- [ ] Click Favorite → icon đổi màu
- [ ] Click "Add to Bag" → callback gọi
- [ ] Click back → quay lại ProductListScreen

---

## 🎯 Tasks Tiếp Theo

### **Này Làm Tiếp**
1. [ ] Kết nối API thực (thay mock data)
2. [ ] Tạo Cart Screen
3. [ ] Tạo Favorites Screen
4. [ ] User Profile
5. [ ] Checkout & Payment

### **Code Quality**
1. [ ] Add unit tests
2. [ ] Add integration tests
3. [ ] Error handling
4. [ ] Add animations
5. [ ] Performance optimization

---

## 💡 Tips Hữu Ích

### **Hot Reload**
```
Ctrl+Shift+Backspace (sau khi sửa code)
hoặc Click Recompose button trong Preview
```

### **Preview UI Nhanh**
```kotlin
@Preview(showBackground = true, heightDp = 800)
@Composable
fun PreviewProductDetailScreen() {
    ProductDetailScreen(productId = 1)
}

// Nhấn Preview tab trong Android Studio
```

### **Logcat Filtering**
```
Logcat → Filter → Package Name
hoặc
Logcat → Filter regex: "ProductDetail"
```

### **Breakpoint Debug**
```
1. Click vào dòng code
2. Set breakpoint (F9)
3. Run app
4. Khi hit breakpoint, pause & inspect
```

---

## 📞 Troubleshooting

### **Error: "Unresolved reference"**
```
Solution: Click "Sync Now" ở top bar
hoặc File → Sync Project with Gradle Files
```

### **Error: "Gradle build failed"**
```
Solution: 
./gradlew clean
./gradlew build
```

### **App Crash khi click vào ProductCard**
```
Check: 
1. Navigation route được define không? (MainActivity)
2. ProductDetailViewModel được tạo không?
3. productId được truyền đúng không?
```

### **ProductDetailScreen không load dữ liệu**
```
Check:
1. LaunchedEffect có chạy không? (Thêm println)
2. viewModel.loadProductDetail() được gọi không?
3. productDetail state có update không?
```

---

## 📱 Emulator Setup

### **Tạo Virtual Device Nếu Chưa Có**
```
Android Studio → Device Manager → Create Device
Chọn: Pixel 7 (1440x3120)
System Image: Android 14
Hoàn thành
```

### **Run App Trên Emulator**
```
1. Start emulator (Device Manager → Play)
2. Android Studio: Select device
3. Click Run (Shift+F10)
4. Chọn emulator
5. Click OK
```

---

## 🎨 Customize Design

### **Thay Đổi Màu Sắc**
```kotlin
// Tìm trong component files:
Color.Black → Color(0xFFXXXXXX)

// Ví dụ:
Text(color = Color.Black)  // Thay đổi ở đây
```

### **Thay Đổi Font Size**
```kotlin
Text(fontSize = 28.sp)  // Thay đổi 28 thành giá trị khác
```

### **Thay Đổi Padding/Margin**
```kotlin
modifier = Modifier.padding(horizontal = 24.dp)  // Thay 24 thành giá trị khác
```

---

## 📚 Tài Liệu Thêm

Xem các file documentation:
- 📄 `PRODUCT_DETAIL_GUIDE.md` - Hướng dẫn chi tiết ProductDetailScreen
- 📄 `COMPONENTS_DETAIL.md` - Chi tiết từng component
- 📄 `PROJECT_SUMMARY.md` - Tóm tắt toàn project

---

## ✨ Bạn Đã Hoàn Thành!

Congratulations! 🎉 Bạn vừa:
- ✅ Tạo ProductDetailViewModel với full logic
- ✅ Tạo 5 components UI
- ✅ Tạo ProductDetailScreen hoàn chỉnh
- ✅ Kết nối navigation
- ✅ Test compile thành công

**Tiếp theo**: Implement Cart Screen hoặc kết nối API thực!

---

**Happy Coding! 💻**

