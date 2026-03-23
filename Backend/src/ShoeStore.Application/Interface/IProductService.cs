using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ShoeStore.Domain.Entities;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Interface
{

    public interface IProductService
    {
        Task AddProduct(CreateProductDTO dto);
        Task UpdateProduct(int id, UpdateProductDTO dto, CancellationToken token);


        // Get the product
        Task<IEnumerable<ProductResponseDTO>> GetProductAsync(ProductSearchRequest request);
    }
}
