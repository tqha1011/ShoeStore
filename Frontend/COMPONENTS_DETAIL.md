# 🧩 Components Breakdown - Chi Tiết Từng Component

## 📌 Danh Sách Tất Cả Components

| Component | Tệp | Chức Năng |
|-----------|-----|---------|
| ProductHeroImage | ProductHeroImage.kt | Hiển thị ảnh sản phẩm |
| ProductHeaderInfo | ProductHeaderInfo.kt | Hiển thị tên, giá, rating |
| SizeSelector | SizeSelector.kt | Chọn kích thước giày |
| ActionButtonsSection | ActionButtonsSection.kt | Nút Add to Bag + Favorite |
| ExpandableSection | ExpandableSection.kt | Phần thông tin mở rộng |
| ProductDetailScreen | ProductDetailScreen.kt | Màn hình chính |

---

## 🖼️ ProductHeroImage

### Chức Năng
Hiển thị ảnh sản phẩm chiếm toàn bộ chiều rộng màn hình, tỷ lệ 1:1 (hình vuông).

### Code
```kotlin
@Composable
fun ProductHeroImage(
    imageUrl: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
)
```

### Parameter
- `imageUrl`: URL của ảnh (từ Repository)
- `contentDescription`: Mô tả ảnh (cho accessibility, screen readers)
- `modifier`: Custom styling (padding, margin, v.v.)

### Ví Dụ Sử Dụng
```kotlin
ProductHeroImage(
    imageUrl = product.imageUrl,           // URL từ database
    contentDescription = product.name      // "Nike Air Max 270"
)
```

### Màn Hình Hiển Thị
```
┌─────────────────────────┐
│                         │
│   [   Ảnh Sản Phẩm   ]   │  <- AspectRatio 1:1 (square)
│                         │
│                         │
└─────────────────────────┘
```

---

## 📝 ProductHeaderInfo

### Chức Năng
Hiển thị thông tin cơ bản sản phẩm: tên, giá, rating, loại sản phẩm.

### Code
```kotlin
@Composable
fun ProductHeaderInfo(
    name: String,
    price: Double,
    rating: Double,
    reviewCount: Int,
    productType: String = "Men's Shoes",
    modifier: Modifier = Modifier
)
```

### Parameter
- `name`: Tên sản phẩm (VD: "Nike Air Max 270")
- `price`: Giá (VD: 160.0 → "$160")
- `rating`: Đánh giá sao (VD: 4.8)
- `reviewCount`: Số reviews (VD: 128)
- `productType`: Loại giày (VD: "Men's Shoes", "Running Shoes")

### Ví Dụ Sử Dụng
```kotlin
ProductHeaderInfo(
    name = "Nike Air Max 270",
    price = 160.0,
    rating = 4.8,
    reviewCount = 128,
    productType = "Men's Shoes"
)
```

### Màn Hình Hiển Thị
```
Nike Air Max 270                           $160
Men's Shoes

⭐ 4.8 • 128 reviews
```

---

## 📏 SizeSelector

### Chức Năng
Cho phép user chọn kích thước giày (7, 8, 9, 10, 11).

### Code
```kotlin
@Composable
fun SizeSelector(
    selectedSize: Int?,
    onSizeSelected: (Int) -> Unit,
    onSizeGuideClick: () -> Unit = {},
    modifier: Modifier = Modifier
)
```

### Parameter
- `selectedSize`: Size hiện tại được chọn (null = không chọn)
- `onSizeSelected`: Callback khi user click size button
- `onSizeGuideClick`: Callback khi click "Size Guide"

### Ví Dụ Sử Dụng
```kotlin
SizeSelector(
    selectedSize = 8,  // User đã chọn size 8
    onSizeSelected = { size ->
        viewModel.selectSize(size)  // Update state
    },
    onSizeGuideClick = {
        // Navigate to Size Guide screen
    }
)
```

### Màn Hình Hiển Thị
```
SELECT SIZE            [📐 SIZE GUIDE]

┌───┬───┬───┬───┬───┐
│ 7 │ 8 │ 9 │10 │11 │  <- Size 8 được highlight (đen)
└───┴───┴───┴───┴───┘
```

### Style Khi Select/Unselect
```kotlin
// Unselected (mặc định)
border: 1.5.dp grayBorder
background: white
text: black

// Selected (user click)
border: 1.5.dp black
background: black
text: white
```

---

## 🛒 ActionButtonsSection

### Chức Năng
Hiển thị 2 nút: "Add to Bag" (nút chính) và "Favorite" (nút phụ).

### Code
```kotlin
@Composable
fun ActionButtonsSection(
    onAddToCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
)
```

### Parameter
- `onAddToCartClick`: Callback khi click "Add to Bag"
- `onFavoriteClick`: Callback khi click "Favorite"
- `isFavorite`: Trạng thái hiện tại yêu thích hay chưa

### Ví Dụ Sử Dụng
```kotlin
ActionButtonsSection(
    onAddToCartClick = {
        viewModel.addToCart(product.id)
        onNavigateToCart()  // Navigate to cart screen
    },
    onFavoriteClick = {
        viewModel.toggleFavorite(product.id)
    },
    isFavorite = product.isFavorite
)
```

### Màn Hình Hiển Thị
```
┌─────────────────────────┐
│  ADD TO BAG             │  <- Full width, background black, text white
└─────────────────────────┘

┌─────────────────────────┐
│  ❤️  FAVORITE           │  <- Full width, border, text black
└─────────────────────────┘
```

### Logic
- Icon trái tim đỏ nếu `isFavorite = true`
- Icon trái tim đen nếu `isFavorite = false`
- Click "Add to Bag" → phải chọn size trước (validation ở ViewModel)

---

## 📂 ExpandableSection

### Chức Năng
Hiển thị phần thông tin có thể mở rộng/thu gọn (accordion pattern).

### Code
```kotlin
@Composable
fun ExpandableSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
)
```

### Parameter
- `title`: Tiêu đề phần (VD: "Shipping & Returns")
- `content`: Nội dung khi mở rộng
- `modifier`: Custom styling

### Ví Dụ Sử Dụng
```kotlin
ExpandableSection(
    title = "Shipping & Returns",
    content = "Free standard shipping on orders over \$50. Returns accepted within 30 days."
)

ExpandableSection(
    title = "Product Description",
    content = "The Nike Air Max 270 was the first lifestyle Air Max..."
)
```

### Màn Hình Hiển Thị
```
SHIPPING & RETURNS                    ▼
(Nội dung ẩn)

--- Click ---

SHIPPING & RETURNS                    △
Free standard shipping on orders...
Returns accepted within 30 days.

--- Click lại ---

SHIPPING & RETURNS                    ▼
(Nội dung ẩn lại)
```

### State Quản Lý
```kotlin
var isExpanded by remember { mutableStateOf(false) }
// Mỗi ExpandableSection có state riêng biệt
// Không chia sẻ state với ExpandableSection khác
```

---

## 🎬 ProductDetailScreen - Màn Hình Chính

### Chức Năng
Kết hợp tất cả components lại thành 1 màn hình hoàn chỉnh.

### Code
```kotlin
@Composable
fun ProductDetailScreen(
    productId: Int,
    viewModel: ProductDetailViewModel = ProductDetailViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToCart: () -> Unit = {}
)
```

### Parameter
- `productId`: ID sản phẩm cần hiển thị
- `viewModel`: ViewModel quản lý logic
- `onBackClick`: Callback khi click nút back
- `onNavigateToCart`: Callback khi thêm vào giỏ hàng thành công

### Cấu Trúc Nội Bộ

#### 1. **State Collection** (Lấy dữ liệu từ ViewModel)
```kotlin
val productDetail by viewModel.productDetail.collectAsState(initial = null)
val selectedSize by viewModel.selectedSize.collectAsState(initial = null)
val isLoading by viewModel.isLoading.collectAsState(initial = false)
```

#### 2. **LaunchedEffect** (Tải dữ liệu lần đầu)
```kotlin
LaunchedEffect(productId) {
    viewModel.loadProductDetail(productId)
}
// Chạy 1 lần khi component được tạo
// Nếu productId thay đổi, chạy lại
```

#### 3. **Loading State** (Hiển thị khi đang tải)
```kotlin
if (isLoading || productDetail == null) {
    Box(...) { CircularProgressIndicator(...) }
    return  // Không render nội dung khác
}
```

#### 4. **Main Content** (Nội dung chính)
```
TopAppBar (Nút back + Logo + Shopping bag)
    ↓
ProductHeroImage (Ảnh)
    ↓
ProductHeaderInfo (Tên, giá, rating)
    ↓
SizeSelector (Chọn kích thước)
    ↓
ActionButtonsSection (Nút Add + Favorite)
    ↓
Divider (Đường kẻ)
    ↓
ExpandableSection (Shipping)
    ↓
ExpandableSection (Description)
```

---

## 🔗 Navigation Flow

### Màn Hình ProductListScreen → ProductDetailScreen

```kotlin
// 1. User click vào ProductCard
ProductCard(
    onProductClick = { productId ->
        // 2. Gọi callback từ ProductListScreen
        onNavigateToDetail(productId)
    }
)

// 3. ProductListScreen callback gọi navigate
onNavigateToDetail = { productId ->
    navController.navigate("product_detail/$productId")
}

// 4. MainActivity nhận request navigate
composable("product_detail/{productId}") { backStackEntry ->
    val productId = backStackEntry.arguments?.getString("productId")?.toInt() ?: 1
    ProductDetailScreen(productId = productId, ...)
}

// 5. ProductDetailScreen được render
ProductDetailScreen(productId = 1)
  ↓
viewModel.loadProductDetail(1)
  ↓
Repository.getAllProducts() → tìm product ID = 1
  ↓
_productDetail.value = Product(id=1, name="Nike Air Max 270", ...)
  ↓
UI cập nhật và render
```

---

## 🎨 Color Scheme

| Phần | Màu | Hex |
|------|-----|-----|
| Background | Trắng | #FFFFFF |
| Text Chính | Đen | #000000 |
| Text Phụ | Xám | #808080 |
| Border | Xám Nhạt | #E0E0E0 |
| Button Primary | Đen | #000000 |
| Button Primary Text | Trắng | #FFFFFF |
| Button Secondary Border | Xám Nhạt | #E0E0E0 |

---

## 📐 Layout Sizing

| Thành Phần | Width | Height |
|-----------|-------|--------|
| ProductHeroImage | Match Parent | 1:1 aspect ratio |
| Size Button | Weight(1f) | 56.dp |
| TopAppBar | Match Parent | 64.dp |
| Button | Match Parent | 56.dp |
| Spacer | - | 8dp / 12dp / 16dp / 32dp |

---

## ✨ Tips & Best Practices

### 1. **Tránh Re-compose Không Cần Thiết**
```kotlin
// ❌ Bad: Callback tạo mới mỗi lần
ProductHeroImage(
    imageUrl = productDetail?.imageUrl,
    contentDescription = productDetail?.name
) { /* ... */ }  // Lambda mới mỗi render

// ✅ Good: Dùng remember nếu cần
val onSizeSelected = remember { { size: Int -> viewModel.selectSize(size) } }
```

### 2. **Async Image Loading**
```kotlin
AsyncImage(
    model = imageUrl,           // URL từ API
    contentScale = ContentScale.Crop,  // Cắt ảnh
    onLoading = { /* loading state */ }
)
```

### 3. **State Management**
```kotlin
// ❌ Bad: State ở component
var selectedSize by remember { mutableStateOf<Int?>(null) }

// ✅ Good: State ở ViewModel
val selectedSize by viewModel.selectedSize.collectAsState()
onSizeSelected = { size -> viewModel.selectSize(size) }
```

---

## 🧪 Test Cases

```kotlin
// Test 1: ProductDetailScreen load đúng sản phẩm
// Verify: productDetail != null, name hiển thị đúng

// Test 2: Click size button
// Verify: selectedSize = 8, button highlight

// Test 3: Click "Add to Bag" không chọn size
// Verify: Show error message

// Test 4: Click "Favorite"
// Verify: Icon trái tim đổi màu, isFavorite = true

// Test 5: Click "Add to Bag" với size đã chọn
// Verify: onNavigateToCart được gọi, navigate to cart
```

