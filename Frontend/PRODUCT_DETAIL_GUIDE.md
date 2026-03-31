# 📱 ProductDetailScreen - Hướng Dẫn Chi Tiết

## 📋 Tổng Quan
Màn hình **ProductDetailScreen** hiển thị thông tin chi tiết của một sản phẩm khi user click vào ProductCard trên danh sách sản phẩm.

### Cấu Trúc MVVM
```
View (UI)                    ViewModel                   Data
ProductDetailScreen   <-->  ProductDetailViewModel  <-->  ProductRepository
    (UI Composables)        (Logic & State)              (Mock Data)
```

---

## 🏗️ Cấu Trúc Thư Mục

```
features/product/
├── data/
│   ├── models/
│   │   └── Product.kt              # Data model (dữ liệu)
│   └── repositories/
│       └── ProductRepository.kt    # Repository (quản lý dữ liệu)
│
├── ui/
│   ├── components/
│   │   ├── ProductHeroImage.kt       # Component ảnh sản phẩm
│   │   ├── ProductHeaderInfo.kt      # Component thông tin tên/giá/rating
│   │   ├── SizeSelector.kt           # Component chọn kích thước
│   │   ├── ActionButtonsSection.kt   # Component nút Add/Favorite
│   │   ├── ExpandableSection.kt      # Component phần mở rộng
│   │   └── ProductCard.kt            # Component card sản phẩm
│   │
│   ├── product_detail/
│   │   └── ProductDetailScreen.kt    # Màn hình chi tiết (màn hình chính)
│   │
│   └── product_list/
│       └── ProductListScreen.kt      # Màn hình danh sách sản phẩm
│
└── viewmodel/
    ├── ProductDetailViewModel.kt     # ViewModel chi tiết
    └── ProductListViewModel.kt       # ViewModel danh sách
```

---

## 🎯 File Chính Và Chức Năng

### 1️⃣ **ProductDetailViewModel.kt** - Quản Lý Logic & State
**Chức năng**: Trung gian giữa UI và Data, quản lý tất cả state của màn hình chi tiết

**State được quản lý**:
```kotlin
// Thông tin sản phẩm hiện tại
private val _productDetail = MutableStateFlow<Product?>(null)
val productDetail = _productDetail.asStateFlow()

// Size được chọn
private val _selectedSize = MutableStateFlow<Int?>(null)
val selectedSize = _selectedSize.asStateFlow()

// Trạng thái loading
private val _isLoading = MutableStateFlow(false)
val isLoading = _isLoading.asStateFlow()
```

**Các Hàm Chính**:
- `loadProductDetail(productId)` - Tải thông tin sản phẩm
- `selectSize(size)` - Lưu size được chọn
- `toggleFavorite(productId)` - Đánh dấu yêu thích
- `addToCart(productId)` - Thêm vào giỏ hàng
- `resetState()` - Reset toàn bộ state khi quay lại

---

### 2️⃣ **ProductDetailScreen.kt** - Giao Diện Chính
**Chức năng**: Render toàn bộ UI màn hình chi tiết sản phẩm

**Layout Chính**:
```
┌─────────────────────────────────┐
│ TopAppBar                       │  <- Nút back, logo, shopping bag
├─────────────────────────────────┤
│ ProductHeroImage                │  <- Ảnh sản phẩm (1:1 aspect ratio)
├─────────────────────────────────┤
│ ProductHeaderInfo               │  <- Tên, giá, rating
├─────────────────────────────────┤
│ SizeSelector                    │  <- Chọn kích thước
├─────────────────────────────────┤
│ ActionButtonsSection            │  <- "Add to Bag" + "Favorite"
├─────────────────────────────────┤
│ ExpandableSection (Shipping)    │  <- Thông tin vận chuyển
│ ExpandableSection (Description) │  <- Mô tả sản phẩm
└─────────────────────────────────┘
```

---

### 3️⃣ **Components - Các Component Riêng Lẻ**

#### **ProductHeroImage.kt**
- Hiển thị ảnh sản phẩm full-width
- Tỷ lệ 1:1 (square)
- Hỗ trợ placeholder khi load

#### **ProductHeaderInfo.kt**
- Hiển thị tên sản phẩm (Black, 28sp, Bold)
- Giá tiền ở góc phải
- Rating + số review
- Loại sản phẩm (Men's Shoes, etc.)

#### **SizeSelector.kt**
- Grid size buttons (7, 8, 9, 10, 11)
- Highlight size được chọn (màu đen)
- Nút "Size Guide"

#### **ActionButtonsSection.kt**
- Nút "ADD TO BAG" (đen, full-width)
- Nút "FAVORITE" (border, với icon trái tim)
- Callback khi click

#### **ExpandableSection.kt**
- Click để mở rộng/thu gọn nội dung
- Icon expand/collapse xoay 180°
- State quản lý bằng `remember { mutableStateOf() }`

---

## 🔄 Luồng Dữ Liệu & Điều Khiển

### **Khi User Click vào ProductCard**:
```
ProductListScreen
  ↓
onProductClick callback gọi: navController.navigate("product_detail/$productId")
  ↓
ProductDetailScreen khởi tạo với productId
  ↓
LaunchedEffect kích hoạt → gọi viewModel.loadProductDetail(productId)
  ↓
ViewModel tải dữ liệu từ Repository
  ↓
_productDetail được cập nhật → UI re-compose
```

### **Khi User Chọn Size & Click "Add to Bag"**:
```
User click size button
  ↓
onSizeSelected callback → viewModel.selectSize(size)
  ↓
_selectedSize được cập nhật → Text button đổi màu
  ↓
User click "ADD TO BAG"
  ↓
onAddToCartClick callback → viewModel.addToCart(productId)
  ↓
productRepository.addToCart(productId)
  ↓
Navigate to Cart screen: onNavigateToCart()
```

---

## 🎨 State Management Pattern

### **MutableStateFlow vs StateFlow**
```kotlin
// Private MutableStateFlow - chỉ ViewModel có thể sửa
private val _productDetail = MutableStateFlow<Product?>(null)

// Public StateFlow - UI chỉ có thể đọc
val productDetail: StateFlow<Product?> = _productDetail.asStateFlow()

// UI collect từ public StateFlow
val product by viewModel.productDetail.collectAsState(initial = null)
```

**Tại sao cần 2 cái?**
- `_productDetail` (private): ViewModel sửa dữ liệu
- `productDetail` (public): UI đọc dữ liệu
- Tránh UI sửa state trực tiếp → Bảo mật logic

---

## ✅ Import Navigation vào MainActivity

```kotlin
// 1. Import ProductDetailScreen & ViewModel
import com.example.shoestoreapp.features.product.ui.product_detail.ProductDetailScreen
import com.example.shoestoreapp.features.product.viewmodel.ProductDetailViewModel

// 2. Thêm route
composable("product_detail/{productId}") { backStackEntry ->
    val productId = backStackEntry.arguments?.getString("productId")?.toInt() ?: 1
    
    ProductDetailScreen(
        productId = productId,
        viewModel = remember { ProductDetailViewModel() },
        onBackClick = { navController.popBackStack() },
        onNavigateToCart = { println("Go to cart") }
    )
}

// 3. Update ProductListScreen callback
ProductListScreen(
    ...
    onNavigateToDetail = { productId ->
        navController.navigate("product_detail/$productId")
    }
)
```

---

## 🧪 Testing Component Riêng Lẻ

```kotlin
@Preview(showBackground = true)
@Composable
fun PreviewProductHeroImage() {
    ProductHeroImage(
        imageUrl = "https://...",
        contentDescription = "Nike Air Max"
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSizeSelector() {
    SizeSelector(
        selectedSize = 8,
        onSizeSelected = { size -> println("Selected: $size") }
    )
}
```

---

## 🚀 Cách Chạy & Test

### 1. **Chạy toàn app**
```bash
./gradlew compileDebugKotlin  # Check lỗi Kotlin
./gradlew assembleDebug        # Build APK
```

### 2. **View Preview trong Android Studio**
- Mở file ProductDetailScreen.kt
- Nhấn `Preview` (bên phải)
- Hoặc Ctrl+Shift+P

### 3. **Test Navigation**
- Change `startDestination = "product_list"`
- Click vào ProductCard
- Verify navigate sang ProductDetailScreen

---

## 📝 Callback Functions - Ai Phụ Trách?

| Callback | Phụ Trách | Chi Tiết |
|----------|----------|---------|
| `onProductClick` | Frontend | ProductListScreen → ProductDetailScreen |
| `onSizeSelected` | Frontend | Update UI (highlight size) |
| `onAddToCartClick` | Frontend/Backend | Add to DB + Navigate |
| `toggleFavorite` | Frontend/Backend | Update DB + UI |
| `onBackClick` | Frontend | Pop back stack |

---

## 🔍 Sơ Đồ Class & Mối Liên Hệ

```
ViewModel (ProductDetailViewModel)
├── State: productDetail, selectedSize, isLoading
├── Function: loadProductDetail(), selectSize(), toggleFavorite(), addToCart()
└── Gọi → Repository.getAllProducts(), Repository.toggleFavorite(), Repository.addToCart()

Repository (ProductRepository)
├── Mock Data: listOf(Product(...), ...)
├── State: _productList (MutableStateFlow)
├── Function: getAllProducts(), toggleFavorite(), addToCart()
└── Trả về → Flow<List<Product>>

Screen (ProductDetailScreen)
├── Collect: productDetail, selectedSize, isLoading
├── Component: ProductHeroImage, ProductHeaderInfo, SizeSelector, ...
└── Callback: onProductClick, onSizeSelected, onAddToCartClick, ...
```

---

## 📚 Tài Liệu Tham Khảo

- [Jetpack Compose Documentation](https://developer.android.com/develop/ui/compose)
- [MVVM Architecture](https://developer.android.com/topic/architecture/recommended-app-arch)
- [Coroutines & Flow](https://kotlinlang.org/docs/flow.html)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)

