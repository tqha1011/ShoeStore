package com.example.shoestoreapp.features.user.checkout.data.models

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
    val fullAddress: String = " ",
    val isDefault: Boolean = true
) {

}

