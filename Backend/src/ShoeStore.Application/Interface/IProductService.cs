using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface
{
    internal interface IProductService
    {
        // Create
        void AddProduct(Product product);
        // Delete
        void Delete(Product product);
        // Update
        void UpdateProduct(Product product);
        // Get by id
        Task<Product?> GetByIdAsyc(Guid productId);
    }
}
