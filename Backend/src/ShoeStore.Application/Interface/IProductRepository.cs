using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Domain.Common;
using ShoeStore.Domain.Entities;
using ShoeStore.Application.DTOs;

namespace ShoeStore.Application.Interface
{
    public interface IProductRepository
    {
        IQueryable<Product> SeachProduct(ProductSearchRequest request);
    }
}
