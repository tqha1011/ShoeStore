package com.example.shoestoreapp.features.cart.data.repositories

import com.example.shoestoreapp.features.cart.data.models.CartItem
import com.example.shoestoreapp.features.cart.data.models.CartSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * CartRepository: Lớp này cung cấp và quản lý dữ liệu giỏ hàng.
 *
 * Chức năng:
 * - CRUD operations cho cart items (add, remove, update quantity, clear)
 * - Cung cấp danh sách items trong giỏ
 * - Tính toán cart summary (subtotal, shipping, tax)
 */
class CartRepository {

    // Mock database - Sau này kết nối thực DB/API
    private val _cartItems = MutableStateFlow (
        listOf(
            CartItem(
                id = 1,
                productId = 1,
                name = "Nike Air Max Dn",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDYuBPFFJLEfFz0Py1cLCYY4mSYh8ht2I_IsEEvfrRf-m7JMitJlG107_qxF4PuGRYaXu8TLlv53wf9bJkeCRJ6RkzTUO1EOFlAMUxvm9HROdkbDSMfiHHb-OdXpBfcfdB3kOBdCEZP2ouam01i6n5pmcLd36I_Epo0qdFyErTf4jBSDzeFtJ0WNVByHzcZs8ayMmcnnMt64jXrO0qXJCNo58ULhuK0cRvSpMHH7WIIAo79FTmx6eOtqiCp8NigYDRliBAygbyr2ig",
                description = "Black/Dark Grey/Anthracite",
                price = 160.0,
                quantity = 1,
                size = "10.5",
                stock = 5
            ),
            CartItem(
                id = 2,
                productId = 2,
                name = "Nike Dunk Low Retro",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDtZWOHm8KStDdiNh78lDG_B84kQInaHTzRZtk_tSpuznjleepUiB2U0zqe4jso2r2TraatBbjbJu_jEj3j7DRq6LW-GF-M9sXWysNBV1Q48AjvpvqNUKBzumoBAjE_QP6VxAqXIbSE6qdkB0KhEpEZ8G8BsmVJO7RnNRWeV0rQPYXKuucmUY4E5__5eg6n8y-TwL6z1Hpozv0Nucn8FdGDV98-lgfJK2AlOOzrynAI86__RCSFvTafsoXGfA3J-2LLhaP3O1Xdbig",
                description = "White/Black",
                price = 115.0,
                quantity = 1,
                size = "9",
                stock = 0
            ),
            CartItem(
                id = 3,
                productId = 3,
                name = "Nike Air Zoom Pegasus 40",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBcTR_-_NtrU8acuOHnVk5UdbtGYG9xrEPRs0et2GL3zT8Ay7-FTGhpDsXqplNoAqE7MjOfZQnDvJXsU5q6RMUoA-N2vr3u7hoeURB0KI9FnEgdn6gp5yq1p9oXI4AJzMkUH0MUq0t2Xl1-Z295_rfzJDPhB2XQ62m-2_JmhICsqz8SAisZLMjTd2VL6_KFD4rmEAxjcG7aeyGoFlOx6aeBxPO0zSSTRDmyyaBuHBdNUofuCQTGuAGeELmX2GFqEcsm-WzYMlBepik",
                description = "Bright Crimson/University Red",
                price = 130.0,
                quantity = 1,
                size = "11",
                stock = 8
            )
        )
    )
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    /**
     * Thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm đã có, tăng quantity
     */
    fun addToCart(item: CartItem): Boolean {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find {
            it.productId == item.productId && it.size == item.size
        }

        if (existingItem != null) {
            // Kiểm tra stock
            if (existingItem.quantity + item.quantity > existingItem.stock) {
                return false
            }
            // Cập nhật quantity
            val updatedItem = existingItem.copy(
                quantity = existingItem.quantity + item.quantity
            )
            currentItems[currentItems.indexOf(existingItem)] = updatedItem
        } else {
            // Thêm item mới
            currentItems.add(item)
        }

        _cartItems.value = currentItems
        return true
    }

    /**
     * Xóa item khỏi giỏ hàng
     */
    fun removeFromCart(itemId: Int): Boolean {
        val currentItems = _cartItems.value.toMutableList()
        val removed = currentItems.removeIf { it.id == itemId }
        if (removed) {
            _cartItems.value = currentItems
        }
        return removed
    }

    /**
     * Cập nhật số lượng của item
     */
    fun updateQuantity(itemId: Int, newQuantity: Int): Boolean {
        if (newQuantity <= 0) return false

        val currentItems = _cartItems.value.toMutableList()
        val item = currentItems.find { it.id == itemId } ?: return false

        // Kiểm tra stock
        if (newQuantity > item.stock) {
            return false
        }

        val updatedItem = item.copy(quantity = newQuantity)
        currentItems[currentItems.indexOf(item)] = updatedItem
        _cartItems.value = currentItems
        return true
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    fun clearCart() {
        _cartItems.value = emptyList()
    }

    /**
     * Lấy danh sách items hiện tại
     */
    fun getCartItems(): List<CartItem> = _cartItems.value

    /**
     * Tính cart summary
     */
    fun calculateSummary(): CartSummary {
        val items = _cartItems.value
        val subtotal = items.sumOf { it.getTotalPrice() }
        val itemCount = items.size

        return CartSummary(
            subtotal = subtotal,
            shippingCost = 15.0,
            tax = 0.0,
            itemCount = itemCount
        )
    }

    /**
     * Kiểm tra giỏ hàng có items không
     */
    fun isCartEmpty(): Boolean = _cartItems.value.isEmpty()

    /**
     * Lấy tổng số lượng sản phẩm (tính quantity)
     */
    fun getTotalQuantity(): Int = _cartItems.value.sumOf { it.quantity }
}

