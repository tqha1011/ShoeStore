# 📐 Refactoring State Management - Component vs ViewModel

## 🎯 Vấn Đề Ban Đầu

### ❌ **Sai: State ở Component**

```kotlin
@Composable
fun ExpandableSection(...) {
    // ❌ Sai: Component tự quản lý state
    var isExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.clickable { isExpanded = !isExpanded }
    ) {
        // ...
        if (isExpanded) { ... }
    }
}

// Sử dụng
ExpandableSection(
    title = "Shipping",
    content = "..."
    // ❌ Không thể truyền state từ ngoài
)
```

**Tại sao sai?**
- Component tự quản lý state → Violate Separation of Concerns
- Khó test, khó reuse
- Nếu muốn share state với component khác → không thể
- State bị destroy khi component recompose

---

### ✅ **Đúng: State ở ViewModel**

```kotlin
// ViewModel quản lý state
class ExpandableDetailViewModel {
    private val _isShippingExpanded = MutableStateFlow(false)
    val isShippingExpanded: StateFlow<Boolean> = _isShippingExpanded.asStateFlow()
    
    fun toggleShipping() {
        _isShippingExpanded.value = !_isShippingExpanded.value
    }
}

// Component chỉ hiển thị (Presentational)
@Composable
fun ExpandableSection(
    title: String,
    content: String,
    isExpanded: Boolean,              // ✅ Nhận từ props
    onExpandedChange: (Boolean) -> Unit // ✅ Callback
) {
    Column(
        modifier = Modifier.clickable { onExpandedChange(!isExpanded) }
    ) {
        // ...
        if (isExpanded) { ... }
    }
}

// Sử dụng
ExpandableSection(
    title = "Shipping",
    content = "...",
    isExpanded = isShippingExpanded.value,  // ✅ Từ ViewModel
    onExpandedChange = { viewModel.toggleShipping() }
)
```

---

## 📊 So Sánh Chi Tiết

| Tiêu Chí | Component Quản Lý | ViewModel Quản Lý |
|---------|-----------------|------------------|
| **Khi recompose** | State bị reset | State được giữ lại |
| **Chia sẻ state** | ❌ Khó | ✅ Dễ (tất cả component dùng 1 ViewModel) |
| **Test** | ❌ Khó test | ✅ Dễ test (có thể mock ViewModel) |
| **Maintain** | ❌ Logic rải rác | ✅ Logic tập trung ở ViewModel |
| **Scope** | Chỉ local | Persist qua lifecycle |

---

## 🔄 Các Loại State Cần Quản Lý

### **1. UI State** (Nên ở ViewModel) ✅
```kotlin
selectedFilter: String         // Nên ở ViewModel
searchText: String             // Nên ở ViewModel
isLoading: Boolean             // Nên ở ViewModel
productList: List<Product>     // Nên ở ViewModel
isExpanded: Boolean            // Nên ở ViewModel
```

**Lý do:** Cần persist khi recompose, có thể share, cần test

---

### **2. UI-Only State** (Có thể ở Component) ⚠️
```kotlin
selectedBottomTab: BottomNavTab  // OK ở Component
// Lý do: Chỉ dùng cho navigation local, không cần persist
```

**Khi nào OK ở Component?**
- Chỉ affect UI nhỏ (animation, dropdown open/close)
- Không chia sẻ với component khác
- Không cần test

**Khi nào phải ở ViewModel?**
- Affect multiple components
- Cần persist qua recompose
- Cần test
- Cần share với component khác

---

## ✅ Files Đã Refactor

### **1. ExpandableSection.kt** 

**Trước:**
```kotlin
var isExpanded by remember { mutableStateOf(false) }  // ❌ Local state
```

**Sau:**
```kotlin
isExpanded: Boolean,                    // ✅ Props
onExpandedChange: (Boolean) -> Unit     // ✅ Callback
```

---

### **2. ProductListScreen.kt**

**Trước:**
```kotlin
var selectedFilter by remember { mutableStateOf("All Shoes") }  // ❌
var searchText by remember { mutableStateOf("") }              // ❌
```

**Sau:**
```kotlin
val selectedFilter = viewModel.selectedFilter.collectAsState()  // ✅
val searchText = viewModel.searchText.collectAsState()          // ✅
```

---

### **3. ProductListViewModel.kt** (Mới tạo)

```kotlin
private val _selectedFilter = MutableStateFlow("All Shoes")     // ✅
val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

private val _searchText = MutableStateFlow("")                  // ✅
val searchText: StateFlow<String> = _searchText.asStateFlow()

fun onFilterSelected(filter: String) { ... }                    // ✅
fun onSearchChanged(query: String) { ... }                      // ✅
```

---

## 🏗️ Kiến Trúc MVVM Chuẩn

```
┌────────────────────────────────────┐
│       UI Layer                     │
│  ProductListScreen                 │
│  ├─ ExpandableSection              │
│  ├─ SearchBar                      │
│  ├─ FilterChips                    │
│  └─ ProductCard                    │
│                                    │
│  ✅ Chỉ nhận state từ props        │
│  ✅ Gọi callback khi có sự kiện   │
└──────────┬─────────────────────────┘
           │ collectAsState()
           ↓
┌────────────────────────────────────┐
│     ViewModel Layer                │
│  ProductListViewModel              │
│  ├─ State:                         │
│  │  ├─ selectedFilter              │
│  │  ├─ searchText                  │
│  │  ├─ products                    │
│  │  └─ isLoading                   │
│  │                                 │
│  ├─ Callbacks:                     │
│  │  ├─ onFilterSelected()          │
│  │  ├─ onSearchChanged()           │
│  │  └─ toggleFavorite()            │
│  │                                 │
│  └─ Logic:                         │
│     ├─ getFilteredProducts()       │
│     ├─ searchProducts()            │
│     └─ applyFiltersAndSearch()     │
└──────────┬─────────────────────────┘
           │
           ↓ calls
┌────────────────────────────────────┐
│    Repository Layer                │
│  ProductRepository                 │
│  └─ getAllProducts()               │
└────────────────────────────────────┘
```

---

## 📋 Checklist

### State Management

- [x] ✅ ExpandableSection - Move state sang ViewModel
- [x] ✅ ProductListScreen - Move filter & search sang ViewModel
- [ ] ⏳ SignInScreen - Check state (isVisible)
- [ ] ⏳ SignUpScreen - Check state (isVisible)
- [ ] ⏳ WelcomeScreen - Check state (hasNavigated)

### Naming Convention

- [x] ✅ Presentational component - nhận state via props
- [x] ✅ ViewModel - quản lý state
- [x] ✅ Callback naming - onEvent() (onFilterSelected, onSearchChanged)

---

## 🎓 Quy Tắc Vàng

> **"Components are stupid, ViewModels are smart"**

- **Component:** Chỉ biết cách vẽ UI
- **ViewModel:** Biết logic và quản lý state
- **Repository:** Cung cấp data

---

## 🚀 Next Steps

1. Refactor SignInScreen & SignUpScreen
2. Refactor WelcomeScreen
3. Check tất cả các Screens có state cần move sang ViewModel
4. Setup ViewModel Factory cho dependency injection

