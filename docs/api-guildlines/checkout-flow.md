# Hướng Dẫn Quy Trình Checkout - ShoeStore

Tài liệu này hướng dẫn các frontend developer hiểu rõ quy trình **Checkout**, **Đặt Hàng với SePay**, và **Nhận Thông Báo Thanh Toán qua SignalR**.

---

## 1. Quy Trình Checkout (Prepare)

### Mục Đích
Chuẩn bị phiên checkout bằng cách xác thực các sản phẩm, kiểm tra tồn kho, và tính toán tổng tiền.

### API Endpoint
```
POST /api/checkout/prepare
```

### Request Body
```json
[
  {
    "variantId": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 2
  },
  {
    "variantId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "quantity": 1
  }
]
```

### Response (Success - 200)
```json
{
  "items": [
    {
      "variantId": "550e8400-e29b-41d4-a716-446655440000",
      "productName": "Giày Nike Air Max",
      "colorName": "Đen",
      "size": 42,
      "unitPrice": 2500000,
      "quantity": 2,
      "stockAvailable": 10,
      "isOutOfStock": false,
      "subTotal": 5000000
    }
  ],
  "summary": {
    "totalPrice": 5000000,
    "finalPrice": 5000000
  },
  "warnings": []
}
```

### Response Errors

| Status Code | Lỗi | Hành Động |
|---|---|---|
| **401** | `Unauthorized` | Người dùng chưa đăng nhập - Yêu cầu đăng nhập lại |
| **404** | `Variant.NotFound` | Sản phẩm không còn tồn tại - Xóa khỏi giỏ hàng |
| **429** | `Too Many Requests` | Yêu cầu quá nhanh - Chờ một lúc rồi thử lại |
| **500** | Server Error | Lỗi máy chủ - Thử lại sau |

### Kotlin Compose Implementation

```kotlin
// 1. Chuẩn bị dữ liệu giỏ hàng
val cartItems = listOf(
    CheckOutRequestDto(variantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), quantity = 2),
    CheckOutRequestDto(variantId = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"), quantity = 1)
)

// 2. Gọi API prepare
val response = checkoutService.prepareCheckout(cartItems)

// 3. Xử lý kết quả
when {
    response.isSuccess -> {
        val summary = response.getOrNull()
        Log.d("Checkout", "Tổng tiền: ${summary?.summary?.finalPrice}")
        // Hiển thị chi tiết đơn hàng lên UI
        updateOrderSummary(summary)
    }
    response.isFailure -> {
        val error = response.exceptionOrNull()
        Log.e("Checkout", "Lỗi: ${error?.message}")
        showErrorDialog(error?.message)
    }
}
```

### Lưu Ý
- ✅ Luôn gọi **prepare** trước khi hiển thị trang **Xác Nhận Đơn Hàng**
- ✅ Kiểm tra **warnings** và thông báo cho người dùng nếu hàng sắp hết
- ✅ Sử dụng `finalPrice` hiển thị trên UI (đã áp dụng voucher, phí vận chuyển)

---

## 2. Quy Trình Đặt Hàng (Place Order) + SePay

### Mục Đích
Tạo đơn hàng, sinh hóa đơn, và gửi yêu cầu thanh toán đến SePay gateway.

### Sơ Đồ Quy Trình

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Frontend gọi PlaceOrder API                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 2. Backend xác thực & tạo hóa đơn (Status: PENDING)         │
│    ↓ KIỂM TRA tồn kho (KHÔNG TRỪ KHO LÚC NÀY)               │
│    ↓ Áp dụng voucher & Tính phí vận chuyển                  │
│                                                             │
│ 3. Backend sinh `OrderCode` & `FinalPrice` gửi về FE        │
│                                                             │
│ 4. Frontend tự gen mã QR VietQR (hoặc dùng thư viện)        │
│    ↓ Hiển thị mã QR trực tiếp trên màn hình chờ             │
│                                                             │
│ 5. Khách hàng dùng App Ngân hàng quét QR & chuyển tiền      │
│                                                             │
│ 6. SePay nhận được tiền -> Gửi webhook về Backend           │
│    ↓ Backend xác thực webhook (so sánh số tiền, chữ ký)     │
│    ↓ Backend CHÍNH THỨC TRỪ TỒN KHO DB                      │
│    ↓ Backend cập nhật Status: PAID                          │
│    ↓ Backend gọi SignalR notification cho Frontend          │
│                                                             │
│ 7. Frontend nhận thông báo via SignalR                      │
│    ↓ Cập nhật UI: Đơn hàng đã thanh toán                    │
│    ↓ Ẩn mã QR, chuyển hướng đến trang Thành Công            │
└─────────────────────────────────────────────────────────────┘
```

### API Endpoint
```
POST /api/checkout/place-order?fromUserCart=false
```

### Request Body
```json
{
  "items": [
    {
      "variantId": "550e8400-e29b-41d4-a716-446655440000",
      "quantity": 2
    }
  ],
  "voucherIds": [1, 2],
  "fullName": "Nguyễn Văn A",
  "address": "123 Đường Lê Lợi, Quận 1, TPHCM",
  "paymentId": 1,
  "phoneNumber": "+84912345678"
}
```

### Response (Success - 201)
```json
{
  "invoicePublicId": "550e8400-e29b-41d4-a716-446655440000",
  "orderCode": "DH000001",
  "fullName": "Nguyễn Văn A",
  "shippingAddress": "123 Đường Lê Lợi, Quận 1, TPHCM",
  "phoneNumber": "+84912345678",
  "status": "Pending",
  "shippingFee": 20000,
  "finalPrice": 4980000,
  "createdAt": "2026-04-08T10:30:00Z",
  "vouchers": [],
  "details": []
}
```

### Response Errors

| Status Code | Lỗi | Hành Động |
|---|---|---|
| **400** | `Stock.NotEnough` | Hàng không đủ - Quay lại giỏ hàng, giảm số lượng |
| **404** | `Variant.NotFound` | Sản phẩm không tồn tại - Xóa khỏi giỏ hàng |
| **404** | `CartItem.NotFound` | Item trong giỏ bị xóa - Xóa khỏi giỏ hàng |
| **404** | `Voucher.NotFound` | Voucher hết hạn hoặc đã dùng - Chọn voucher khác |
| **409** | `Checkout.Concurrency` | Sản phẩm vừa được bán hết - Thử lại |
| **429** | `Too Many Requests` | Đặt hàng quá nhanh - Chờ một lúc |
| **500** | Server Error | Lỗi máy chủ - Thử lại sau |

### Kotlin Compose Implementation

```kotlin
// 1. Chuẩn bị dữ liệu đơn hàng
val orderRequest = PlaceOrderRequestDto(
    items = listOf(
        CheckOutRequestDto(variantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), quantity = 2)
    ),
    voucherIds = listOf(1, 2),
    fullName = "Nguyễn Văn A",
    address = "123 Đường Lê Lợi, Quận 1, TPHCM",
    paymentId = 1,
    phoneNumber = "+84912345678"
)

// 2. Gọi API place-order
val response = checkoutService.placeOrder(orderRequest, fromUserCart = false)

// 3. Xử lý kết quả
when {
    response.isSuccess -> {
        val invoice = response.getOrNull()
        val orderCode = invoice?.orderCode ?: return
        
        Log.d("Order", "Đơn hàng tạo thành công: $orderCode")
        
        // 4. Lưu orderCode để sau này dùng
        saveOrderCode(orderCode)
        
        // 5. Kết nối SignalR và join group
        connectSignalRAndJoinGroup(orderCode)
        
        // 6. Chuyển hướng đến trang thanh toán
        navigateToPaymentPage(invoice)
    }
    response.isFailure -> {
        val error = response.exceptionOrNull()
        Log.e("Order", "Lỗi: ${error?.message}")
        showErrorDialog(error?.message)
    }
}

private fun connectSignalRAndJoinGroup(orderCode: String) {
    // Chi tiết xem phần SignalR ở dưới
}
```

### Lưu Ý Quan Trọng ⚠️
- ✅ **Lưu `orderCode`** ngay sau khi API trả về - Cần để nghe thông báo
- ✅ **Kết nối SignalR TRƯỚC** khi gửi yêu cầu thanh toán đến SePay
- ✅ **Không gọi API này nhiều lần liên tiếp** - Sẽ bị rate limit
- ✅ **Nếu gặp `Checkout.Concurrency`** - Có người khác vừa mua cùng sản phẩm, thử lại
- ✅ Nội dung chuyển khoản: BẮT BUỘC phải truyền orderCode vào nội dung thanh toán để Webhook backend nhận diện được đơn.

---

## 3. Quy Trình Nhận Thông Báo Thanh Toán (SignalR)

### Mục Đích
Nhận thông báo thanh toán **real-time** từ server khi SePay xác nhận chuyển tiền.

### Sơ Đồ Quy Trình

```
┌────────────────────────────────────────────────────┐
│ 1. Frontend kết nối SignalR Hub (/notify-hub)      │
├────────────────────────────────────────────────────┤
│                                                    │
│ 2. Frontend join group với orderCode               │
│    (Ví dụ: group = "DH000001")                     │
│                                                    │
│ 3. Chờ thông báo sự kiện "ReceiveNotification"     │
│                                                    │
│ 4. Khi SePay webhook báo tiền về:                  │
│    ↓ Backend gọi `ReceiveNotification` xuống group │
│    ↓ Frontend nhận được cục JSON thông báo         │
│                                                    │
│ 5. Frontend ẩn mã QR, hiển thị màn hình Thành công │
│                                                    │
│ 6. Frontend rời group khi không cần nữa            │
└────────────────────────────────────────────────────┘
```

### Kotlin Compose Implementation

```kotlin
// 1. Kết nối SignalR Hub
private fun initializeSignalR() {
    val token = sharedPreferences.getString("jwt_token", "") ?: return
    
    hubConnection = HubConnectionBuilder()
        .withUrl("https://your-api.com/notify-hub") {
            headers["Authorization"] = "Bearer $token"
        }
        .withAutomaticReconnect()
        .build()
    
    // 2. Lắng nghe thông báo thanh toán
    hubConnection?.on("ReceiveNotification", { notification: PaymentNotificationDto ->
        Log.d("SignalR", "Nhận thông báo: ${notification.message}")
        
        // 3. Cập nhật trạng thái đơn hàng
        updateOrderStatus(notification.orderCode, "PAID")
        
        // 4. Hiển thị thông báo thành công lên UI
        showSuccessNotification(notification.message)
        
        // 5. Sau 2 giây, chuyển hướng tới trang chi tiết đơn hàng
        lifecycleScope.launch {
            delay(2000)
            navigateToOrderDetails(notification.orderCode)
        }
        
    }, PaymentNotificationDto::class.java)
    
    // 6. Xử lý khi mất kết nối
    hubConnection?.onreconnected {
        Log.d("SignalR", "Kết nối lại thành công")
        // Nếu orderCode còn lưu, rejoin group
        currentOrderCode?.let {
            joinInvoiceGroup(it)
        }
    }
    
    // 7. Bắt đầu kết nối
    hubConnection?.start()?.blockingAwait()
}

// 8. Join group khi tạo đơn hàng xong
fun joinInvoiceGroup(orderCode: String) {
    currentOrderCode = orderCode
    
    hubConnection?.invoke("JoinInvoiceGroup", orderCode)
        .blockingAwait()
    
    Log.d("SignalR", "Đã join group: $orderCode")
}

// 9. Rời group khi không cần nghe nữa (ví dụ: người dùng rời trang)
fun leaveInvoiceGroup(orderCode: String) {
    hubConnection?.invoke("LeftInvoiceGroup", orderCode)
        .blockingAwait()
    
    Log.d("SignalR", "Đã rời group: $orderCode")
    currentOrderCode = null
}

// 10. Ngắt kết nối khi ứng dụng đóng
fun disconnectSignalR() {
    currentOrderCode?.let { leaveInvoiceGroup(it) }
    
    hubConnection?.stop()?.blockingAwait()
    Log.d("SignalR", "Đã ngắt kết nối")
}

// Data class nhận từ SignalR
data class PaymentNotificationDto(
    val message: String,      // "Payment of 5,000,000 VND for order #DH000001 has been successfully received."
    val amount: Long,         // 5000000
    val orderCode: String,    // "DH000001"
    val isSuccess: Boolean,   // true
    val timestamp: String     // "2026-04-08T10:30:00Z"
)
```

### Lifecycle Management (Quan Trọng!)

```kotlin
// Trong Activity/Fragment
override fun onResume() {
    super.onResume()
    
    // Khi màn hình active: kết nối SignalR
    initializeSignalR()
    
    // Nếu đã có orderCode từ lần trước, rejoin
    savedOrderCode?.let {
        joinInvoiceGroup(it)
    }
}

override fun onPause() {
    super.onPause()
    
    // Khi màn hình không active: rời group (nhưng giữ kết nối)
    // Vì người dùng có thể quay lại
    currentOrderCode?.let { leaveInvoiceGroup(it) }
}

override fun onDestroy() {
    super.onDestroy()
    
    // Khi activity bị hủy: ngắt kết nối SignalR
    disconnectSignalR()
}
```

### WebSocket Connection Best Practices

| ✅ Nên Làm | ❌ Không Nên Làm |
|---|---|
| Kết nối khi app khởi động | Kết nối mỗi lần gọi API |
| Join/Leave group động | Giữ kết nối lúc app không dùng |
| Xử lý reconnect tự động | Ignore lỗi kết nối |
| Lưu JWT token và refresh khi hết hạn | Hardcode token |
| Timeout nếu kết nối > 30s | Kết nối vô tận |

---

## 4. Quy Trình Hoàn Chỉnh: Từ Đầu Đến Cuối

### Timeline Người Dùng

```
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 1: Người dùng nhấp "Xem Chi Tiết Giỏ Hàng"                │
├─────────────────────────────────────────────────────────────────┤
│ → Frontend gọi API /prepare                                      │
│ ← Backend trả về summary + warnings                             │
│ → UI hiển thị tổng tiền, phí vận chuyển, sản phẩm               │
│ ✓ Người dùng kiểm tra lại thông tin                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 2: Người dùng nhấp "Tiếp Tục Thanh Toán"                 │
├─────────────────────────────────────────────────────────────────┤
│ → Frontend kết nối SignalR                                       │
│ → Frontend gọi API /place-order                                  │
│ ← Backend tạo đơn hàng, trả về invoice + orderCode             │
│ → Frontend lưu orderCode = "DH000001"                            │
│ → Frontend join group "DH000001" via SignalR                     │
│ ✓ Sẵn sàng nhận thông báo                                        │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 3: Người dùng Chuyển Tiền qua SePay                        │
├─────────────────────────────────────────────────────────────────┤
│ → Frontend chuyển hướng đến SePay                                │
│ → Người dùng chuyển khoản ngân hàng                             │
│ → SePay xác nhận thành công                                      │
│ ✓ Tiền về tài khoản cửa hàng                                     │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 4: Backend Nhận Webhook từ SePay (KHÔNG phải từ FE)       │
├─────────────────────────────────────────────────────────────────┤
│ → SePay gửi webhook đến /api/payment/webhook/sepay              │
│ → Backend xác thực signature                                     │
│ → Backend cập nhật Invoice: Status = PAID                       │
│ → Backend gọi SignalR: SendPaymentNotification                  │
│ ✓ Gửi thông báo đến tất cả client trong group "DH000001"       │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 5: Frontend Nhận SignalR Notification                      │
├─────────────────────────────────────────────────────────────────┤
│ ← Frontend lắng nghe "ReceiveNotification"                       │
│ → Hiển thị toast: "Thanh toán thành công!"                      │
│ → Cập nhật UI: Order Status = PAID                              │
│ → Sau 2 giây: Chuyển hướng đến Order Details                    │
│ → Rời group "DH000001"                                           │
│ ✓ Người dùng thấy đơn hàng đã thanh toán                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. Error Handling Scenarios

### Kịch Bản 1: Hàng Hết Giữa Giữa Prepare và Place Order

```
1. Người dùng gọi /prepare → Còn 10 cái
2. Người dùng khác mua hết 9 cái trong lúc đó
3. Người dùng gọi /place-order
4. Backend trả về: 400 Bad Request - "Stock.NotEnough"
5. Frontend hiển thị: "Hàng chỉ còn 1 cái, bạn muốn mua?"
```

**Frontend Xử Lý:**
```kotlin
when (error.code) {
    "Stock.NotEnough" -> {
        showDialog(
            title = "Hàng không đủ",
            message = "Sản phẩm chỉ còn ${availableStock} cái",
            positiveButton = "Quay lại giỏ hàng"
        ) {
            navigateBack()
            // Gọi lại prepare để update số lượng
        }
    }
}
```

### Kịch Bản 2: Voucher Hết Hạn Lúc Checkout

```
1. Người dùng chọn voucher hôm qua
2. Hôm nay voucher hết hạn
3. Gọi /place-order
4. Backend trả về: 404 Not Found - "Voucher.NotFound"
5. Frontend hiển thị: "Voucher đã hết hạn, vui lòng chọn voucher khác"
```

**Frontend Xử Lý:**
```kotlin
when (error.code) {
    "Voucher.NotFound" -> {
        showDialog(
            title = "Voucher hết hạn",
            message = "Vui lòng chọn voucher khác",
            positiveButton = "Chọn voucher"
        ) {
            // Mở danh sách voucher
            showVoucherSelection()
        }
    }
}
```

### Kịch Bản 3: SignalR Mất Kết Nối

```
1. Người dùng đặt hàng → orderCode = "DH000001"
2. Người dùng chuyển tiền qua SePay
3. Màn hình rơi vào khu vực không có WiFi
4. SignalR mất kết nối tạm thời
5. Backend gửi notification → không nhận được
```

**Frontend Xử Lý:**

```kotlin
// AutomaticReconnect sẽ tự động kết nối lại
val hubConnection = HubConnectionBuilder()
    .withUrl(URL)
    .withAutomaticReconnect() // ← Quan trọng!
    .build()

// Khi kết nối lại
hubConnection?.onreconnected {
    Log.d("SignalR", "Kết nối lại")
    
    // Rejoin group
    currentOrderCode?.let { joinInvoiceGroup(it) }
    
    // Nếu vẫn không nhận được notification, poll API
    checkOrderStatusViaAPI(currentOrderCode)
}

private fun checkOrderStatusViaAPI(orderCode: String) {
    val invoice = checkoutService.getOrderStatus(orderCode)
    if (invoice.status == "PAID") {
        // Hiển thị thành công
        showSuccessNotification("Thanh toán thành công!")
    }
}
```

---

## 6. Dependency & Library (Kotlin)

### Gradle Dependencies

```gradle
dependencies {
    // SignalR
    implementation 'com.microsoft.signalr:signalr:8.0.0'
    
    // Retrofit for REST API
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
    
    // Room for local storage (if needed)
    implementation 'androidx.room:room-runtime:2.5.2'
    
    // Compose
    implementation 'androidx.compose.ui:ui:1.5.0'
    implementation 'androidx.compose.material3:material3:1.0.0'
}
```

---

## 7. Checklist Cho FE Developer

### Trước Khi Deploy

- [ ] Kiểm tra `/prepare` endpoint trả về dữ liệu đúng format
- [ ] Kiểm tra `/place-order` tạo hóa đơn thành công
- [ ] Kiểm tra SignalR kết nối được với server
- [ ] Kiểm tra notification nhận được real-time
- [ ] Kiểm tra xử lý lỗi + error messages
- [ ] Kiểm tra lifecycle: onResume/onPause/onDestroy
- [ ] Kiểm tra reconnect tự động khi mất WiFi
- [ ] Kiểm tra lưu orderCode để fallback polling

### Testing Scenarios

```kotlin
// Test 1: Happy path
1. Gọi /prepare ✓
2. Gọi /place-order ✓
3. Kết nối SignalR ✓
4. Nhận notification ✓
5. Xem order details ✓

// Test 2: Network interruption
1. Đặt hàng thành công
2. Ngắt WiFi
3. Chuyến tiền qua SePay
4. Bật WiFi lại
5. Kiểm tra AutomaticReconnect + Polling

// Test 3: Timeout handling
1. Kết nối SignalR timeout (> 30s)
2. Kiểm tra error message
3. Cho phép user retry
```

---

## 8. FAQ

### Q: Tại sao phải join group với orderCode?
**A:** Để backend biết gửi notification cho ai. Nếu không join, khi SePay confirm thanh toán, backend sẽ broadcast cho tất cả client (sai!). Chỉ client nào join group "DH000001" mới nhận notification cho đơn hàng đó.

### Q: Nếu user không nhận được notification qua SignalR?
**A:** Fallback sang polling. Sau 10 giây không nhận notification, gọi API `GET /api/invoice/{orderCode}` để check trạng thái.

### Q: Có thể gọi `/place-order` nhiều lần không?
**A:** Không! Sẽ bị rate limit (429). Backend cho phép 1 request per 2 giây per user. Nếu user spam, sẽ bị chặn 1 phút.

### Q: SignalR endpoint URL là gì?
**A:** `/hubs/notify` (không cần `/api/` prefix)
```
WebSocket: wss://your-api.com/hubs/notify
HTTP Fallback: https://your-api.com/hubs/notify
```

### Q: JWT token hết hạn khi checkout thì sao?
**A:** Nếu token hết hạn giữa chừng:
- SignalR sẽ disconnect (401)
- Refresh token → reconnect
- Rejoin group với orderCode cũ

---

## 9. Reference

- **SignalR Tutorial:** https://learn.microsoft.com/en-us/aspnet/core/signalr/

---

**Cập nhật lần cuối:** 08/04/2026


