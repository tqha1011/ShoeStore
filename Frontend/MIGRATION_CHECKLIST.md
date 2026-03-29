# 📋 Danh sách công việc (Checklist) - Di chuyển Cấu trúc

## ✅ Đã hoàn thành

### Tạo cấu trúc mới
- [x] Tạo thư mục `features/user/product/`
- [x] Tạo `data/models/` package
- [x] Tạo `data/repositories/` package
- [x] Tạo `ui/components/` package
- [x] Tạo `ui/product_list/` package
- [x] Tạo `ui/product_detail/` package
- [x] Tạo `viewmodel/` package

### Copy và tạo file mới
- [x] `Product.kt` (data model)
- [x] `ProductRepository.kt` (data layer)
- [x] `ProductListViewModel.kt` (viewmodel)
- [x] `ProductDetailViewModel.kt` (viewmodel)
- [x] `ProductCard.kt` (component)
- [x] `TopAppBar.kt` (component)
- [x] `BottomNavBar.kt` (component)
- [x] `SearchBar.kt` (component)
- [x] `FilterChips.kt` (component)
- [x] `SizeSelector.kt` (component)
- [x] `ProductHeaderInfo.kt` (component)
- [x] `ProductHeroImage.kt` (component)
- [x] `ActionButtonsSection.kt` (component)
- [x] `ExpandableSection.kt` (component)
- [x] `ProductImage.kt` (component)
- [x] `ProductListScreen.kt` (screen)
- [x] `ProductDetailScreen.kt` (screen)

### Cập nhật package names
- [x] Cập nhật tất cả package declarations
- [x] Cập nhật tất cả internal imports
- [x] Cập nhật imports ở MainActivity.kt

### Documentation
- [x] Tạo REFACTORING_STRUCTURE.md

---

## ⏳ Chưa hoàn thành (Next Steps)

### Build & Test
- [ ] Build project (Ctrl+B)
- [ ] Kiểm tra xem có compile errors không
- [ ] Run app trên emulator
- [ ] Test ProductListScreen
- [ ] Test ProductDetailScreen
- [ ] Test navigation

### Cleanup (Optional)
- [ ] Xóa thư mục `features/product/` cũ (nếu chắc chắn)
- [ ] Xóa các file cũ trong `features/product/`

### Git
- [ ] Commit thay đổi với message: "refactor: reorganize product features under user module"
- [ ] Push lên branch

---

## 🔍 Kiểm tra lại sau khi Build

### Error Resolution
Nếu có error, kiểm tra:

1. **Import errors**
   ```kotlin
   // Sai
   import com.example.shoestoreapp.features.product.ui.components.*
   
   // Đúng
   import com.example.shoestoreapp.features.user.product.ui.components.*
   ```

2. **ViewModel initialization**
   ```kotlin
   // Kiểm tra có cần import ProductListViewModel từ đúng package
   ProductListViewModel = remember { ProductListViewModel() }
   ```

3. **Navigation routes**
   ```kotlin
   // Kiểm tra MainActivity.kt navigation compose xài đúng Screen
   composable("product_list") {
       ProductListScreen(...)
   }
   ```

---

## 📝 File được tham chiếu có thể cần cập nhật

Nếu có file khác trong project tham chiếu đến `features.product`, hãy kiểm tra:
- [ ] `MainActivity.kt` - ✅ Đã cập nhật
- [ ] Các Activity khác (nếu có)
- [ ] Navigation graph (nếu có)
- [ ] build.gradle (nếu có references)
- [ ] Constants hoặc utils (nếu có)

---

## 🎯 Mục tiêu cuối cùng

Sau khi hoàn tất:
```
✅ Code được tổ chức tốt
✅ Package names phản ánh structure rõ ràng
✅ Dễ phân biệt User vs Admin features
✅ Sẵn sàng cho future expansion
✅ Team developers dễ hiểu codebase
```

---

## 💡 Ghi chú

- **Không xóa file cũ ngay**: Đợi đến khi chắc chắn cấu trúc mới hoạt động tốt
- **Keep a backup**: Nếu có git, hãy commit trước khi xóa
- **Test thoroughly**: Build, test trên emulator, kiểm tra navigation

---

**Last Updated:** 2026-03-29

