package com.example.shoestoreapp.features.user.checkout.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.util.Locale

// DTO hứng dữ liệu từ Backend trả về qua SignalR
data class PaymentNotificationDto(
    val message: String,
    val amount: Double,
    val orderCode: String,
    val isSuccess: Boolean,
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentQRScreen(
    orderCode: String,
    finalPrice: Double,
    bankCode: String,     // Nhận từ Backend
    bankAccount: String,  // Nhận từ Backend
    accountName: String,  // Nhận từ Backend
    onBackToHomeClick: () -> Unit
) {
    var isPaymentSuccess by remember { mutableStateOf(false) }
    var countdownTime by remember { mutableStateOf(5) }
    val coroutineScope = rememberCoroutineScope() // Dùng để chuyển luồng

    // 1. Chuẩn bị nội dung mã QR
    val rawDescription = if (bankCode.uppercase() == "VIETINBANK") "SEVQR $orderCode" else orderCode
    val encodedDes = URLEncoder.encode(rawDescription, StandardCharsets.UTF_8.toString())
    val amount = finalPrice.toLong()
    val formattedAmount = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)

    // 2. Lắp ghép đường link nhúng SePay
    val sePayQrUrl = "https://qr.sepay.vn/img" +
            "?acc=$bankAccount" +
            "&bank=$bankCode" +
            "&amount=$amount" +
            "&des=$encodedDes" +
            "&template=qronly"

    // 3. TÍCH HỢP SIGNALR LẮNG NGHE THANH TOÁN
    DisposableEffect(orderCode) {
        val hubUrl = "https://deploy-service-h6acgba9dkc0gvcw.eastasia-01.azurewebsites.net/hubs/notify\n" +
                "deploy-service-h6acgba9dkc0gvcw.eastasia-01.azurewebsites.net"

        val hubConnection = HubConnectionBuilder
            .create(hubUrl)
            .build()

        // Đăng ký lắng nghe sự kiện
        hubConnection.on("ReceivePaymentNotification", { dto ->
            // Đẩy data về luồng chính (Main Thread) để không bị crash
            coroutineScope.launch(Dispatchers.Main) {
                if (dto.isSuccess && dto.orderCode == orderCode) {
                    isPaymentSuccess = true
                }
            }
        }, PaymentNotificationDto::class.java)

        try {
            // Khởi động kết nối
            hubConnection.start().blockingAwait()
            // Bắt buộc Join Group ngay khi start thành công
            hubConnection.invoke("JoinInvoiceGroup", orderCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Dọn dẹp khi thoát khỏi màn hình
        onDispose {
            try {
                if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    hubConnection.invoke("LeaveInvoiceGroup", orderCode)
                    hubConnection.stop()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 4. LOGIC ĐẾM NGƯỢC KHI THÀNH CÔNG
    if (isPaymentSuccess) {
        LaunchedEffect(isPaymentSuccess) {
            while (countdownTime > 0) {
                delay(1000)
                countdownTime--
            }
            onBackToHomeClick() // Đếm xong 5s thì tự động nhảy về trang chủ
        }
    }

    // 5. GIAO DIỆN
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Order Payment", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Hiệu ứng chuyển cảnh mượt mà giữa QR và màn hình Thành công
            AnimatedContent(targetState = isPaymentSuccess, label = "payment_transition") { success ->
                if (success) {
                    // --- MÀN HÌNH THANH TOÁN THÀNH CÔNG ---
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Payment Successful!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF16A34A)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Thank you for your purchase.\nYour order has been verified automatically.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        Text(
                            text = "You will be redirected in $countdownTime seconds...",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { countdownTime / 5f },
                            modifier = Modifier
                                .width(200.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color.Black,
                            trackColor = Color(0xFFF3F4F6)
                        )
                    }
                } else {
                    // --- MÀN HÌNH QUÉT MÃ QR ---
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Scan QR Code to Pay",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            // Bọc lót nếu Backend trả về rỗng
                            text = "Account Holder: ${accountName.ifEmpty { "Shoe Store Official" }}",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Hiển thị ảnh QR Code động
                        AsyncImage(
                            model = sePayQrUrl,
                            contentDescription = "SePay VietQR Code",
                            modifier = Modifier
                                .size(280.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bảng tóm tắt thông tin
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Amount:", color = Color.Gray)
                                    Text("$formattedAmount VND", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Description:", color = Color.Gray)
                                    Text(rawDescription, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Your order will be processed automatically upon payment receipt.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = onBackToHomeClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "I have completed the payment", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}