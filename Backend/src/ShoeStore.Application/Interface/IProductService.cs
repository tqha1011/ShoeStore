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
        void AddProduct(CreateProductDto dto);
        Task<ErrorOr<Success>> UpdateProduct(int id, UpdateProductDto dto, CancellationToken token);


        // Get the product
        Task<ErrorOr<IEnumerable<ProductResponseDto>>> GetProductAsync(ProductSearchRequest request, CancellationToken  token);
    }
}
