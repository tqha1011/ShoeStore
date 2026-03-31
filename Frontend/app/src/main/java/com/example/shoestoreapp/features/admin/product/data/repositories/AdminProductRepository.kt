package com.example.shoestoreapp.features.admin.product.data.repositories
import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
import com.example.shoestoreapp.features.admin.product.data.models.StockStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
class AdminProductRepository {
    private val _adminProducts = MutableStateFlow<List<AdminProduct>>(emptyList())
    val adminProducts: StateFlow<List<AdminProduct>> = _adminProducts.asStateFlow()
    init {
        loadProducts()
    }
    private fun loadProducts() {
        val products = listOf(
            AdminProduct(1, "Nike Air Max Pulse", "https://lh3.googleusercontent.com/aida-public/AB6AXuDwufuEZHBvHZJJUTXVgm7O75XsxkFiNVCT2M7ISbTRLR3BLvbo-7DF9sA6MwLmr-v9_yjikGRx2EV08iqBexy7O57PMouDPiL4OnKJ6Upcf6TPAl8WAfz88uDc9EUfDnySUWvehg7ClaX6YTYUO_PbQ-KSso_1Drjte-4D4IGNfWeM5eBqqHaK_08CTS4dnfnHVcqzKXGfNHce1rEm1sI8cvzIPN4SxYSkc4w8AYu1t_OEsb6RMG7Xb-E9ew4rghopJxsOMLCnoLY", "Bright Green", 160.0, StockStatus.IN_STOCK, 45, "BESTSELLER", "Men's Shoes"),
            AdminProduct(2, "Nike Pegasus 40", "https://lh3.googleusercontent.com/aida-public/AB6AXuCuFfe3XM4GbYhoi9pK8IUNRK3forsaZty1dlWnLMG5h5V1Wov3-aeCm7WVKAx_x1hs1T8XmuatZWdigwWyo7czUAWSeN8JA0ab7_Wcn3qgSY-vM1Jmg8ZnUjzfBDqApiS6tVXj7gwML7_7XGyGytBdB0XvA47W-3eqAab5SNfvbjrVldn2PRHf5PY-dnbwKFCYeu6nNFpT4WshkOTnwsfjLhd8ervIQHgQ2kow_eMVfB2rk8YTF3oieq7Ck451Mq94ffGzBWRZ4tg", "Red and Black", 130.0, StockStatus.LOW_STOCK, 8, "NEW RELEASE", "Running Shoes"),
            AdminProduct(3, "Nike Dunk Low", "https://lh3.googleusercontent.com/aida-public/AB6AXuAXmaWWRstkot5MlS-EdXL_yIzWU8x2ZH0gWuaM5Uh99pqjXTqIbU5kmXY4gLt1pvP40w5QXBlrciyyNkw2UW6eKtn2jJPiLo8dkeDNwVDWzFsIn42iWZSBesfAGHyiC7dmjBDYTMlgLO3vqA9YVQNjMOjjRLlo2_8H1PmsSvWK1vXUa4KB-FaZeNXa9otxbK5VWeY6Roz84OKEvpcFnLxfEOjCtbBL2mGsPpTGjBnrXoZYTKZ9ItG9FpgqhShAwe6p2NMxKN1i3Qc", "White and Grey", 115.0, StockStatus.OUT_OF_STOCK, 0, "ICONIC", "Vintage Style"),
            AdminProduct(4, "Nike Air Force 1 '07", "https://lh3.googleusercontent.com/aida-public/AB6AXuDPuHcFzgBc1ls_ulq7d3AZD5AcPA950O6PceFLbpiKjTvzoTCeGmF_sFnk7T3ybS0rk_AubITpk55u9-yJThhAY6Ar7Jmn5vcbm2hUSNbzOQQ4I9NKIw2HOTzCKaTlzs80ohcubsLjJIwbU7ETl8fkq67wOKhCbYRyaS4GRHDZ9TfCSrW7fzv_qeAkeK-yeheU3vvHWOcbN0aBxoY0wlivMOvHJFdakwwVGbR1gyEhB7B-1yPd03ec692Dybf8tXPkd7WXlZfd-Lk", "Black and White", 110.0, StockStatus.IN_STOCK, 62, "CLASSIC", "Men's Shoes"),
            AdminProduct(5, "Nike Invincible 3", "https://lh3.googleusercontent.com/aida-public/AB6AXuBClnepqEq8kKOWXqUfREbYT9uN_oYNu23xsokmerBGwXeel5pfWU_H54Ys51tA73zUeh7bqwkcPHTLi6OrwzfgLULlX8Fhu6qFzJpv47BtoV7uxxfh6X4XPTmEsaBHg6ESroab0UBUygdTnA9u9ICaK-srl0pvuJQbf3W3ebQ1cn2Rg71jtuVuOHFTy2iwgiXbSdj7sIidzV5UEZh2-KVgtWYNL4SHEgKj-oYxo-NZ7LsoOKxanHJS6xoKd8rXtT0n41HwQUvLeVc", "Blue and White", 180.0, StockStatus.LOW_STOCK, 3, "PREMIUM", "Running Shoes"),
            AdminProduct(6, "Nike Cosmic Unity 3", "https://lh3.googleusercontent.com/aida-public/AB6AXuBReQrgCAnP987rt9zU7bm7lfzE8u2_3zz8QGFgaoowGlGIYCc0yAb1fq29kDeOIfEFj9f9vhW07-6Q7iIckjMf_kKQk1xMnAkqxo8pzeAcYD80VStS6CjjZp5-jyDRr2J_hUVXQ1Aje5TOgzVFmev1lnCXcBTESGy4hF0fb--ycpo2sYM2af4R2aQLBSfC7x05Z-4G9k-EPmEsM3GKTM1M6WnFB9TOCgCuIxVojlvkczA6IUonx9iF92_eHk9B2ogRezV-pV2bE94", "Neon Yellow", 170.0, StockStatus.IN_STOCK, 28, "POPULAR", "Basketball Shoes")
        )
        _adminProducts.value = products
    }
}
