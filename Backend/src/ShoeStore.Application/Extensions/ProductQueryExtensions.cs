using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Extensions
{
    public static class ProductQueryExtensions
    {
        extension(IQueryable<Product> query)
        {
            public IQueryable<Product> ApplySearch(string? keyWord)
            {
                if (string.IsNullOrEmpty(keyWord)) return query;

                return query.Where(p =>
                    p.ProductName.Contains(keyWord)
                );
            }

            public IQueryable<Product> ApplyBrand(string? brand)
            {
                if (string.IsNullOrEmpty(brand)) return query;
                return query.Where(p => p.Brand != null && p.Brand.Contains(brand));
            }

            public IQueryable<Product> ApplySizeId(List<int?>? sizeIds)
            {
                if (sizeIds == null || !sizeIds.Any()) return query;
                return query.Where(p => p.ProductVariants.Any(v => sizeIds.Contains(v.SizeId)));
            }

            public IQueryable<Product> ApplyColorId(List<int?>? colorIds)
            {
                if (colorIds == null || !colorIds.Any()) return query;
                return query.Where(p => p.ProductVariants.Any(v => colorIds.Contains(v.ColorId)));
            }

            public IQueryable<Product> ApplyProductId(int? productId)
            {
                if (!productId.HasValue) return query;
                return query.Where(p => p.Id == productId.Value);
            }

            public IQueryable<Product> ApplyPriceRange(decimal? min, decimal? max)
            {
                if (min.HasValue && max.HasValue && min > max)
                    return query;

                if(min.HasValue || max.HasValue)
                {
                    query = query.Where(p => p.ProductVariants.Any( v => 
                        v.IsSelling && !v.IsDeleted && (!min.HasValue || v.Price >= min.Value) &&
                        (!max.HasValue || v.Price <= max.Value)));
                }

                return query;
            }

            public IQueryable<Product> ApplySort(string? sort)
            {
                return sort?.Trim().ToLower() switch
                {
                    "price_asc" => query.OrderBy(p => p.ProductVariants
                        .Where(v => v.IsSelling && !v.IsDeleted)
                        .Min(v => (decimal?)v.Price)),
                    "price_desc" => query.OrderByDescending(p => p.ProductVariants
                        .Where(v => v.IsSelling && !v.IsDeleted)
                        .Max(v => (decimal?)v.Price)),
                    "name_asc" => query.OrderBy(p => p.ProductName),
                    "name_desc" => query.OrderByDescending(p => p.ProductName),
                    _ => query.OrderByDescending(p => p.CreatedAt)
                };
            }

            public IQueryable<Product> ApplyPaging(int pageIndex, int pageSize)
            {
                return query.Skip((pageIndex - 1) *  pageSize).Take(pageSize);
            }
        }
    }
}
