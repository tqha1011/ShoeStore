using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Domain.Common;
using ShoeStore.Domain.Entities; 

namespace ShoeStore.Application.Interface
{
    internal interface IProductRepository
    {
        void Add(Product product);
        void Update(Product product);
        void Delete(Product product);
        Task<Product?> GetByIdAsync(Guid id);
    }
}
