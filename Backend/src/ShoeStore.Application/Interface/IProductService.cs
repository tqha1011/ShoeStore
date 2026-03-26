using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface
{

    public interface IProductService
    {
        // Add the product
        Task AddProduct(CreateProductDto dto);

        // Update the product
        Task<ErrorOr<Success>> UpdateProduct(int id, UpdateProductDto dto, CancellationToken token);

        // Soft delete product
        Task DeleteProduct(int id, CancellationToken token);

        // Get all product
        Task<IEnumerable<Product>> GetAllProducts();

        // Get the product request
        Task<ErrorOr<IEnumerable<ProductResponseDto>>> GetProductAsync(ProductSearchRequest request, CancellationToken  token);
    }
}
