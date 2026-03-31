# 📚 Giải Thích Chi Tiết Code Admin Product Management

## 📖 Mục Lục

1. [AdminProduct.kt](#1-adminproductkt)
2. [AdminProductRepository.kt](#2-adminproductrepositorykt)
3. [AdminProductListViewModel.kt](#3-adminproductlistviewmodelkt)
4. [AdminProductCard.kt](#4-adminproductcardkt)
5. [AdminSearchBar.kt](#5-adminsearchbarkt)
6. [AdminFilterChips.kt](#6-adminfilterchhipskt)
7. [AdminTopAppBar.kt](#7-admintopappbarkt)
8. [AdminBottomNavBar.kt](#8-adminbottomnavbarkt)
9. [AdminProductListScreen.kt](#9-adminproductlistscreenkt)

---

## 1. AdminProduct.kt

### Enum: StockStatus

```kotlin
enum class StockStatus {
    IN_STOCK,      // Sản phẩm còn hàng (quantity > 10)
    LOW_STOCK,     // Sắp hết hàng (quantity từ 1-10)
    OUT_OF_STOCK   // Hết hàng (quantity = 0)
}
```

**Ý nghĩa:**
- Giúp xác định trạng thái tồn kho của sản phẩm
- Dùng để lọc và hiển thị màu sắc khác nhau trên UI

---

### Data Class: AdminProduct

```kotlin
data class AdminProduct(
    val id: Int,                      // ID duy nhất
    val name: String,                 // Tên sản phẩm
    val imageUrl: String,             // Liên kết ảnh
    val description: String,          // Mô tả (ví dụ: màu sắc)
    val price: Double,                // Giá bán
    val stockStatus: StockStatus,     // Trạng thái kho
    val quantity: Int,                // Số lượng còn lại
    val category: String,             // Danh mục
    val productType: String           // Loại sản phẩm
)
```

**Ví dụ thực tế:**
```kotlin
AdminProduct(
    id = 1,
    name = "Nike Air Max Pulse",
    imageUrl = "https://...",
    description = "Bright Green",
    price = 160.0,
    stockStatus = StockStatus.IN_STOCK,    // Còn hàng
    quantity = 45,                          // Còn 45 cái
    category = "BESTSELLER",
    productType = "Men's Shoes"
)
```

---

## 2. AdminProductRepository.kt

### Khai Báo StateFlow

```kotlin
private val _adminProducts = MutableStateFlow<List<AdminProduct>>(emptyList())
val adminProducts: StateFlow<List<AdminProduct>> = _adminProducts.asStateFlow()
```

**Giải thích:**
- `_adminProducts` (private) - MutableStateFlow để thay đổi dữ liệu
- `adminProducts` (public) - StateFlow chỉ đọc để ViewModel dùng
- Khởi tạo với `emptyList()` - Danh sách trống ban đầu

**Tại sao dùng StateFlow?**
- Quản lý state (trạng thái) dữ liệu
- Tự động phát thông báo khi dữ liệu thay đổi
- UI tự động recompose khi nhận thông báo

---

### Init Block: Tải Dữ Liệu

```kotlin
init {
    loadProducts()
}
```

**Chạy ngay khi:**
- `AdminProductRepository()` được khởi tạo
- Dùng để load dữ liệu ban đầu

---

### Hàm: loadProducts()

```kotlin
private fun loadProducts() {
    val products = listOf(
        AdminProduct(1, "Nike Air Max Pulse", "...", "Bright Green", 160.0, 
                    StockStatus.IN_STOCK, 45, ...),
        AdminProduct(2, "Nike Pegasus 40", "...", "Red and Black", 130.0,
                    StockStatus.LOW_STOCK, 8, ...),
        // ... thêm sản phẩm khác
    )
    _adminProducts.value = products
}
```

**Chức năng:**
- Tạo danh sách 6 sản phẩm mẫu
- Gán vào `_adminProducts.value`
- Tự động phát thông báo cho subscribers

**Sau này sẽ thay thế bằng:**
```kotlin
// API call
private fun loadProducts() {
    viewModelScope.launch {
        try {
            val response = apiService.getAllProducts()
            _adminProducts.value = response.products
        } catch (e: Exception) {
            // Handle error
        }
    }
}
```

---

### Hàm: setFilter()

```kotlin
fun setFilter(filter: String) {
    _selectedFilter.value = filter
}
```

**Tác dụng:**
- Lưu filter được chọn
- Khi gọi hàm này, `_selectedFilter` phát thông báo
- ViewModel nhận thông báo và cập nhật UI

**Ví dụ:**
```kotlin
repository.setFilter("IN STOCK")  // _selectedFilter.value = "IN STOCK"
```

---

### Hàm: getFilteredProducts()

```kotlin
fun getFilteredProducts(): List<AdminProduct> {
    val filter = _selectedFilter.value
    return when (filter) {
        "IN_STOCK" -> adminProducts.value.filter { it.stockStatus == StockStatus.IN_STOCK }
        "LOW_STOCK" -> adminProducts.value.filter { it.stockStatus == StockStatus.LOW_STOCK }
        "OUT_OF_STOCK" -> adminProducts.value.filter { it.stockStatus == StockStatus.OUT_OF_STOCK }
        else -> adminProducts.value  // Default: ALL PRODUCTS
    }
}
```

**Giải thích từng phần:**

1. **Lấy filter hiện tại:**
   ```kotlin
   val filter = _selectedFilter.value  // "IN STOCK", "LOW STOCK", ...
   ```

2. **Switch case theo filter:**
   ```kotlin
   when (filter) {
       "IN_STOCK" -> ...      // Nếu filter = "IN STOCK"
       "LOW_STOCK" -> ...     // Nếu filter = "LOW STOCK"
       ...
       else -> ...            // Các trường hợp khác
   }
   ```

3. **Lọc danh sách:**
   ```kotlin
   adminProducts.value.filter { it.stockStatus == StockStatus.IN_STOCK }
   //                  ^^^^^^ Lambda function
   // it = product hiện tại
   // it.stockStatus == ... = điều kiện lọc
   ```

**Ví dụ thực tế:**
```
Input: _selectedFilter.value = "IN_STOCK"
       adminProducts.value = [6 sản phẩm]

Process:
  adminProducts.value.filter { it.stockStatus == IN_STOCK }
  → Lọc chỉ giữ sản phẩm có status = IN_STOCK
  → Xoá các sản phẩm có status = LOW_STOCK, OUT_OF_STOCK

Output: [3 sản phẩm có status IN_STOCK]
```

---

### Hàm: searchProducts()

```kotlin
fun searchProducts(query: String): List<AdminProduct> {
    return if (query.isEmpty()) {
        adminProducts.value
    } else {
        adminProducts.value.filter { product ->
            product.name.contains(query, ignoreCase = true)
        }
    }
}
```

**Giải thích:**

1. **Nếu search trống:**
   ```kotlin
   if (query.isEmpty())
       return adminProducts.value  // Trả về tất cả
   ```

2. **Nếu search có text:**
   ```kotlin
   else
       return adminProducts.value.filter { product ->
           product.name.contains(query, ignoreCase = true)
           // Kiểm tra tên sản phẩm chứa text tìm kiếm
           // ignoreCase = true → không phân biệt hoa/thường
       }
   ```

**Ví dụ:**
```
Input: query = "nike"
       adminProducts.value = 
       [
           "Nike Air Max Pulse",     ✓ Chứa "nike"
           "Nike Pegasus 40",        ✓ Chứa "nike"
           "Adidas Ultraboost"       ✗ Không chứa "nike"
       ]

Output: ["Nike Air Max Pulse", "Nike Pegasus 40"]
```

---

## 3. AdminProductListViewModel.kt

### State - Khai Báo

```kotlin
private val _products = MutableStateFlow<List<AdminProduct>>(emptyList())
val products: StateFlow<List<AdminProduct>> = _products.asStateFlow()
```

**Ý nghĩa:**
- `_products` = nơi lưu dữ liệu (private)
- `products` = để UI đọc (public)
- Type: `StateFlow<List<AdminProduct>>` = danh sách sản phẩm

---

### Init Block

```kotlin
init {
    loadProducts()
}
```

**Tự động chạy khi:**
- ViewModel được tạo
- Load dữ liệu từ repository

---

### Hàm: loadProducts()

```kotlin
private fun loadProducts() {
    viewModelScope.launch {
        _isLoading.value = true
        try {
            _products.value = repository.adminProducts.value
        } finally {
            _isLoading.value = false
        }
    }
}
```

**Giải thích:**

1. **Launch coroutine:**
   ```kotlin
   viewModelScope.launch { ... }
   // Chạy không đồng bộ (async)
   // Tự động hủy khi ViewModel destroyed
   ```

2. **Set loading:**
   ```kotlin
   _isLoading.value = true  // Bắt đầu load
   ```

3. **Lấy dữ liệu:**
   ```kotlin
   _products.value = repository.adminProducts.value
   // Gọi repository để lấy danh sách
   ```

4. **Try-finally:**
   ```kotlin
   try { ... } 
   finally {
       _isLoading.value = false  // Luôn set loading = false
       // Dù thành công hay thất bại
   }
   ```

---

### Hàm: onFilterChanged()

```kotlin
fun onFilterChanged(filter: String) {
    _selectedFilter.value = filter
    repository.setFilter(filter)
    applyFiltersAndSearch()
}
```

**Flow:**
1. UI gọi `onFilterChanged("IN STOCK")`
2. Cập nhật local state: `_selectedFilter.value = "IN STOCK"`
3. Báo repository: `repository.setFilter("IN STOCK")`
4. Cập nhật danh sách hiển thị: `applyFiltersAndSearch()`
5. Emit thông báo → UI recompose

---

### Hàm: onSearchChanged()

```kotlin
fun onSearchChanged(query: String) {
    _searchText.value = query
    applyFiltersAndSearch()
}
```

**Tương tự onFilterChanged nhưng lưu search text**

---

### Hàm: applyFiltersAndSearch()

```kotlin
private fun applyFiltersAndSearch() {
    val filtered = repository.getFilteredProducts()
    _products.value = repository.searchProducts(_searchText.value).let { searched ->
        if (_searchText.value.isEmpty()) {
            filtered
        } else {
            filtered.filter { product ->
                searched.any { it.id == product.id }
            }
        }
    }
}
```

**Giải thích phức tạp:**

1. **Lấy sản phẩm đã lọc:**
   ```kotlin
   val filtered = repository.getFilteredProducts()
   // filtered = sản phẩm sau khi apply filter
   ```

2. **Tìm kiếm:**
   ```kotlin
   repository.searchProducts(_searchText.value)
   // searched = sản phẩm sau khi apply search
   ```

3. **Kết hợp (AND logic):**
   ```kotlin
   .let { searched ->
       if (_searchText.value.isEmpty()) {
           filtered           // Không search → chỉ dùng filter
       } else {
           filtered.filter { product ->
               searched.any { it.id == product.id }
               // Chỉ giữ sản phẩm có trong cả 2 danh sách
           }
       }
   }
   ```

**Ví dụ:**
```
Filter = "IN STOCK" → [A, B, C] (3 sản phẩm)
Search = "nike" → [A, B, D] (3 sản phẩm)

Kết hợp AND:
[A, B, C] ∩ [A, B, D] = [A, B]  (2 sản phẩm)
```

---

## 4. AdminProductCard.kt

### Component Declaration

```kotlin
@Composable
fun AdminProductCard(
    product: AdminProduct,
    onProductClick: (Int) -> Unit = {}
) { ... }
```

**Parameters:**
- `product: AdminProduct` - Dữ liệu sản phẩm
- `onProductClick: (Int) -> Unit = {}` - Callback khi click (mặc định không làm gì)

---

### Hình Ảnh Sản Phẩm

```kotlin
AsyncImage(
    model = product.imageUrl,
    contentDescription = product.name,
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .padding(0.dp)
        .background(androidx.compose.ui.graphics.Color(0xFFF5F5F5)),
    contentScale = ContentScale.Crop
)
```

**Chi tiết:**

| Tham số | Ý nghĩa |
|--------|---------|
| `model = product.imageUrl` | Load ảnh từ URL |
| `contentDescription = product.name` | Text mô tả cho accessibility |
| `.fillMaxWidth()` | Rộng bằng parent |
| `.aspectRatio(1f)` | Hình vuông (rộng = cao) |
| `.padding(0.dp)` | Không khoảng cách |
| `.background(0xFFF5F5F5)` | Background xám nhạt |
| `ContentScale.Crop` | Cắt ảnh vừa với size |

---

### Trạng Thái Stock - Color Logic

```kotlin
val (statusText, statusColor) = when (product.stockStatus) {
    StockStatus.IN_STOCK -> "IN STOCK" to androidx.compose.ui.graphics.Color.Black
    StockStatus.LOW_STOCK -> "LOW STOCK: ${product.quantity}" to androidx.compose.ui.graphics.Color(0xFFFF9800)
    StockStatus.OUT_OF_STOCK -> "OUT OF STOCK" to androidx.compose.ui.graphics.Color(0xFFD32F2F)
}
```

**Giải thích:**

1. **Destructuring tuple:**
   ```kotlin
   val (statusText, statusColor) = ...
   // Gán 2 giá trị vào 2 biến
   ```

2. **When expression:**
   ```kotlin
   when (product.stockStatus) {
       IN_STOCK -> ...        // Nếu còn hàng
       LOW_STOCK -> ...       // Nếu sắp hết
       OUT_OF_STOCK -> ...    // Nếu hết hàng
   }
   ```

3. **Pair (to):**
   ```kotlin
   "IN STOCK" to Color.Black
   // Tạo tuple 2 phần tử: ("IN STOCK", Color.Black)
   ```

4. **Format text:**
   ```kotlin
   "LOW STOCK: ${product.quantity}"
   // "LOW STOCK: 08" (dùng padStart để pad 0)
   ```

---

## 5. AdminSearchBar.kt

### Component

```kotlin
@Composable
fun AdminSearchBar(
    searchText: String,
    onSearchChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search Inventory") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        shape = RoundedCornerShape(24.dp),
        maxLines = 1
    )
}
```

**Chi tiết:**

| Property | Ý nghĩa |
|----------|---------|
| `value` | Text hiện tại |
| `onValueChange` | Callback mỗi khi text thay đổi |
| `placeholder` | Text hiển thị khi rỗng |
| `leadingIcon` | Icon ở bên trái |
| `shape = RoundedCornerShape(24.dp)` | Góc tròn 24dp |
| `maxLines = 1` | Chỉ 1 dòng |

---

## 6. AdminFilterChips.kt

### LazyRow Loop

```kotlin
LazyRow(...) {
    items(filters.size) { index ->
        val filter = filters[index]
        val isSelected = selectedFilter == filter
        
        Text(
            text = filter,
            modifier = Modifier
                .background(
                    color = if (isSelected) Color.Black else Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable { onFilterSelected(filter) }
                ...
        )
    }
}
```

**Giải thích:**

1. **Loop qua danh sách:**
   ```kotlin
   items(filters.size) { index ->
       val filter = filters[index]
   }
   // Tương tự: for (int i = 0; i < filters.size(); i++)
   ```

2. **Kiểm tra chọn:**
   ```kotlin
   val isSelected = selectedFilter == filter
   // true nếu filter này = filter được chọn
   ```

3. **Background color:**
   ```kotlin
   color = if (isSelected) Color.Black else Color.White
   // Đen nếu chọn, trắng nếu không
   ```

4. **Click handler:**
   ```kotlin
   .clickable { onFilterSelected(filter) }
   // Gọi callback khi user click
   ```

---

## 7. AdminTopAppBar.kt

### Row Layout

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .background(Color.White)
        .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
)
```

**Chi tiết:**

| Modifier | Ý nghĩa |
|----------|---------|
| `fillMaxWidth()` | Rộng toàn màn hình |
| `height(64.dp)` | Cao 64dp |
| `background(Color.White)` | Nền trắng |
| `padding(16.dp)` | Khoảng cách 16dp |
| `Arrangement.SpaceBetween` | Các item phân tán ra |
| `Alignment.CenterVertically` | Căn giữa theo chiều dọc |

---

## 8. AdminBottomNavBar.kt

### Enum & Loop

```kotlin
enum class AdminBottomNavTab {
    ADMIN, ORDERS, ANALYTICS, SETTINGS
}

Row(...) {
    AdminBottomNavTab.values().forEach { tab ->
        val isSelected = selectedTab == tab
        Text(
            text = tab.name,
            color = if (isSelected) Color.Black else Color.LightGray,
            modifier = Modifier.clickable { onTabSelected(tab) }
        )
    }
}
```

**Giải thích:**

1. **Enum:**
   ```kotlin
   enum class AdminBottomNavTab {
       ADMIN, ORDERS, ANALYTICS, SETTINGS
   }
   // 4 tab cố định
   ```

2. **Loop qua enum:**
   ```kotlin
   AdminBottomNavTab.values().forEach { tab ->
       // Duyệt tất cả 4 tab
   }
   ```

3. **Get name:**
   ```kotlin
   tab.name  // "ADMIN", "ORDERS", ...
   ```

---

## 9. AdminProductListScreen.kt

### Scaffold Layout

```kotlin
Scaffold(
    topBar = { AdminTopAppBar(...) },
    bottomBar = { AdminBottomNavBar(...) }
) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Content
    }
}
```

**Chức năng:**
- `Scaffold` = material layout chứa topBar + content + bottomBar
- `paddingValues` = khoảng cách cho topBar/bottomBar
- `.padding(paddingValues)` = thêm khoảng cách để tránh overlap

---

### Collect State

```kotlin
val products = viewModel.products.collectAsState()
val selectedFilter = viewModel.selectedFilter.collectAsState()
val searchText = viewModel.searchText.collectAsState()
val isLoading = viewModel.isLoading.collectAsState()
```

**Ý nghĩa:**
- `collectAsState()` = convert Flow/StateFlow thành State
- State tự động update khi value thay đổi
- UI tự động recompose

---

### LazyVerticalGrid

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),  // 2 cột
    modifier = Modifier.fillMaxSize()
) {
    items(products.value.size) { index ->
        AdminProductCard(
            product = products.value[index],
            onProductClick = { productId ->
                println("Edit product: $productId")
            }
        )
    }
}
```

**Chi tiết:**

| Code | Ý nghĩa |
|------|---------|
| `GridCells.Fixed(2)` | 2 cột cố định |
| `items(size)` | Loop qua tất cả item |
| `products.value[index]` | Lấy sản phẩm thứ index |

---

## 🎯 Tóm Tắt Data Flow

```
┌─────────────────────────────────┐
│    AdminProductListScreen       │
│  (UI - Hiển thị)                │
└───────────┬───────────────────────┘
            │ collectAsState()
            ↓
┌─────────────────────────────────┐
│  AdminProductListViewModel      │
│  State:                         │
│  - _products                    │
│  - _selectedFilter              │
│  - _searchText                  │
│  - _isLoading                   │
│                                 │
│  Methods:                       │
│  - onFilterChanged()            │
│  - onSearchChanged()            │
│  - applyFiltersAndSearch()      │
└───────────┬───────────────────────┘
            │ calls
            ↓
┌─────────────────────────────────┐
│ AdminProductRepository          │
│ State:                          │
│ - _adminProducts                │
│ - _selectedFilter               │
│                                 │
│ Methods:                        │
│ - loadProducts()                │
│ - setFilter()                   │
│ - getFilteredProducts()         │
│ - searchProducts()              │
└───────────┬───────────────────────┘
            │ uses
            ↓
┌─────────────────────────────────┐
│ AdminProduct (Model)            │
│ StockStatus (Enum)              │
│ (Dữ liệu)                       │
└─────────────────────────────────┘
```

---

## ✅ Kiểm Tra Hiểu

**Câu hỏi 1:** Khi user click "IN STOCK" filter, điều gì xảy ra?
- UI gọi `onFilterChanged("IN STOCK")`
- ViewModel lưu filter + gọi `applyFiltersAndSearch()`
- Repository lọc danh sách → trả về chỉ sản phẩm IN_STOCK
- ViewModel update `_products.value`
- UI nhận thông báo → recompose → hiển thị sản phẩm mới

**Câu hỏi 2:** Khác biệt giữa `_products` và `products`?
- `_products` = private MutableStateFlow (có thể thay đổi)
- `products` = public StateFlow (chỉ đọc)
- Tách riêng để bảo vệ dữ liệu

**Câu hỏi 3:** Tại sao dùng `collectAsState()`?
- Convert Flow → State trong Compose
- State tự động update UI khi value thay đổi

---

## 🚀 Next Steps

1. Kết nối API backend
2. Thêm handling errors
3. Implement Add/Edit Product screens
4. Thêm pagination để load nhiều sản phẩm

