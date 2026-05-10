package com.example.shoestoreapp.features.user.checkout.data.repositories

import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto


object CheckoutSession {
    var pendingItems: List<CheckOutRequestDto> = emptyList()
}