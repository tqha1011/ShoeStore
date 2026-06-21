package com.example.shoestoreapp.features.user.checkout.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.shoestoreapp.core.utils.Constants
import com.example.shoestoreapp.features.user.checkout.viewmodel.CheckoutViewModel
import com.example.shoestoreapp.features.user.product.ui.components.TopBanner
import com.example.shoestoreapp.features.user.product.ui.components.UserTopBarTitle
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.util.Locale

// ==========================================
// DATA TRANSFER OBJECTS (DTOs)
// ==========================================

data class PaymentNotificationDto(
    val message: String,
    val amount: Double,
    val orderCode: String,
    val isSuccess: Boolean,
    val createdAt: String
)

private data class QrPaymentData(
    val sePayQrUrl: String,
    val formattedAmount: String,
    val rawDescription: String
)

// ==========================================
// MAIN COMPOSABLE SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentQRScreen(
    orderCode: String,
    finalPrice: Double,
    bankCode: String,
    bankAccount: String,
    accountName: String,
    viewModel: CheckoutViewModel,
    onBackClick: () -> Unit,
    onBackToHomeClick: () -> Unit
) {
    var isPaymentSuccess by remember { mutableStateOf(false) }

    val showBanner by viewModel.showBanner.collectAsState()
    val bannerMessage by viewModel.bannerMessage.collectAsState()
    val isBannerSuccess by viewModel.isBannerSuccess.collectAsState()

    ObservePaymentSignalR(
        orderCode = orderCode,
        onPaymentSuccess = { isPaymentSuccess = true }
    )

    val qrData = remember(orderCode, finalPrice, bankCode, bankAccount) {
        generateQrData(orderCode, finalPrice, bankCode, bankAccount)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { UserTopBarTitle(text = "ORDER PAYMENT") },
                navigationIcon = {
                    if (!isPaymentSuccess) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
            ) {
                TopBanner(
                    message = bannerMessage,
                    isSuccess = isBannerSuccess,
                    isVisible = showBanner,
                    onDismiss = { viewModel.hideBanner() }
                )
            }

            AnimatedContent(targetState = isPaymentSuccess, label = "payment_transition") { success ->
                if (success) {
                    PaymentSuccessView(onBackToHomeClick = onBackToHomeClick)
                } else {
                    PaymentScanQrView(
                        accountName = accountName,
                        sePayQrUrl = qrData.sePayQrUrl,
                        formattedAmount = qrData.formattedAmount,
                        rawDescription = qrData.rawDescription,
                        orderCode = orderCode,
                        viewModel = viewModel,
                        onPaymentConfirmed = { isPaymentSuccess = true }
                    )
                }
            }
        }
    }
}

// ==========================================
// BACKGROUND I/O OPERATION (DOWNLOADING FILE)
// ==========================================

/**
 * Hàm xử lý tải hình ảnh bất đồng bộ từ URL và ghi tệp tin vào thư viện ảnh hệ thống (MediaStore).
 * Thiết kế tuân thủ kiến trúc Scoped Storage (Không yêu cầu quyền WRITE_EXTERNAL_STORAGE trên API >= 29).
 */
private suspend fun saveQrToGallery(context: Context, qrUrl: String, orderCode: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            // 1. Khởi tạo luồng mạng để tải dữ liệu hình ảnh thô và giải mã sang Bitmap
            val url = URL(qrUrl)
            val connection = url.openConnection().apply {
                doInput = true
                connect()
            }
            val input = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(input) ?: return@withContext false

            // 2. Thiết lập cấu hình Metadata cho tệp lưu trữ thông qua ContentValues
            val filename = "KicksHub_QR_$orderCode.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Định nghĩa thư mục lưu trữ chuyên biệt của ứng dụng trong thư mục Pictures gốc
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/KicksHub")
                    put(MediaStore.MediaColumns.IS_PENDING, 1) // Khóa tệp tạm thời trong quá trình ghi dữ liệu
                }
            }

            // 3. Thực thi chèn bản ghi dữ liệu thông qua ContentResolver
            val resolver = context.contentResolver
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext false

            // 4. Mở luồng Output Stream để ghi nén thực thể Bitmap thành luồng dữ liệu nhị phân
            val outputStream: OutputStream = resolver.openOutputStream(imageUri) ?: return@withContext false
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()

            // 5. Giải phóng trạng thái khóa tệp sau khi hoàn tất chu trình ghi dữ liệu vật lý
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// ==========================================
// HELPER FUNCTIONS & SUB-COMPONENTS
// ==========================================

private fun generateQrData(
    orderCode: String,
    finalPrice: Double,
    bankCode: String,
    bankAccount: String
): QrPaymentData {
    val rawDescription = if (bankCode.uppercase(Locale.ROOT) == "VIETINBANK") "SEVQR $orderCode" else orderCode
    val encodedDes = URLEncoder.encode(rawDescription, StandardCharsets.UTF_8.toString())
    val amount = finalPrice.toLong()
    val formattedAmount = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount)

    val sePayQrUrl = "https://qr.sepay.vn/img" +
            "?acc=$bankAccount" +
            "&bank=$bankCode" +
            "&amount=$amount" +
            "&des=$encodedDes" +
            "&template=qronly"

    return QrPaymentData(sePayQrUrl, formattedAmount, rawDescription)
}

@Composable
private fun ObservePaymentSignalR(
    orderCode: String,
    onPaymentSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(orderCode) {
        val hubUrl = "${Constants.BASE_URL.trimEnd('/')}/hubs/notify"
        val hubConnection = HubConnectionBuilder.create(hubUrl)
            .withHeader("ngrok-skip-browser-warning", "true")
            .build()

        hubConnection.on("ReceivePaymentNotification", { dto ->
            coroutineScope.launch {
                if (dto.isSuccess && dto.orderCode == orderCode) {
                    onPaymentSuccess()
                }
            }
        }, PaymentNotificationDto::class.java)

        try {
            hubConnection.start().blockingAwait()
            hubConnection.invoke("JoinInvoiceGroup", orderCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
}

@Composable
private fun PaymentSuccessView(onBackToHomeClick: () -> Unit) {
    var countdownTime by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        while (countdownTime > 0) {
            delay(1000)
            countdownTime--
        }
        onBackToHomeClick()
    }

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
}

@Composable
private fun PaymentScanQrView(
    accountName: String,
    sePayQrUrl: String,
    formattedAmount: String,
    rawDescription: String,
    orderCode: String,
    viewModel: CheckoutViewModel,
    onPaymentConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isChecking by viewModel.isCheckingPayment.collectAsState()

    // Quản lý trạng thái xử lý cục bộ khi đang tải và lưu trữ ảnh QR
    var isSavingImage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Scan QR Code to Pay",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Account Holder: ${accountName.ifEmpty { "Shoe Store Official" }}",
            fontSize = 13.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = sePayQrUrl,
                contentDescription = "SePay VietQR Code",
                modifier = Modifier
                    .size(230.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        }

        // NÚT TẢI ẢNH QR VỀ MÁY
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    isSavingImage = true
                    val success = saveQrToGallery(context, sePayQrUrl, orderCode)
                    if (success) {
                        viewModel.showCustomBanner("QR code saved to your gallery.", true)
                    } else {
                        viewModel.showCustomBanner("Failed to save the QR code. Please check your network connection.", false)
                    }
                    isSavingImage = false
                }
            },
            enabled = !isSavingImage,
            modifier = Modifier
                .wrapContentSize()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
            border = ButtonDefaults.outlinedButtonBorder(enabled = !isSavingImage).copy(width = 1.dp)
        ) {
            if (isSavingImage) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download Icon",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Save QR to Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Amount:", color = Color.Gray, fontSize = 13.sp)
                    Text("$formattedAmount VND", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Description:", color = Color.Gray, fontSize = 13.sp)
                    Text(rawDescription, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your order will be processed automatically upon payment receipt.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.verifyPaymentManual(
                    orderCode = orderCode,
                    onSuccess = { onPaymentConfirmed() }
                )
            },
            enabled = !isChecking,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "I have completed the payment", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
