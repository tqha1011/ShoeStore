package com.example.shoestoreapp.features.user.invoice.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.shoestoreapp.R
import com.example.shoestoreapp.core.utils.Constants
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.displayName
import com.example.shoestoreapp.features.invoice.model.toInvoiceStatus
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.abs

data class OrderStatusNotification(
    val orderCode: String,
    val status: InvoiceStatus
)

class OrderStatusSignalRManager(
    private val context: Context,
    private val tokenManager: TokenManager
) {
    private var hubConnection: HubConnection? = null
    private val appContext = context.applicationContext
    private val _notifications = MutableSharedFlow<OrderStatusNotification>(extraBufferCapacity = 8)
    val notifications = _notifications.asSharedFlow()

    suspend fun startConnection() {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) return

        val jwtToken = tokenManager.getToken.firstOrNull().orEmpty()
        if (jwtToken.isBlank()) return

        try {
            val hubUrl = "${Constants.BASE_URL.trimEnd('/')}/hubs/notify"
            hubConnection = HubConnectionBuilder.create(hubUrl)
                .withAccessTokenProvider(Single.defer { Single.just(jwtToken) })
                .withHeader("ngrok-skip-browser-warning", "true")
                .build()

            hubConnection?.on("ReceiveNotification", { orderCode, rawStatus ->
                val status = rawStatus.toInvoiceStatus()
                val notification = OrderStatusNotification(orderCode, status)
                _notifications.tryEmit(notification)
                showLocalNotification(notification)
            }, String::class.java, String::class.java)

            hubConnection?.start()?.blockingAwait()
            Log.d("ORDER_SIGNAL_R", "Connected to order status hub")
        } catch (e: Exception) {
            Log.e("ORDER_SIGNAL_R", "Order status SignalR error: ${e.message}")
        }
    }

    fun stopConnection() {
        try {
            hubConnection?.stop()
        } catch (e: Exception) {
            Log.e("ORDER_SIGNAL_R", "Stop SignalR error: ${e.message}")
        } finally {
            hubConnection = null
        }
    }

    private fun showLocalNotification(notification: OrderStatusNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureNotificationChannel()

        val title = "KicksHub order update"
        val content = "Order ${notification.orderCode} is now ${notification.status.displayName()}."
        val builder = NotificationCompat.Builder(appContext, ORDER_STATUS_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(appContext).notify(
            abs(notification.orderCode.hashCode()),
            builder.build()
        )
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            ORDER_STATUS_CHANNEL_ID,
            "Order status",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "KicksHub order status updates"
        }

        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val ORDER_STATUS_CHANNEL_ID = "kickshub_order_status"
    }
}
