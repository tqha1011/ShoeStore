package com.example.shoestoreapp.features.user.product.data.repositories

import com.example.shoestoreapp.features.user.product.data.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ProductRepository: Lớp này chỉ cung cấp dữ liệu sản phẩm.
 *
 * Chức năng:
 * - Cung cấp danh sách sản phẩm từ API hoặc database (CRUD)
 * - Không xử lý business logic (filter, search, toggle favorite)
 * - Không quản lý state UI
 *
 * Hiện tại dùng mock data để test giao diện
 * Sau này sẽ kết nối API
 *
 * Note:
 * - Business logic nên ở ViewModel
 * - State UI nên ở ViewModel
 * - Repository chỉ cung cấp raw data
 */
class ProductRepository {

    // ============ MOCK DATA ============
    // Sau này sẽ lấy từ API
    companion object {
        val mockProducts = listOf(
        Product(
            id = 1,
            name = "Nike Air Max 270",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCm7lFwP1IzeCX8YditdOp8Rn3YhAZOc0WtaUuVfuytsItXV2UdGcUSv5OMXfQZp7K8nOF9Heonf4TKCQHrhwXOe-jNwcsXo3gctUCYrYZSUWxX0AwCXfaWRMPc0SHuhlJTlhBef2czpW4_k3YVem6hPpsfKhrU9NAjTGi0qAnWBY4ih4v9D-Rb6h1riBTATNaIszwQZLo6H4kD7F2Bfhy17SGyTbN1ANYIaAR6jBDvmKHA8xSX4ZiJ5PMmyiH9CHCiDmHunYygde2G",
            description = "Red and Black",
            price = 160.0,
            rating = 4.5,
            reviewCount = 128,
            category = "BESTSELLER",
            productType = "Men's Shoes",
            isFavorite = false
        ),
        Product(
            id = 2,
            name = "Nike Dunk Low Retro",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAswSpfF50W3yhc0OFMSIGdrtUHOEORgTadwla9hqUorshSPfw3YNToiryDdr1sxgCHm8zC49eUiBwPJDGtl04NuXzm4pXIoA8UAqkl9T73Kt4fy4wYXHHRjKwQun0frQJZSQWg7h7bT8dsnlIv0PyLNfV_TskKDn8lZi4jHaFfPKoTvTu1FgVB35apFPXX8eMH6a0HZug0WcZvcDVjSzFsUiOScMKa1CGjW010vEdLKJibru564OF_7pdWGdJdZjVPSAzdUvOTN9W_",
            description = "Neon Green",
            price = 115.0,
            rating = 4.3,
            reviewCount = 95,
            category = "NEW RELEASE",
            productType = "Men's Shoes",
            isFavorite = false
        ),
        Product(
            id = 3,
            name = "Nike Air Zoom Pegasus 40",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBytazNxjtHnoopxwWT60FA_VvTWGK4hdMsgSF4GuL5Bjl4lDxb-lOK03WBjG25JsRfCCZFRQFxdS7jWqI0kc-wAqlQeihxxtazZBoLVYw7ekvpwH3TZQnMZCKLopvBauXoNxg5iw2aFENH4VpcT7UQp9QKxCafBv6bhcASTLucAdId-j35Rk05ndOSe4CCB7xoUFhH5-L35rX-f2j7bpeLWxpTMyT8o8xblASd_ZPa0eH2ThHjLoj3mRYSEG8_TjouXCS3nCx4mrCq",
            description = "White and Pastel",
            price = 130.0,
            rating = 4.7,
            reviewCount = 215,
            category = "SUSTAINABLE MATERIALS",
            productType = "Running Shoes",
            isFavorite = false
        ),
        Product(
            id = 4,
            name = "Nike Air Max Pulse",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDv9Q-k0TzebdRwwB4jSqGysHR4hEFRI7529lcQv5oT45Ww66QD3ThxTP-wffVqS33cKaNKgepP_awFY9LXgyK0yl08tkQYJuYPxpOnxcrBBhqyFiEHflk92PYIXrtRKvhPKCpQSc8TBemOk02R7KuNGy-EiX_5qKp0cHX6D423x39qCcXT60sXOoxWhgY4HWMA2lnpNTUGWaG1OJyplLvxu7YLAP-5kn9ZEtxEirhGw5_jnTi4cb7cj89vMp3nLooKMGtAlC43LVwx",
            description = "Black and White",
            price = 150.0,
            rating = 4.2,
            reviewCount = 67,
            category = "LIMITED EDITION",
            productType = "Men's Shoes",
            isFavorite = false
        ),
        Product(
            id = 5,
            name = "Air Jordan 1 Mid",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCN6SZG8cQpf6cHWMpn_lK__9nhuCsFLkIBDPHwdPRwFzXiE8PeeoKHrw0ovFruw0caBiTGbHS4Ct0brO1NGTfV4JlKtCfoUcz3Hd0QiyAhu9DTFteV_54m7YpXov4WBAhhNqlE2KOfk8X0-EKQ7u_FYTE-RBEdBOvux3z7kOZY9LMeDNCqi2JUTMCt0yDcLB6Ca0LiM0-lA76uv26BLGSHPJZcxDr_e1dgaKoP10fQa2REVpWhRawoQFQJW9MnNy-LrrivF9caQiy2",
            description = "Red and White",
            price = 125.0,
            rating = 4.4,
            reviewCount = 320,
            category = "POPULAR",
            productType = "Men's Shoes",
            isFavorite = false
        ),
        Product(
            id = 6,
            name = "Nike Blazer Mid '77",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD2nED-yx0v7ZViCrXQEEyeUYTxsKHLJf-NbR5gpWbI1m9YV2hv-6XM53PZ0g1AtPIxpNSR4cLyML1ORunXcyP7XkPEwoa49027tzJr1d9La4jkl3btbuk-HXwJCKnZMuAvxW8Apn-5Rj-LBDsIBb7s5rf5X3z-gD7fmJzGY90AUR7i0ZQwtXbuAUpwY35jmaW6Ofm-ZMblDBSaNOIPY8L6CMqdAPSo47RB3zE4j4YIaEfXhLYxuOoJDQB81ZzXayWIqaX64741Ialv",
            description = "White and Classic",
            price = 105.0,
            rating = 4.6,
            reviewCount = 180,
            category = "ICONIC",
            productType = "Vintage Style",
            isFavorite = false
        )
        )
    }

    // ============ CUNG CẤP DỮ LIỆU ============
    /**
     * Lấy tất cả sản phẩm (raw data từ mock/API)
     * 
     * @return StateFlow<List<Product>> - Danh sách sản phẩm
     */
    fun getAllProducts(): StateFlow<List<Product>> {
        return MutableStateFlow(mockProducts).asStateFlow()
    }

    // ============ API METHODS (Khi cần save) ============
    /**
     * Lưu trạng thái yêu thích lên server (tuần tự này)
     * 
     * @param productId - ID sản phẩm
     * @param isFavorite - Trạng thái yêu thích mới
     */
    fun updateFavoriteToAPI(productId: Int, isFavorite: Boolean) {
        // TODO: Gọi API để lưu favorite status
        println("Saving favorite status: product=$productId, isFavorite=$isFavorite")
    }

    /**
     * Thêm sản phẩm vào giỏ hàng (gọi API)
     * 
     * @param productId - ID của sản phẩm
     */
    fun addToCart(productId: Int) {
        // TODO: Gọi API để thêm vào giỏ hàng
        println("Added product $productId to cart")
    }

    // ============ DATA LOGIC METHODS ============
    /**
     * Đảo trạng thái yêu thích của sản phẩm (Data Logic Layer)
     * 
     * Chức năng:
     * - Tìm sản phẩm theo ID
     * - Đảo trạng thái isFavorite
     * - Gọi API để lưu thay đổi
     * - Trả về sản phẩm đã update (để ViewModel cập nhật state)
     * 
     * Note: Hàm này chỉ xử lý logic data, không quản lý state UI
     * 
     * @param productId - ID sản phẩm
     * @return Product? - Sản phẩm đã update, hoặc null nếu không tìm thấy
     */
    fun toggleFavorite(productId: Int): Product? {
        val currentList = getAllProducts().value
        val product = currentList.find { it.id == productId }
        
        return product?.copy(isFavorite = !product.isFavorite).also { updatedProduct ->
            if (updatedProduct != null) {
                // Gọi API để lưu thay đổi lên server
                updateFavoriteToAPI(productId, updatedProduct.isFavorite)
            }
        }
    }
}
