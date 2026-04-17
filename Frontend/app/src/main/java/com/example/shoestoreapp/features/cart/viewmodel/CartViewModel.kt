package com.example.shoestoreapp.features.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.cart.data.models.CartItem
import com.example.shoestoreapp.features.cart.data.models.CartSummary
import com.example.shoestoreapp.features.cart.data.remote.CartItemResponseDto
import com.example.shoestoreapp.features.cart.data.repositories.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * CartViewModel: Quản lý logic và state của giỏ hàng.
 *
 * Chức năng:
 * - Quản lý state UI (cart items, loading, error)
 * - Xử lý business logic (add/remove/update items) qua API
 * - Tính toán cart summary
 * - Xử lý callbacks từ UI (user interactions)
 * - Kết nối giữa UI và Repository
 * - Mapping CartItemResponseDto → CartItem
 *
 * API Methods sử dụng:
 * - repository.addToCart(variantId, quantity) - Thêm sản phẩm
 * - repository.updateCartItem(cartItemId, newVariantId, quantity) - Cập nhật item
 * - repository.removeCartItem(cartItemId) - Xóa 1 item
 * - repository.removeFromCart(cartItemIds) - Xóa nhiều items
 *
 * Note:
 * - Sử dụng GUID (String) thay vì Int ID
 * - Tất cả operations gửi kèm token Authorization
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

    // Map từ CartItem.id (hashCode) → cartItemId (GUID từ Backend)
    private val idToCartItemIdMap = mutableMapOf<String, String>()

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
                // API_PEND: Implement backend GET /api/cart endpoint
                // Tạm thời để empty list
                _cartItems.value = emptyList()
                _isCartEmpty.value = true
                _errorMessage.value = ""
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                _isLoading.value = false
            }
        }
    }

    /**
     * Cập nhật cart summary dựa trên items hiện tại
     * Tính toán: subtotal, shipping, tax, total
     */
    private fun updateCartSummary() {
        val items = _cartItems.value
        val subtotal = items.sumOf { it.price * it.quantity }
        

        val shipping = 0.0
        val tax = subtotal * 0.1  // 10% tax

        _cartSummary.value = CartSummary(
            subtotal = subtotal,
            shippingCost = shipping,
            tax = tax,
            itemCount = items.size
        )
    }

    // ============ CALLBACKS - USER INTERACTIONS ============
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     * Gọi API: POST /api/cart
     * 
     * @param variantId - GUID của product variant
     * @param quantity - Số lượng sản phẩm
     */
    fun onAddToCart(variantId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Gọi repository thêm sản phẩm
                val response = repository.addToCart(variantId, quantity).first()

                if (response != null) {
                    // Mapping DTO → CartItem
                    val cartItem = mapDtoToCartItem(response)
                    
                    // Lưu mapping từ item.id → cartItemId
                    idToCartItemIdMap[cartItem.id.toString()] = response.cartItemId
                    
                    // Thêm item vào list
                    val updatedItems = _cartItems.value + cartItem
                    _cartItems.value = updatedItems
                    _isCartEmpty.value = false
                    
                    // Cập nhật summary
                    updateCartSummary()
                    
                    _errorMessage.value = ""
                } else {
                    _errorMessage.value = "Không thể thêm sản phẩm vào giỏ"
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi thêm sản phẩm"
                _isLoading.value = false
            }
        }
    }

    /**
     * Cập nhật item trong giỏ (variant và/hoặc quantity)
     * Gọi API: PUT /api/cart
     * 
     * @param itemIdString - String ID của CartItem (từ UI)
     * @param newVariantId - GUID của product variant mới
     * @param newQuantity - Số lượng mới
     */
    fun onUpdateCartItem(itemIdString: String, newVariantId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    _errorMessage.value = "Số lượng phải lớn hơn 0"
                    return@launch
                }

                // Lấy cartItemId từ map
                val cartItemId = idToCartItemIdMap[itemIdString]
                if (cartItemId == null) {
                    _errorMessage.value = "Không tìm thấy cart item"
                    return@launch
                }

                _isLoading.value = true
                
                // Gọi repository cập nhật item
                val response = repository.updateCartItem(cartItemId, newVariantId, newQuantity).first()

                if (response != null) {
                    // Mapping DTO → CartItem
                    val updatedCartItem = mapDtoToCartItem(response)
                    
                    // Cập nhật mapping
                    idToCartItemIdMap[updatedCartItem.id.toString()] = response.cartItemId
                    
                    // Cập nhật item trong list
                    val updatedItems = _cartItems.value.map { item ->
                        if (item.id == updatedCartItem.id) updatedCartItem else item
                    }
                    _cartItems.value = updatedItems
                    
                    // Cập nhật summary
                    updateCartSummary()
                    
                    _errorMessage.value = ""
                } else {
                    _errorMessage.value = "Không thể cập nhật sản phẩm"
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi cập nhật"
                _isLoading.value = false
            }
        }
    }

    /**
     * Xóa item khỏi giỏ
     * Gọi API: POST /api/cart/remove-items
     * 
     * @param itemIdString - String ID của CartItem (từ UI)
     */
    fun onRemoveItem(itemIdString: String) {
        viewModelScope.launch {
            try {
                // Lấy cartItemId từ map
                val cartItemId = idToCartItemIdMap[itemIdString]
                if (cartItemId == null) {
                    _errorMessage.value = "Không tìm thấy cart item"
                    return@launch
                }

                _isLoading.value = true
                
                // Gọi repository xóa item
                val success = repository.removeCartItem(cartItemId).first()

                if (success) {
                    // Xóa item khỏi list
                    val updatedItems = _cartItems.value.filter { it.id.toString() != itemIdString }
                    _cartItems.value = updatedItems
                    _isCartEmpty.value = updatedItems.isEmpty()
                    
                    // Xóa mapping
                    idToCartItemIdMap.remove(itemIdString)
                    
                    // Cập nhật summary
                    updateCartSummary()
                    
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
     * Tăng số lượng item (increment)
     * 
     * @param cartItemId - GUID của cart item
     */
    fun onIncreaseQuantity(cartItemId: String) {
        val item = _cartItems.value.find { it.id.toString() == cartItemId } ?: return
        onUpdateCartItem(cartItemId, item.productId, item.quantity + 1)
    }

    /**
     * Giảm số lượng item (decrement)
     * 
     * @param cartItemId - GUID của cart item
     */
    fun onDecreaseQuantity(cartItemId: String) {
        val item = _cartItems.value.find { it.id.toString() == cartItemId } ?: return
        if (item.quantity > 1) {
            onUpdateCartItem(cartItemId, item.productId, item.quantity - 1)
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

    /**
     * Mapping CartItemResponseDto (từ API) → CartItem (for UI)
     * 
     * @param dto - Response từ backend
     * @return CartItem - Model dùng cho UI
     */
    private fun mapDtoToCartItem(dto: CartItemResponseDto): CartItem {
        return CartItem(
            id = dto.cartItemId.hashCode(),
            productId = dto.productVariantId ?: "",
            name = dto.productName ?: "",
            imageUrl = dto.imageUrl ?: "",
            description = dto.brand ?: "",
            price = dto.price,
            quantity = dto.quantity,
            size = "${dto.size} (${dto.colorName ?: ""})",  // Kích cỡ + Màu sắc
            stock = dto.stock
        )
    }
}

