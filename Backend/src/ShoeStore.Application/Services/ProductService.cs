using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ErrorOr;

namespace ShoeStore.Application.Services
{
    public class ProductService : IProductService
    {
        private readonly IUnitOfWork _uow;
        private readonly IProductRepository _productRepository;

        public ProductService(IUnitOfWork uow, IProductRepository productRepository)
        {
            _uow = uow;
            _productRepository = productRepository;
        }

        // 1. Lấy danh sách (Search/Filter/Paging)
        // Trả về danh sách đã phân trang
        public async Task<ErrorOr<PageResult<ProductResponseDto>>> GetProductsAsync(ProductSearchRequest request, CancellationToken token)
        {
            var query = _productRepository.SearchProduct(request);


        }

        // 2. Lấy chi tiết (Dùng ProductResponseDto)
        Task<ErrorOr<ProductResponseDto>> GetProductByIdAsync(int id, CancellationToken token);

        // 3. Thêm mới: Dùng Status "Created" 
        // Hoặc trả về chính ProductResponseDto để lấy ID mới tạo
        Task<ErrorOr<Created>> AddProductAsync(CreateProductDto dto, CancellationToken token);

        // 4. Cập nhật: Dùng Status "Updated"
        Task<ErrorOr<Updated>> UpdateProductAsync(int id, UpdateProductDto dto, CancellationToken token);

        // 5. Xóa: Dùng Status "Deleted"
        Task<ErrorOr<Deleted>> DeleteProductAsync(int id, CancellationToken token);

        // 6. Các thao tác phụ khác: Dùng Status "Success"
        Task<ErrorOr<Success>> ToggleStatusAsync(int id, CancellationToken token);
    }
}
