package com.example.shoestoreapp.features.user.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.cart.data.models.CartItem
import com.example.shoestoreapp.features.user.cart.data.models.CartSummary
import com.example.shoestoreapp.features.user.cart.data.remote.CartItemResponseDto
import com.example.shoestoreapp.features.user.cart.data.repositories.CartRepository
import com.example.shoestoreapp.features.user.cart.data.repositories.CartRepositoryImpl
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto
import com.example.shoestoreapp.features.user.checkout.data.repositories.CheckoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CartUiState {
    data object Loading : CartUiState
    data class Success(
        val items: List<CartItem>,
        val summary: CartSummary,
        val isEmpty: Boolean
    ) : CartUiState
    data class Error(val message: String) : CartUiState
}

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
 * - repository.updateCartItemQuantity(cartItemId, quantity) - Cập nhật item
 * - repository.removeCartItems(cartItemIds) - Xóa nhiều items
 *
 * Note:
 * - Sử dụng GUID (String) thay vì Int ID
 * - Tất cả operations gửi kèm token Authorization
 */
class CartViewModel(
    private val repository: CartRepository = CartRepositoryImpl()
) : ViewModel() {

    // ============ STATE ============
    private val _cartUiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val cartUiState: StateFlow<CartUiState> = _cartUiState.asStateFlow()

    private val _updatingItemIds = MutableStateFlow<Set<String>>(emptySet())
    val updatingItemIds: StateFlow<Set<String>> = _updatingItemIds.asStateFlow()

    private val _shippingFee = MutableStateFlow(0.0)

    private val _cartDtos = MutableStateFlow<List<CartItemResponseDto>>(emptyList())


    // ============ INIT ============
    init {
        fetchCartItems()
    }

    // ============ LOAD DATA ============
    /**
     * Tải dữ liệu giỏ hàng từ backend khi mở màn hình Cart/Bag.
     */
    fun fetchCartItems() {
        viewModelScope.launch {
            _cartUiState.value = CartUiState.Loading

            try {
                val result = repository.getCartItems()
                result.onSuccess { response ->
                    setSuccess(response.items, response.shippingFee)
                }.onFailure { throwable ->
                    val message = throwable.message ?: "Unable to load cart items"
                    _cartUiState.value = CartUiState.Error(message)
                }
            } catch (e: Exception) {
                val message = e.message ?: "Unknown error"
                _cartUiState.value = CartUiState.Error(message)
            }
        }
    }

    private fun setSuccess(dtoItems: List<CartItemResponseDto>, shippingFee: Double) {
        _cartDtos.value = dtoItems
        _shippingFee.value = shippingFee
        val uiItems = dtoItems.map(::mapDtoToCartItem)
        val summary = buildSummary(uiItems, shippingFee)
        _cartUiState.value = CartUiState.Success(
            items = uiItems,
            summary = summary,
            isEmpty = uiItems.isEmpty()
        )
    }

    private fun buildSummary(items: List<CartItem>, shippingFee: Double): CartSummary {
        val subtotal = items.sumOf { it.price * it.quantity }

        return CartSummary(
            subtotal = subtotal,
            shippingCost = shippingFee,
            itemCount = items.size
        )
    }

    private fun updateCartDtos(transform: (List<CartItemResponseDto>) -> List<CartItemResponseDto>) {
        var updated: List<CartItemResponseDto>? = null
        _cartDtos.update { current ->
            transform(current).also { updated = it }
        }
        val uiItems = (updated ?: _cartDtos.value).map(::mapDtoToCartItem)
        _cartUiState.value = CartUiState.Success(
            items = uiItems,
            summary = buildSummary(uiItems, _shippingFee.value),
            isEmpty = uiItems.isEmpty()
        )
    }

    // ============ CALLBACKS - USER INTERACTIONS ============

    /**
     * Xóa item khỏi giỏ
     * Gọi API: POST /api/cart/remove-items
     * 
     * @param itemIdString - String ID của CartItem (từ UI)
     */
    fun onRemoveItem(itemIdString: String) {
        viewModelScope.launch {
            try {
                val result = repository.removeCartItems(listOf(itemIdString))
                result.onSuccess {
                    updateCartDtos { items -> items.filter { it.cartItemId != itemIdString } }
                }
            } catch (_: Exception) {
                // Keep current state on failure.
            }
        }
    }


    /**
     * Tăng số lượng item (increment)
     * 
     * @param cartItemId - GUID của cart item
     */
    fun onIncreaseQuantity(cartItemId: String) {
        updateQuantityByItemId(cartItemId, isIncrement = true)
    }

    /**
     * Giảm số lượng item (decrement)
     * 
     * @param cartItemId - GUID của cart item
     */
    fun onDecreaseQuantity(cartItemId: String) {
        updateQuantityByItemId(cartItemId, isIncrement = false)
    }

    private fun updateQuantityByItemId(itemIdString: String, isIncrement: Boolean) {

        val dto = _cartDtos.value.firstOrNull { it.cartItemId == itemIdString }
            ?: return

        updateQuantity(dto, isIncrement)
    }


    fun updateQuantity(item: CartItemResponseDto, isIncrement: Boolean) {
        val newQuantity = if (isIncrement) item.quantity + 1 else item.quantity - 1
        if (newQuantity <= 0) {
            return
        }

        val updatingKey = item.cartItemId
        if (_updatingItemIds.value.contains(updatingKey)) return

        viewModelScope.launch {
            _updatingItemIds.update { it + updatingKey }

            try {
                val result = repository.updateCartItemQuantity(item.cartItemId, newQuantity)
                result.onSuccess { updatedDto ->
                    updateCartDtos { items ->
                        items.map { dto ->
                            if (dto.cartItemId == updatedDto.cartItemId) updatedDto else dto
                        }
                    }
                }
            } finally {
                _updatingItemIds.update { it - updatingKey }
            }
        }
    }


    /**
     * Xử lý checkout (chuẩn bị dữ liệu để chuyển sang checkout screen)
     * Logic checkout thực tế sẽ ở CheckoutViewModel
     */
    fun onCheckout() {
        // 1. Map danh sách CartItemResponseDto sang CheckOutRequestDto
        val checkoutItems = _cartDtos.value.map { dto ->
            CheckOutRequestDto(
                variantId = dto.productVariantId,
                quantity = dto.quantity
            )
        }

        // 2. Cất vào kho chứa (Repository)
        CheckoutSession.pendingItems = checkoutItems
    }

    /**
     * Mapping CartItemResponseDto (từ API) → CartItem (for UI)
     * 
     * @param dto - Response từ backend
     * @return CartItem - Model dùng cho UI
     */
    private fun mapDtoToCartItem(dto: CartItemResponseDto): CartItem {
        return CartItem(
            cartItemId = dto.cartItemId,
            productId = dto.productVariantId,
            name = dto.productName,
            imageUrl = dto.imageUrl ?: "",
            description = dto.colorName,
            price = dto.price,
            quantity = dto.quantity,
            size = "${dto.size} (${dto.colorName})",  // Kích cỡ + Màu sắc
            stock = dto.stock
        )
    }
}
