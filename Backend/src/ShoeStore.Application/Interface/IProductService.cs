using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Interface
{

    public interface IProductService
    {
        // 1. Lấy danh sách (Search/Filter/Paging)
        // Trả về danh sách đã phân trang
        Task<ErrorOr<PageResult<ProductResponseDto>>> GetProductsAsync(ProductSearchRequest request, CancellationToken token);

        // 2. Lấy chi tiết (Dùng ProductResponseDto)
        Task<ErrorOr<ProductResponseDto>> GetProductByIdAsync(int id, CancellationToken token);

        // 3. Thêm mới: Dùng Status "Created" 
        // Hoặc trả về chính ProductResponseDto để lấy ID mới tạo
        Task<ErrorOr<Created>> AddProductAsync(CreateProductDto dto, CancellationToken token);

        // 4. Cập nhật: Dùng Status "Updated"
        Task<ErrorOr<Updated>> UpdateProductAsync(int id, UpdateProductDto dto, CancellationToken token);

        // 5. Xóa: Dùng Status "Deleted"
        Task<ErrorOr<Deleted>> DeleteProductAsync(int id, CancellationToken token);

    }
}
