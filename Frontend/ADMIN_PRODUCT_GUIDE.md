# Hướng Dẫn Admin Product Management

## 📋 Tổng Quan

Tôi vừa tạo xong màn hình quản lý sản phẩm cho Admin theo kiến trúc MVVM. Hệ thống này cho phép admin:
- Xem danh sách sản phẩm
- Tìm kiếm sản phẩm
- Lọc sản phẩm theo trạng thái kho (IN STOCK, LOW STOCK, OUT OF STOCK)

---

## 📁 Cấu Trúc Thư Mục

```
features/admin/product/
├── data/
│   ├── models/
│   │   └── AdminProduct.kt          ← Mô hình dữ liệu sản phẩm
│   └── repositories/
│       └── AdminProductRepository.kt ← Quản lý dữ liệu sản phẩm
├── ui/
│   ├── AdminProductListScreen.kt    ← Màn hình chính
│   └── components/
│       ├── AdminTopAppBar.kt        ← Thanh trên (Menu, NIKE, Add Product)
│       ├── AdminSearchBar.kt        ← Thanh tìm kiếm
│       ├── AdminFilterChips.kt      ← Chip lọc sản phẩm
│       ├── AdminProductCard.kt      ← Card hiển thị sản phẩm
│       └── AdminBottomNavBar.kt     ← Thanh dưới (ADMIN, ORDERS, ...)
└── viewmodel/
    └── AdminProductListViewModel.kt ← Quản lý state & logic
```

---

## 🔍 Chi Tiết Từng File

### 1️⃣ **AdminProduct.kt** (Data Model)

**Chức năng:** Định nghĩa cấu trúc dữ liệu sản phẩm cho admin

**Bao gồm:**
- `StockStatus` enum: 3 trạng thái kho (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)
- `AdminProduct` data class: Thông tin sản phẩm

```kotlin
data class AdminProduct(
    val id: Int,                           // ID sản phẩm
    val name: String,                      // Tên ("Nike Air Max 270")
    val imageUrl: String,                  // Link ảnh
    val description: String,               // Mô tả ("Red and Black")
    val price: Double,                     // Giá tiền
    val stockStatus: StockStatus,          // Trạng thái kho
    val quantity: Int,                     // Số lượng còn lại
    val category: String,                  // Danh mục
    val productType: String                // Loại sản phẩm
)
```

**Khác biệt vs User Product Model:**
- Thêm field `stockStatus` và `quantity` để quản lý kho

---

### 2️⃣ **AdminProductRepository.kt** (Data Source)

**Chức năng:** Cung cấp dữ liệu sản phẩm cho ViewModel

**Các chức năng chính:**

```kotlin
// Danh sách tất cả sản phẩm
val adminProducts: StateFlow<List<AdminProduct>>

// Filter hiện tại
val selectedFilter: StateFlow<String>

// Thay đổi filter
fun setFilter(filter: String)

// Lấy sản phẩm đã lọc
fun getFilteredProducts(): List<AdminProduct>

// Tìm kiếm theo tên
fun searchProducts(query: String): List<AdminProduct>
```

**Logic lọc:**
```
ALL PRODUCTS    → Hiển thị tất cả
IN STOCK        → Chỉ sản phẩm có status = IN_STOCK
LOW STOCK       → Chỉ sản phẩm có status = LOW_STOCK
OUT OF STOCK    → Chỉ sản phẩm có status = OUT_OF_STOCK
```

---

### 3️⃣ **AdminProductListViewModel.kt** (Business Logic)

**Chức năng:** Quản lý state và logic ứng dụng

**State quản lý:**
```kotlin
_products: MutableStateFlow<List<AdminProduct>>     // Danh sách hiển thị
_selectedFilter: MutableStateFlow<String>           // Filter chọn
_searchText: MutableStateFlow<String>               // Text tìm kiếm
_isLoading: MutableStateFlow<Boolean>               // Trạng thái loading
```

**Callbacks chính:**
```kotlin
fun onFilterChanged(filter: String)      // Khi user chọn filter
fun onSearchChanged(query: String)       // Khi user gõ tìm kiếm
```

**Flow hoạt động:**
```
User click filter chip
    ↓
onFilterChanged() được gọi
    ↓
setFilter() trong repository
    ↓
applyFiltersAndSearch() lọc lại danh sách
    ↓
_products.value được cập nhật
    ↓
UI recompose và hiển thị sản phẩm mới
```

---

### 4️⃣ **AdminProductCard.kt** (Component)

**Chức năng:** Hiển thị thông tin 1 sản phẩm dạng card

**Bố cục:**
```
┌─────────────────────┐
│   Ảnh sản phẩm      │  (Aspect ratio 1:1)
├─────────────────────┤
│ Nike Air Max Pulse  │  (Tên sản phẩm - Bold)
│ $160.00             │  (Giá - Gray)
│ IN STOCK            │  (Trạng thái - Màu đen/vàng/đỏ)
└─────────────────────┘
```

**Hiển thị trạng thái:**
- `IN STOCK` → Chữ đen
- `LOW STOCK: 08` → Chữ vàng cam
- `OUT OF STOCK` → Chữ đỏ

---

### 5️⃣ **AdminSearchBar.kt** (Component)

**Chức năng:** Tìm kiếm sản phẩm theo tên

```kotlin
AdminSearchBar(
    searchText: String,              // Text tìm kiếm hiện tại
    onSearchChanged: (String) -> Unit // Callback khi user gõ
)
```

**UI:**
```
🔍 [Search Inventory         ]
```

---

### 6️⃣ **AdminFilterChips.kt** (Component)

**Chức năng:** Lọc sản phẩm theo status

**4 Filter chip:**
1. `ALL PRODUCTS` (mặc định)
2. `IN STOCK`
3. `LOW STOCK`
4. `OUT OF STOCK`

**UI:**
```
[ALL PRODUCTS] [IN STOCK] [LOW STOCK] [OUT OF STOCK]
 ├─ Chọn → Đen + chữ trắng
 └─ Chưa chọn → Border xám + chữ xám
```

---

### 7️⃣ **AdminTopAppBar.kt** (Component)

**Chức năng:** Thanh trên cùng

**Bố cục:**
```
☰ Menu    |    NIKE    |    [Add Product]
```

**Callback:**
- `onMenuClick()` - Click menu icon
- `onAddProductClick()` - Click Add Product button

---

### 8️⃣ **AdminBottomNavBar.kt** (Component)

**Chức năng:** Điều hướng giữa các tab admin

**4 Tab:**
```
ADMIN (hiện tại) | ORDERS | ANALYTICS | SETTINGS
```

**Enum:**
```kotlin
enum class AdminBottomNavTab {
    ADMIN, ORDERS, ANALYTICS, SETTINGS
}
```

---

### 9️⃣ **AdminProductListScreen.kt** (Screen)

**Chức năng:** Màn hình chính gộp tất cả components

**Cấu trúc:**
```
┌─────────────────────────────────┐
│     AdminTopAppBar              │  (Menu, NIKE, Add Product)
├─────────────────────────────────┤
│   AdminSearchBar                │  (🔍 Search Inventory)
├─────────────────────────────────┤
│ AdminFilterChips                │  (ALL | IN | LOW | OUT)
├─────────────────────────────────┤
│                                 │
│  ┌──────────────┐┌──────────────┐│
│  │   Product 1  ││   Product 2  ││  LazyVerticalGrid
│  │              ││              ││  (2 cột)
│  └──────────────┘└──────────────┘│
│  ┌──────────────┐┌──────────────┐│
│  │   Product 3  ││   Product 4  ││
│  │              ││              ││
│  └──────────────┘└──────────────┘│
│                                 │
├─────────────────────────────────┤
│   AdminBottomNavBar             │  (ADMIN | ORDERS | ...)
└─────────────────────────────────┘
```

---

## 🔄 Flow Dữ Liệu (Data Flow)

### 1. User Click Filter Chip

```
User click "IN STOCK"
    ↓
AdminFilterChips.onFilterSelected("IN STOCK")
    ↓
AdminProductListScreen.viewModel.onFilterChanged("IN STOCK")
    ↓
AdminProductListViewModel:
  - _selectedFilter.value = "IN STOCK"
  - repository.setFilter("IN STOCK")
  - applyFiltersAndSearch()
    ↓
Repository.getFilteredProducts():
  - Lọc list -> chỉ giữ status = IN_STOCK
    ↓
_products.value = [filtered products]
    ↓
UI Recompose → LazyVerticalGrid hiển thị sản phẩm mới
```

### 2. User Gõ Text Tìm Kiếm

```
User gõ "nike"
    ↓
AdminSearchBar.onSearchChanged("nike")
    ↓
AdminProductListViewModel.onSearchChanged("nike")
    ↓
_searchText.value = "nike"
    ↓
applyFiltersAndSearch():
  - Repository.searchProducts("nike")
  - Kết hợp với filter hiện tại
    ↓
_products.value = [products match "nike" AND filter]
    ↓
UI Recompose
```

---

## 🚀 Cách Sử Dụng

### Hiển thị màn hình Admin

```kotlin
// Trong MainActivity.kt hoặc navigation file
AdminProductListScreen(
    viewModel = AdminProductListViewModel(),
    onMenuClick = { /* Handle menu click */ },
    onAddProductClick = { /* Navigate to add product screen */ },
    onTabSelected = { tab -> /* Handle tab selection */ }
)
```

### Thêm sản phẩm mới

```kotlin
// Trong repository
fun addProduct(product: AdminProduct) {
    _adminProducts.value = _adminProducts.value + product
}
```

### Xóa sản phẩm

```kotlin
fun deleteProduct(productId: Int) {
    _adminProducts.value = _adminProducts.value.filter { it.id != productId }
}
```

---

## 📌 Lưu Ý Quan Trọng

1. **Dữ liệu hiện tại là mock/hardcoded**
   - Sau này kết nối API backend bằng Retrofit

2. **Repository không dùng Flow**
   - Vì logic đơn giản, chỉ cần StateFlow
   - Nếu có async operation, dùng ViewModel.launch

3. **Filter và Search độc lập**
   - Có thể kết hợp: Tìm kiếm trong kết quả đã lọc

4. **Callbacks để mở rộng tính năng**
   - `onMenuClick()` - Mở drawer menu
   - `onAddProductClick()` - Điều hướng tới AddProductScreen
   - `onTabSelected()` - Chuyển sang Orders, Analytics, Settings

---

## 🔗 Kết Nối Với Các Screen Khác

### Navigation Graph (sẽ tạo sau)

```
AdminProductListScreen
    ├─ onAddProductClick() → AdminAddProductScreen
    └─ AdminProductCard.onProductClick() → AdminEditProductScreen
```

---

## ✅ Kiểm Tra Hoàn Thành

- [x] Tạo AdminProduct model với StockStatus
- [x] Tạo AdminProductRepository với filter & search
- [x] Tạo AdminProductListViewModel
- [x] Tạo 5 components (TopBar, SearchBar, FilterChips, ProductCard, BottomBar)
- [x] Tạo AdminProductListScreen main screen
- [x] Cấu trúc MVVM hoàn chỉnh
- [ ] Kết nối API backend
- [ ] Tạo thêm Add/Edit Product screen

---

## 💡 Tiếp Theo

1. Tạo **AdminAddProductScreen** - Để thêm sản phẩm mới
2. Tạo **AdminEditProductScreen** - Để chỉnh sửa sản phẩm
3. Kết nối **API backend** cho CRUD operations
4. Thêm **Pagination** nếu có nhiều sản phẩm
5. Implement **Admin Orders** và **Admin Analytics** screens

