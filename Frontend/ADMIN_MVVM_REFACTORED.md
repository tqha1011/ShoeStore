# 📐 Cấu Trúc MVVM Được Sửa Lại

## Vấn Đề Ban Đầu ❌

```
Repository (Sai)
├── loadProducts()           ← OK (cung cấp data)
├── getFilteredProducts()    ← SAI (business logic)
├── searchProducts()         ← SAI (business logic)
└── setFilter()             ← SAI (quản lý state UI)

ViewModel (Thiếu)
├── onFilterChanged()        → Gọi repository.setFilter()
├── onSearchChanged()        → Gọi repository.searchProducts()
└── applyFiltersAndSearch()  → Gọi repository.getFilteredProducts()
```

**Tại sao sai?**
- Repository làm quá nhiều → khó maintain
- ViewModel chỉ là "proxy" → không xử lý logic
- Nếu thay đổi logic filter → phải sửa Repository (data layer)

---

## Cấu Trúc MVVM Đúng ✅

```
┌─────────────────────────────────────┐
│      UI Layer                       │
│  AdminProductListScreen             │
│  ├─ onFilterChanged()               │
│  └─ onSearchChanged()               │
└────────────┬────────────────────────┘
             │ collectAsState()
             ↓
┌─────────────────────────────────────┐
│      ViewModel Layer                │
│  AdminProductListViewModel          │
│  ├─ State:                          │
│  │  ├─ _products                    │
│  │  ├─ _selectedFilter              │ ← Quản lý filter (không phải repo)
│  │  ├─ _searchText                  │ ← Quản lý search text (không phải repo)
│  │  └─ _isLoading                   │
│  │                                  │
│  ├─ Callbacks:                      │
│  │  ├─ onFilterChanged()            │
│  │  └─ onSearchChanged()            │
│  │                                  │
│  └─ Business Logic: ← MOVED HERE    │
│     ├─ getFilteredProducts()        │
│     ├─ searchProducts()             │
│     └─ applyFiltersAndSearch()      │
└────────────┬────────────────────────┘
             │
             ↓ calls
┌─────────────────────────────────────┐
│      Repository Layer               │
│  AdminProductRepository             │
│  ├─ State:                          │
│  │  └─ _adminProducts               │
│  │                                  │
│  └─ Methods:                        │
│     └─ loadProducts()               │
│        (Chỉ cung cấp raw data)      │
└─────────────────────────────────────┘
```

---

## Chi Tiết Các Hàm

### Repository - Chỉ Cung Cấp Data

```kotlin
class AdminProductRepository {
    // ✅ Cung cấp raw data từ API/Database
    val adminProducts: StateFlow<List<AdminProduct>>
    
    // ✅ Load dữ liệu
    private fun loadProducts()
    
    // ✅ Thêm/Xoá/Sửa (CRUD - nếu cần)
    // fun addProduct(product: AdminProduct)
    // fun deleteProduct(id: Int)
    // fun updateProduct(product: AdminProduct)
    
    // ❌ KHÔNG NÊN: Business logic
    // fun getFilteredProducts()
    // fun searchProducts()
}
```

---

### ViewModel - Xử Lý Business Logic

```kotlin
class AdminProductListViewModel {
    // ============ STATE (Quản lý UI state) ============
    val products: StateFlow<List<AdminProduct>>
    val selectedFilter: StateFlow<String>        // Quản lý ở ViewModel
    val searchText: StateFlow<String>            // Quản lý ở ViewModel
    val isLoading: StateFlow<Boolean>
    
    // ============ CALLBACKS (Từ UI) ============
    fun onFilterChanged(filter: String) {
        _selectedFilter.value = filter
        applyFiltersAndSearch()  // ← Xử lý logic ở đây
    }
    
    fun onSearchChanged(query: String) {
        _searchText.value = query
        applyFiltersAndSearch()  // ← Xử lý logic ở đây
    }
    
    // ============ BUSINESS LOGIC (Xử lý filter & search) ============
    private fun applyFiltersAndSearch() {
        val filtered = getFilteredProducts()
        val searched = searchProducts(_searchText.value)
        
        _products.value = if (_searchText.value.isEmpty()) {
            filtered  // Chỉ dùng filter
        } else {
            // Kết hợp filter AND search
            filtered.filter { product ->
                searched.any { it.id == product.id }
            }
        }
    }
    
    private fun getFilteredProducts(): List<AdminProduct> {
        return when (_selectedFilter.value) {
            "IN STOCK" -> repository.adminProducts.value
                .filter { it.stockStatus == IN_STOCK }
            "LOW STOCK" -> repository.adminProducts.value
                .filter { it.stockStatus == LOW_STOCK }
            "OUT OF STOCK" -> repository.adminProducts.value
                .filter { it.stockStatus == OUT_OF_STOCK }
            else -> repository.adminProducts.value  // ALL
        }
    }
    
    private fun searchProducts(query: String): List<AdminProduct> {
        return if (query.isEmpty()) {
            repository.adminProducts.value
        } else {
            repository.adminProducts.value.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
        }
    }
}
```

---

## Data Flow - Sau Sửa

### Scenario 1: User Click Filter

```
1. User click "IN STOCK" chip
   ↓
2. UI: AdminFilterChips.onFilterSelected("IN STOCK")
   ↓
3. AdminProductListScreen:
   viewModel.onFilterChanged("IN STOCK")
   ↓
4. ViewModel.onFilterChanged():
   _selectedFilter.value = "IN STOCK"
   applyFiltersAndSearch()  ← Xử lý logic HERE (không gọi repo)
   ↓
5. ViewModel.getFilteredProducts():
   repository.adminProducts.value.filter { it.status == IN_STOCK }
   ↓
6. _products.value = [filtered products]
   ↓
7. UI recompose → LazyVerticalGrid hiển thị sản phẩm mới
```

### Scenario 2: User Type Search

```
1. User type "nike"
   ↓
2. UI: AdminSearchBar.onSearchChanged("nike")
   ↓
3. AdminProductListScreen:
   viewModel.onSearchChanged("nike")
   ↓
4. ViewModel.onSearchChanged():
   _searchText.value = "nike"
   applyFiltersAndSearch()  ← Xử lý logic HERE
   ↓
5. ViewModel.searchProducts():
   repository.adminProducts.value.filter { it.name.contains("nike") }
   ↓
6. Kết hợp với filter hiện tại
   _products.value = [filter results] ∩ [search results]
   ↓
7. UI recompose
```

---

## Lợi Ích Của Cấu Trúc Đúng

| Lợi Ích | Giải Thích |
|---------|-----------|
| **Separation of Concerns** | Repository lo data, ViewModel lo logic, UI lo hiển thị |
| **Dễ test** | Có thể test ViewModel riêng mà không cần Repository |
| **Dễ maintain** | Thay đổi logic → chỉ cần sửa ViewModel |
| **Dễ mở rộng** | Thêm filter/search mới chỉ cần sửa ViewModel |
| **Reusability** | Repository có thể dùng cho nhiều ViewModel |
| **Scalability** | Nếu thêm logic phức tạp → không ảnh hưởng Repository |

---

## Ví Dụ: Thêm Filter Mới

**Scenario:** Thêm filter theo price range (0-100, 100-200, etc)

### Với Cấu Trúc Sai (Repository làm)
- Sửa Repository → thêm hàm mới
- Sửa ViewModel → gọi hàm mới
- 👎 Repository biết quá nhiều logic

### Với Cấu Trúc Đúng (ViewModel làm) ✅
- Sửa ViewModel → thêm filter logic
- Repository không thay đổi
- 👍 Repository vẫn chỉ cung cấp raw data

```kotlin
// Thêm trong ViewModel
private val _priceRange = MutableStateFlow(0f..200f)

fun onPriceRangeChanged(min: Float, max: Float) {
    _priceRange.value = min..max
    applyFiltersAndSearch()  // ← Update lại danh sách
}

private fun getFilteredProducts(): List<AdminProduct> {
    var result = repository.adminProducts.value
    
    // Apply stock filter
    result = when (_selectedFilter.value) {
        "IN STOCK" -> result.filter { it.stockStatus == IN_STOCK }
        // ...
    }
    
    // Apply price filter (NEW)
    result = result.filter { 
        it.price >= _priceRange.value.start && 
        it.price <= _priceRange.value.endInclusive 
    }
    
    return result
}
```

---

## Tóm Tắt

| Layer | Trách Nhiệm | Ví Dụ |
|-------|-----------|--------|
| **Repository** | Cung cấp raw data | `adminProducts: StateFlow` |
| **ViewModel** | Xử lý logic + state | `getFilteredProducts()`, `_selectedFilter` |
| **UI** | Hiển thị + capture input | `AdminProductListScreen`, click handler |

**Quy tắc vàng:** ViewModel là "chủ tế" - ViewModel quyết định logic, Repository chỉ cung cấp data.

---

## Files Đã Sửa

✅ `AdminProductRepository.kt` - Xoá logic filter & search  
✅ `AdminProductListViewModel.kt` - Thêm logic filter & search

Giờ cấu trúc MVVM đã chính xác! 🎉

