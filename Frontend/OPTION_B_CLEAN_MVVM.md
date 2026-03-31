# ✅ Option B - Fixed Architecture (Chuẩn MVVM)

## 🎯 Cấu Trúc Mới - Option B

```
┌─────────────────────────────────────────┐
│          UI Layer                       │
│  ProductListScreen                      │
│  ├─ ProductCard                         │
│  ├─ SearchBar                           │
│  └─ FilterChips                         │
│                                         │
│  Chỉ nhận state từ ViewModel            │
│  Gọi callback khi user interact        │
└────────────┬────────────────────────────┘
             │ collectAsState()
             ↓
┌─────────────────────────────────────────┐
│    ViewModel Layer ✅ QUẢN LÝ STATE      │
│                                         │
│  ProductListViewModel:                  │
│  ├─ _products: MutableStateFlow         │
│  ├─ _selectedFilter: MutableStateFlow   │
│  ├─ _searchText: MutableStateFlow       │
│  │                                      │
│  ├─ toggleFavorite(productId)           │
│  │  ① Gọi: repository.toggleFavorite()  │
│  │  ② Update: _products.value           │
│  │                                      │
│  ├─ onFilterSelected()                  │
│  ├─ onSearchChanged()                   │
│  └─ applyFiltersAndSearch()             │
│                                         │
│  Quản lý state UI + gọi repository      │
└────────────┬────────────────────────────┘
             │
             ↓ calls
┌─────────────────────────────────────────┐
│  Repository Layer ✅ CẶP PHÁT & LOGIC    │
│                                         │
│  ProductRepository:                     │
│  ├─ getAllProducts()                    │
│  │  └─ Cung cấp raw data                │
│  │                                      │
│  ├─ toggleFavorite(productId)           │
│  │  ① Đảo isFavorite (data logic)      │
│  │  ② Gọi API save                      │
│  │  ③ Return Product updated            │
│  │                                      │
│  ├─ updateFavoriteToAPI()               │
│  └─ addToCart()                         │
│                                         │
│  Cung cấp data + data logic             │
└─────────────────────────────────────────┘
```

---

## 📊 Chi Tiết Implementation

### **Repository - Data Layer**

```kotlin
class ProductRepository {
    
    // ✅ Cung cấp raw data
    fun getAllProducts(): StateFlow<List<Product>> {
        return MutableStateFlow(mockProducts).asStateFlow()
    }
    
    // ✅ Data Logic: Đảo isFavorite + gọi API
    fun toggleFavorite(productId: Int): Product? {
        val currentList = getAllProducts().value
        val product = currentList.find { it.id == productId }
        
        return product?.copy(isFavorite = !product.isFavorite).also { updatedProduct ->
            if (updatedProduct != null) {
                // Gọi API để lưu
                updateFavoriteToAPI(productId, updatedProduct.isFavorite)
            }
        }
    }
    
    // ✅ API methods
    fun updateFavoriteToAPI(productId: Int, isFavorite: Boolean) {
        println("Saving favorite: product=$productId, isFavorite=$isFavorite")
    }
    
    fun addToCart(productId: Int) {
        println("Added to cart: $productId")
    }
}
```

**Chức năng:**
- ✅ Cung cấp raw data (`getAllProducts()`)
- ✅ Xử lý data logic (`toggleFavorite()` - đảo value)
- ✅ Gọi API (`updateFavoriteToAPI()`)
- ❌ **Không** quản lý state UI

---

### **ViewModel - Business Logic Layer**

```kotlin
class ProductListViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    
    // ✅ Quản lý state UI
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _products.asStateFlow()
    
    // ✅ Gọi repository để toggle, rồi update state
    fun toggleFavorite(productId: Int) {
        // ① Gọi repository xử lý data logic
        val updatedProduct = repository.toggleFavorite(productId)
        
        // ② Update state UI local
        if (updatedProduct != null) {
            val updatedProducts = _products.value.map { product ->
                if (product.id == productId) updatedProduct else product
            }
            _products.value = updatedProducts  // ← UI cập nhật
        }
    }
    
    // ✅ Xử lý filter & search
    fun onFilterSelected(filter: String) { ... }
    fun onSearchChanged(query: String) { ... }
    private fun applyFiltersAndSearch() { ... }
}
```

**Chức năng:**
- ✅ Quản lý state UI (_products, _selectedFilter, _searchText)
- ✅ Gọi repository để xử lý data logic
- ✅ Cập nhật state dựa trên kết quả từ repository

---

## 🔄 Data Flow - User Click Heart Icon

### **Step by Step**

```
1️⃣ User click ❤️ on Product(id=1)
   ↓
2️⃣ ProductCard.onFavoriteClick(1)
   ↓
3️⃣ ProductListScreen: viewModel.toggleFavorite(1)
   ↓
4️⃣ ViewModel.toggleFavorite(1):
   
   updatedProduct = repository.toggleFavorite(1)
                        ↓
   5️⃣ Repository.toggleFavorite(1):
       val product = currentList.find { it.id == 1 }
           = Product(id=1, isFavorite=false)
       
       return product.copy(isFavorite=true)  ← Đảo value
               .also { updatedProduct →
                   updateFavoriteToAPI(1, true)  ← Gọi API
               }
       
       return Product(id=1, isFavorite=true)  ← Trả về
   
   ↓ (back to ViewModel)
   
   if (updatedProduct != null) {
       _products.value = updatedProducts  ← Update state UI
   }
   ↓
6️⃣ _products.value thay đổi
   ↓
7️⃣ collectAsState() nhận thông báo
   ↓
8️⃣ UI recompose → ProductCard hiển thị ❤️ đỏ
```

---

## 📋 Trách Nhiệm Mỗi Layer

| Layer | Trách Nhiệm | Ví Dụ |
|-------|-----------|--------|
| **Repository** | Xử lý data logic + API | `toggleFavorite()` - đảo value |
| **ViewModel** | Quản lý state UI + gọi repo | Gọi `repository.toggleFavorite()`, update `_products` |
| **UI** | Hiển thị + capture event | Gọi `viewModel.toggleFavorite()` |

---

## ✅ Lợi Ích Option B

| Lợi Ích | Chi Tiết |
|---------|---------|
| **Reusability** | ✅ Nhiều ViewModel có thể dùng `repository.toggleFavorite()` |
| **Testability** | ✅ Test Repository mà không cần ViewModel |
| **Separation** | ✅ Clear: Repo = Data Logic, ViewModel = UI Logic |
| **Maintainability** | ✅ Thay đổi logic data → sửa 1 chỗ |
| **Single Responsibility** | ✅ Mỗi class 1 trách nhiệm rõ ràng |

---

## 🎯 Quy Tắc Vàng - Option B

```
┌─────────────────────────────────────────┐
│  Repository (Data Logic)                │
├─────────────────────────────────────────┤
│  ✅ Cung cấp raw data (getAllProducts) │
│  ✅ Xử lý data logic (toggleFavorite)  │
│  ✅ Gọi API (updateFavoriteToAPI)      │
│  ✅ Return kết quả để ViewModel dùng   │
│  ❌ Không quản lý state UI              │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  ViewModel (Business Logic + UI State)  │
├─────────────────────────────────────────┤
│  ✅ Quản lý state UI (_products, ...)  │
│  ✅ Gọi repository để xử lý data       │
│  ✅ Update state dựa trên kết quả repo │
│  ✅ Gọi business logic khác (filter)   │
│  ❌ Không tạo Repository mới            │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  UI (Presentational)                    │
├─────────────────────────────────────────┤
│  ✅ Hiển thị state từ ViewModel        │
│  ✅ Gọi callback (onFavoriteClick)     │
│  ❌ Không xử lý logic                   │
│  ❌ Không tạo/gọi Repository            │
└─────────────────────────────────────────┘
```

---

## 🔍 So Sánh Option A vs Option B

| Aspect | Option A | Option B (✅ Chuẩn) |
|--------|----------|---|
| **toggleFavorite** | ViewModel | **Repository** |
| **Data Logic** | ViewModel | **Repository** |
| **Reusability** | ❌ Khó | **✅ Dễ** |
| **Testability** | ❌ Cần mock ViewModel | **✅ Test Repository độc lập** |
| **Separation** | ❌ ViewModel làm quá nhiều | **✅ Clear responsibility** |
| **MVVM Pattern** | ❌ Sai | **✅ Đúng** |

---

## 📝 Pattern: Smart Repository + Dumb ViewModel

```
┌─────────────────────────────────────┐
│  Repository = Smart                 │
│  Biết cách xử lý dữ liệu           │
│  Trả về kết quả (không quản lý UI) │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  ViewModel = Dumb (Chỉ update state)│
│  Gọi repository                     │
│  Update state dựa trên kết quả      │
└─────────────────────────────────────┘
```

Điều này tạo ra **clean architecture** - easy to test, easy to maintain.

---

## ✅ Files Đã Fix

### **1. ProductRepository.kt**
```
✅ Thêm: toggleFavorite(productId): Product?
   - Xử lý data logic (đảo isFavorite)
   - Gọi API save
   - Return sản phẩm updated
```

### **2. ProductListViewModel.kt**
```
✅ Sửa: toggleFavorite(productId)
   - Gọi repository.toggleFavorite()
   - Update state UI (_products.value)
```

---

## 🚀 Pattern Này Thường Dùng Khi

- ✅ Logic data có thể được reuse ở nhiều chỗ
- ✅ Muốn test logic data riêng
- ✅ Repository quản lý complexity, ViewModel chỉ manage state
- ✅ Cần scalable, maintainable architecture

**Option B là chuẩn MVVM nhất!** 🎉

