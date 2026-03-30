# ✅ Option 1 - Fixed Architecture

## 🎯 Vấn Đề Ban Đầu

```
❌ TRÙNG LẶP & NHẦM LẪN:
┌─────────────────────┐         ┌──────────────────────┐
│   Repository        │         │    ViewModel         │
│                     │         │                      │
│ _productList        │         │ _products            │
│ toggleFavorite()    │         │ toggleFavorite()     │
│                     │         │ applyFiltersAndSearch()
│ Quản lý state UI    │         │ Xử lý logic          │
│ Xử lý business logic│         │                      │
└─────────────────────┘         └──────────────────────┘
      ❌ SAI                           ❌ SAI
   (Data layer)                   (Business layer)
```

**Kết quả:** Có 2 danh sách products độc lập → Data không đồng bộ

---

## ✅ Giải Pháp - Option 1

```
✅ ĐÚNG MVVM:
┌─────────────────────────────────────────────────────────┐
│                  UI Layer                               │
│  ProductListScreen, ProductCard, SearchBar, FilterChips │
│  (Chỉ hiển thị UI, nhận state từ ViewModel)             │
└────────────┬────────────────────────────────────────────┘
             │ collectAsState()
             ↓
┌─────────────────────────────────────────────────────────┐
│              ViewModel Layer ✅ QUẢN LÝ STATE            │
│                                                         │
│  ProductListViewModel:                                  │
│  ├─ _products: MutableStateFlow (state UI)             │
│  ├─ _selectedFilter: MutableStateFlow                  │
│  ├─ _searchText: MutableStateFlow                      │
│  │                                                      │
│  ├─ toggleFavorite(productId)                          │
│  │  ├─ ① Update local state: _products.value          │
│  │  └─ ② Call repo: updateFavoriteToAPI()             │
│  │                                                      │
│  ├─ onFilterSelected(filter)                           │
│  ├─ onSearchChanged(query)                             │
│  └─ applyFiltersAndSearch()                            │
└────────────┬────────────────────────────────────────────┘
             │
             ↓ calls
┌─────────────────────────────────────────────────────────┐
│              Repository Layer ✅ CẦP PHÁT DATA           │
│                                                         │
│  ProductRepository:                                     │
│  ├─ getAllProducts() → StateFlow (raw data)            │
│  ├─ updateFavoriteToAPI(productId, isFavorite)        │
│  └─ addToCart(productId)                               │
│                                                         │
│  Chỉ cung cấp dữ liệu, không xử lý logic               │
└─────────────────────────────────────────────────────────┘
```

---

## 📋 Chi Tiết Từng Layer

### **1. Repository - Cung Cấp Data**

```kotlin
class ProductRepository {
    // ✅ Chỉ có hàm cung cấp data
    fun getAllProducts(): StateFlow<List<Product>> {
        return MutableStateFlow(mockProducts).asStateFlow()
    }
    
    // ✅ Chỉ có hàm API (không xử lý state UI)
    fun updateFavoriteToAPI(productId: Int, isFavorite: Boolean) {
        println("Saving favorite status: product=$productId, isFavorite=$isFavorite")
    }
    
    fun addToCart(productId: Int) {
        println("Added product $productId to cart")
    }
    
    // ❌ Xoá: toggleFavorite() - không nên ở Repository
    // ❌ Xoá: _productList state - không nên quản lý state UI
}
```

---

### **2. ViewModel - Quản Lý State & Logic**

```kotlin
class ProductListViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    
    // ✅ Quản lý tất cả state UI
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("All Shoes")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    // ✅ Quản lý logic toggle favorite
    fun toggleFavorite(productId: Int) {
        // ① Update local state immediately
        val currentProducts = _products.value
        val updatedProducts = currentProducts.map { product ->
            if (product.id == productId) {
                product.copy(isFavorite = !product.isFavorite)
            } else {
                product
            }
        }
        _products.value = updatedProducts
        
        // ② Sau khi update state → gọi API để lưu
        viewModelScope.launch {
            val updatedProduct = updatedProducts.find { it.id == productId }
            if (updatedProduct != null) {
                repository.updateFavoriteToAPI(productId, updatedProduct.isFavorite)
            }
        }
    }
    
    // ✅ Xử lý filter & search logic
    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        applyFiltersAndSearch()
    }
    
    fun onSearchChanged(query: String) {
        _searchText.value = query
        applyFiltersAndSearch()
    }
    
    private fun applyFiltersAndSearch() { ... }
}
```

---

## 🔄 Data Flow - Ví Dụ Thực Tế

### **Scenario: User Click Heart Icon**

```
1. User click ❤️ icon trên sản phẩm ID=1
   ↓
2. ProductCard.onFavoriteClick(1)
   ↓
3. ProductListScreen: viewModel.toggleFavorite(1)
   ↓
4. ViewModel.toggleFavorite(1):
   
   ① Update local state (instant):
      _products.value = [
          Product(id=1, isFavorite=false → true) ✅ Updated locally
          Product(id=2, isFavorite=true),
          ...
      ]
   
   ② Launch coroutine:
      repository.updateFavoriteToAPI(1, true)
         ↓ (network call)
         → API/Server được cập nhật
   ↓
5. _products.value thay đổi
   ↓
6. collectAsState() nhận thông báo
   ↓
7. UI recompose → Heart icon hiện ❤️ đỏ (filled)
```

---

## 📊 So Sánh Trước & Sau

| Aspect | ❌ Trước | ✅ Sau |
|--------|---------|--------|
| **State Location** | Repository + ViewModel (trùng) | ViewModel (tập trung) |
| **toggleFavorite** | Repository (sai) | ViewModel (đúng) |
| **Filter/Search** | ViewModel | ViewModel |
| **API Calls** | Repository | ViewModel → Repository |
| **Data Sync** | ❌ 2 list độc lập | ✅ 1 list duy nhất |
| **Maintainability** | ❌ Logic rải rác | ✅ Logic tập trung |
| **Testability** | ❌ Khó test | ✅ Dễ test |

---

## 🎯 Quy Tắc Vàng - Option 1

```
┌─────────────────────────────────────────────────────┐
│  Repository: Only Data Provider (CRUD + API)       │
├─────────────────────────────────────────────────────┤
│  ✅ Cung cấp raw data                               │
│  ✅ Gọi API endpoints                               │
│  ❌ Không quản lý state UI                          │
│  ❌ Không xử lý business logic                      │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  ViewModel: State Manager + Business Logic         │
├─────────────────────────────────────────────────────┤
│  ✅ Quản lý state UI (products, filter, search)    │
│  ✅ Xử lý logic (filter, search, toggle)           │
│  ✅ Gọi repository khi cần save/load               │
│  ❌ Không tạo Repository (inject instead)          │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  UI: Display Only (Presentational)                 │
├─────────────────────────────────────────────────────┤
│  ✅ Hiển thị data từ ViewModel                      │
│  ✅ Gọi callback khi user interact                 │
│  ❌ Không quản lý state                             │
│  ❌ Không gọi API trực tiếp                        │
└─────────────────────────────────────────────────────┘
```

---

## ✅ Files Đã Fix

### **1. ProductRepository.kt**
```
❌ Xoá: _productList state
❌ Xoá: toggleFavorite() method
✅ Giữ: getAllProducts() - cung cấp data
✅ Giữ: updateFavoriteToAPI() - gọi API
✅ Giữ: addToCart() - gọi API
```

### **2. ProductListViewModel.kt**
```
✅ Giữ: _products state (quản lý state UI)
✅ Thêm: logic save API trong toggleFavorite()
✅ Giữ: toggleFavorite() (xử lý logic)
✅ Giữ: applyFiltersAndSearch() (xử lý logic)
```

---

## 🚀 Lợi Ích

✅ **Single Source of Truth** - 1 state duy nhất (_products trong ViewModel)  
✅ **Clear Responsibility** - Mỗi layer 1 trách nhiệm  
✅ **Easy to Test** - Test ViewModel mà không cần Repository  
✅ **Easy to Maintain** - Logic tập trung ở ViewModel  
✅ **Optimistic Update** - Update UI instantly, save to server async  
✅ **Follows MVVM** - Tuân theo pattern chuẩn

---

## 💡 Optimistic Update Pattern

Trong `toggleFavorite()`:
```
① Update local state ngay → UI responsive
   _products.value = updated

② Gọi API save to server async
   repository.updateFavoriteToAPI()

③ Nếu API fail → có thể rollback
   (implement error handling sau)
```

Điều này tạo ra **user experience tốt** - UI cập nhật immediately, không cần chờ API response.

