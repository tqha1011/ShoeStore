package com.example.shoestoreapp.features.user.checkout.data.repositories

import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto

// Bỏ DTO trong session để làm kho chứa chung (Shared Memory) giữ Cart Screen và Checkout Screen.
object CheckoutSession {
    var pendingItems: List<CheckOutRequestDto> = emptyList()
}