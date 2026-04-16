package com.example.shoestoreapp.core.utils

/**
 * Sealed class để quản lý trạng thái của dữ liệu từ API
 * T: Kiểu dữ liệu trả về khi thành công (ví dụ: Unit, List<Product>, v.v.)
 */
sealed class Resource<out T> {

    // Trạng thái thành công: Chứa dữ liệu trả về
    data class Success<out T>(val data: T) : Resource<T>()

    // Trạng thái lỗi: Chứa thông báo lỗi (thường là từ errorBody của Retrofit)
    data class Error(val message: String) : Resource<Nothing>()

    // Trạng thái đang tải: Dùng để hiện vòng xoay xoay (ProgressBar) trên UI
    object Loading : Resource<Nothing>()
}