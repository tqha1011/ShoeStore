# Hướng dẫn Di chuyển Cấu trúc Thư mục (Product → User/Product)

## 🎯 Mục tiêu
Tổ chức lại codebase để phân biệt rõ ràng giữa:
- **User Features** (features/user/): Tính năng cho người dùng
- **Admin Features** (features/admin/): Tính năng cho quản trị viên (sẽ tạo sau)

---

## 📂 Cấu trúc Cũ (Trước)
```
features/
└── product/
    ├── data/
    │   ├── models/
    │   │   └── Product.kt
    │   └── repositories/
    │       └── ProductRepository.kt
    ├── ui/
    │   ├── components/
    │   ├── product_detail/
    │   └── product_list/
    └── viewmodel/
        ├── ProductDetailViewModel.kt
        └── ProductListViewModel.kt
```

---

## 📂 Cấu trúc Mới (Sau)
```
features/
├── user/
│   └── product/
│       ├── data/
│       │   ├── models/
│       │   │   └── Product.kt
│       │   └── repositories/
│       │       └── ProductRepository.kt
│       ├── ui/
│       │   ├── components/
│       │   │   ├── ProductCard.kt
│       │   │   ├── TopAppBar.kt
│       │   │   ├── BottomNavBar.kt
│       │   │   ├── SearchBar.kt
│       │   │   ├── FilterChips.kt
│       │   │   ├── SizeSelector.kt
│       │   │   ├── ProductHeaderInfo.kt
│       │   │   ├── ProductHeroImage.kt
│       │   │   ├── ActionButtonsSection.kt
│       │   │   ├── ExpandableSection.kt
│       │   │   └── ProductImage.kt
│       │   ├── product_detail/
│       │   │   └── ProductDetailScreen.kt
│       │   └── product_list/
│       │       └── ProductListScreen.kt
│       └── viewmodel/
│           ├── ProductDetailViewModel.kt
│           └── ProductListViewModel.kt
└── admin/
    └── (Sẽ tạo sau cho Admin features)
```

---

## 📦 Thay đổi Package Names

### Cũ
```kotlin
package com.example.shoestoreapp.features.product.*
package com.example.shoestoreapp.features.product.data.models
package com.example.shoestoreapp.features.product.data.repositories
package com.example.shoestoreapp.features.product.ui.components
package com.example.shoestoreapp.features.product.ui.product_list
package com.example.shoestoreapp.features.product.ui.product_detail
package com.example.shoestoreapp.features.product.viewmodel
```

### Mới
```kotlin
package com.example.shoestoreapp.features.user.product.*
package com.example.shoestoreapp.features.user.product.data.models
package com.example.shoestoreapp.features.user.product.data.repositories
package com.example.shoestoreapp.features.user.product.ui.components
package com.example.shoestoreapp.features.user.product.ui.product_list
package com.example.shoestoreapp.features.user.product.ui.product_detail
package com.example.shoestoreapp.features.user.product.viewmodel
```

---

## 🔄 File Được Cập nhật

### 1. MainActivity.kt
**Thay đổi:** Cập nhật imports
```kotlin
// Cũ
import com.example.shoestoreapp.features.product.ui.product_detail.ProductDetailScreen
import com.example.shoestoreapp.features.product.ui.product_list.ProductListScreen
import com.example.shoestoreapp.features.product.viewmodel.ProductDetailViewModel
import com.example.shoestoreapp.features.product.viewmodel.ProductListViewModel

// Mới
import com.example.shoestoreapp.features.user.product.ui.product_detail.ProductDetailScreen
import com.example.shoestoreapp.features.user.product.ui.product_list.ProductListScreen
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel
import com.example.shoestoreapp.features.user.product.viewmodel.ProductListViewModel
```

---

## 📋 Danh sách File Được Tạo Mới

### Data Layer
- `ProductRepository.kt` → `features/user/product/data/repositories/`
- `Product.kt` → `features/user/product/data/models/`

### ViewModel Layer
- `ProductListViewModel.kt` → `features/user/product/viewmodel/`
- `ProductDetailViewModel.kt` → `features/user/product/viewmodel/`

### UI - Components
- `ProductCard.kt` → `features/user/product/ui/components/`
- `TopAppBar.kt` → `features/user/product/ui/components/`
- `BottomNavBar.kt` → `features/user/product/ui/components/`
- `SearchBar.kt` → `features/user/product/ui/components/`
- `FilterChips.kt` → `features/user/product/ui/components/`
- `SizeSelector.kt` → `features/user/product/ui/components/`
- `ProductHeaderInfo.kt` → `features/user/product/ui/components/`
- `ProductHeroImage.kt` → `features/user/product/ui/components/`
- `ActionButtonsSection.kt` → `features/user/product/ui/components/`
- `ExpandableSection.kt` → `features/user/product/ui/components/`
- `ProductImage.kt` → `features/user/product/ui/components/`

### UI - Screens
- `ProductListScreen.kt` → `features/user/product/ui/product_list/`
- `ProductDetailScreen.kt` → `features/user/product/ui/product_detail/`

---

## ✅ Các bước đã hoàn thành

1. ✅ Tạo cấu trúc thư mục mới: `features/user/product/`
2. ✅ Copy tất cả file từ `features/product/` → `features/user/product/`
3. ✅ Cập nhật package names trong tất cả file Kotlin
4. ✅ Cập nhật tất cả imports giữa các file
5. ✅ Cập nhật imports trong MainActivity.kt

---

## 🚀 Bước tiếp theo

### 1. Xóa cấu trúc cũ (optional - nếu bạn chắc chắn)
```
features/product/  ← Xóa thư mục này hoàn toàn
```

### 2. Tạo Admin Features (sau này)
```
features/
└── admin/
    └── product/
        ├── data/
        ├── ui/
        └── viewmodel/
```

### 3. Build & Test
- Nhấn `Ctrl+B` để build project
- Kiểm tra xem có lỗi import không
- Chạy app để test functionality

---

## 📝 Lợi ích của cấu trúc mới

1. **Rõ ràng**: Dễ hiểu đây là features cho User hoặc Admin
2. **Scalable**: Dễ mở rộng khi thêm Admin features
3. **Maintenance**: Dễ bảo trì vì code được tổ chức tốt
4. **Team Development**: Team members hiểu rõ scope của từng features
5. **Future-proof**: Dễ tách riêng thành modules độc lập

---

## ⚠️ Lưu ý quan trọng

- Nếu có file cũ còn tham chiếu đến `features.product`, bạn cần cập nhật import
- Kiểm tra lại `gradle` dependencies nếu có
- Test app sau khi build để đảm bảo không có runtime errors
- Có thể xóa folder `features/product/` cũ nếu chắc chắn không cần dùng

---

**Hoàn thành ngày:** 2026-03-29
**Trạng thái:** ✅ Hoàn tất

