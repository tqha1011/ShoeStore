using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface
{

    public interface IProductService
    {
        // Get the product
        Task<IEnumerable<Product>> GetProductAsync(string? keyWord, string? brand, int? color, int? size, int? productId,
            decimal? minPrice, decimal? maxPric, string? sort, int pageIndex, int pageSize);
    }
}
