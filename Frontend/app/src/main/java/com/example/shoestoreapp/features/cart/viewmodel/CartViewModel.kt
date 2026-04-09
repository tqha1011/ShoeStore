package com.example.shoestoreapp.features.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.cart.data.models.CartItem
import com.example.shoestoreapp.features.cart.data.models.CartSummary
import com.example.shoestoreapp.features.cart.data.repositories.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CartViewModel: Quản lý logic và state của giỏ hàng.
 *
 * Chức năng:
 * - Quản lý state UI (cart items, summary, loading, error)
 * - Xử lý business logic (add/remove/update items)
 * - Tính toán cart summary (subtotal, shipping, tax, total)
 * - Xử lý callbacks từ UI (user interactions)
 * - Kết nối giữa UI và Repository
 *
 * State:
 * - cartItems: Danh sách items trong giỏ
 * - cartSummary: Tổng cộng (subtotal, shipping, tax, total)
 * - isLoading: Trạng thái loading
 * - errorMessage: Thông báo lỗi (nếu có)
 * - isCartEmpty: Giỏ hàng có items hay không
 *
 * Lifecycle:
 * - init: Load dữ liệu cart khi ViewModel được tạo
 * - onCleared: Dọn dẹp khi ViewModel bị destroy (tự động xử lý bởi framework)
 */
class CartViewModel(
    private val repository: CartRepository = CartRepository()
) : ViewModel() {

    // ============ STATE ============
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cartSummary = MutableStateFlow(CartSummary())
    val cartSummary: StateFlow<CartSummary> = _cartSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _isCartEmpty = MutableStateFlow(true)
    val isCartEmpty: StateFlow<Boolean> = _isCartEmpty.asStateFlow()

    // ============ INIT ============
    init {
        loadCart()
    }

    // ============ LOAD DATA ============
    /**
     * Tải dữ liệu giỏ hàng từ repository
     * Cập nhật items và summary
     */
    private fun loadCart() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Lấy items từ repository
                val items = repository.getCartItems()
                _cartItems.value = items

                // Tính summary
                val summary = repository.calculateSummary()
                _cartSummary.value = summary

                // Kiểm tra giỏ trống
                _isCartEmpty.value = items.isEmpty()

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                _isLoading.value = false
            }
        }
    }

    /**
     * Cập nhật cart items từ repository
     * Được gọi sau khi có thay đổi trong repository
     */
    private fun updateCart() {
        val items = repository.getCartItems()
        _cartItems.value = items

        val summary = repository.calculateSummary()
        _cartSummary.value = summary

        _isCartEmpty.value = items.isEmpty()
    }

    // ============ CALLBACKS - USER INTERACTIONS ============
    /**
     * Xóa item khỏi giỏ
     */
    fun onRemoveItem(itemId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = repository.removeFromCart(itemId)

                if (success) {
                    updateCart()
                    _errorMessage.value = ""
                } else {
                    _errorMessage.value = "Không thể xóa item"
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi xóa"
                _isLoading.value = false
            }
        }
    }

    /**
     * Cập nhật số lượng item
     */
    fun onUpdateQuantity(itemId: Int, newQuantity: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = repository.updateQuantity(itemId, newQuantity)

                if (success) {
                    updateCart()
                    _errorMessage.value = ""
                } else {
                    _errorMessage.value = "Số lượng không hợp lệ hoặc vượt tồn kho"
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi cập nhật"
                _isLoading.value = false
            }
        }
    }

    /**
     * Tăng số lượng item (increment)
     */
    fun onIncreaseQuantity(itemId: Int) {
        val item = _cartItems.value.find { it.id == itemId } ?: return
        onUpdateQuantity(itemId, item.quantity + 1)
    }

    /**
     * Giảm số lượng item (decrement)
     */
    fun onDecreaseQuantity(itemId: Int) {
        val item = _cartItems.value.find { it.id == itemId } ?: return
        if (item.quantity > 1) {
            onUpdateQuantity(itemId, item.quantity - 1)
        }
    }

    /**
     * Xử lý checkout (chuẩn bị dữ liệu để chuyển sang checkout screen)
     * Logic checkout thực tế sẽ ở CheckoutViewModel
     */
    fun onCheckout(): Boolean {
        // Validation
        if (_cartItems.value.isEmpty()) {
            _errorMessage.value = "Giỏ hàng trống"
            return false
        }

        // Có thể thêm validations khác ở đây nếu cần
        return true
    }

    /**
     * Xóa thông báo lỗi
     */
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}

