using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Interface.ProductInterface;

public interface IProductService
{
    // 1. Lấy danh sách (Search/Filter/Paging)
    // Trả về danh sách đã phân trang
    Task<ErrorOr<PageResult<ProductResponseDto>>> GetProductsUserAsync(ProductSearchRequest request,
        CancellationToken token);
    // 2. Lấy chi tiết (Dùng ProductResponseDto)
    Task<ErrorOr<ProductResponseDto>> GetProductByGuidAsync(Guid productGuid, CancellationToken token);

    // 3. Thêm mới: Dùng Status "Created" 
    // Hoặc trả về chính ProductResponseDto để lấy ID mới tạo
    Task<ErrorOr<Guid>> AddProductAsync(CreateProductDto dto, CancellationToken token);

    // 4. Cập nhật: Dùng Status "Updated"
    Task<ErrorOr<Updated>> UpdateProductAsync(Guid productGuid, UpdateProductDto dto, CancellationToken token);

    // 5. Xóa: Dùng Status "Deleted"
    Task<ErrorOr<Deleted>> DeleteProductAsync(Guid productGuid, CancellationToken token);
    Task<ErrorOr<PageResult<ProductAdminRespone>>> GetProductsAdminAsync(ProductAdminRequestDto request, CancellationToken token);
}