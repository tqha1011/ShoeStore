package com.example.shoestoreapp.features.checkout.data.models

/**
 * Data class đại diện cho địa chỉ giao hàng của khách hàng.
 *
 * @param id - ID địa chỉ
 * @param fullName - Tên đầy đủ người nhận
 * @param street - Tên đường
 * @param city - Thành phố
 * @param state - Tỉnh/Bang
 * @param postalCode - Mã bưu chính
 * @param country - Quốc gia
 * @param isDefault - Có phải địa chỉ mặc định hay không
 */
data class DeliveryAddress(
    val id: String = "",
    val fullName: String = "Jordan Smith",
    val street: String = "123 Innovation Drive, Silicon Valley",
    val city: String = "San Francisco",
    val state: String = "CA",
    val postalCode: String = "94103",
    val country: String = "United States",
    val isDefault: Boolean = true
) {
    fun getFullAddress(): String {
        return "$street\n$city, $state $postalCode\n$country"
    }
}

