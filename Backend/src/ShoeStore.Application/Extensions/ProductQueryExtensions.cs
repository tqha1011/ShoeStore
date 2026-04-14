using ShoeStore.Domain.Entities;
using ShoeStore.Application.DTOs.ProductDTOs;
namespace ShoeStore.Application.Extensions
{
    public static class ProductQueryExtensions
    {
        public static IQueryable<Product> ApplySearch(this IQueryable<Product> query, string? keyWord)
        {
            if (string.IsNullOrEmpty(keyWord)) return query;

            return query.Where(p =>
                p.ProductName.Contains(keyWord)
            );
        }

        public static IQueryable<Product> ApplyBrand(this IQueryable<Product> query, string? brand)
        {
            if (string.IsNullOrEmpty(brand)) return query;
            return query.Where(p => p.Brand != null && p.Brand.Contains(brand));
        }

        public static IQueryable<Product> ApplySizeId(this IQueryable<Product> query, List<int?>? sizeIds)
        {
            if (sizeIds == null || !sizeIds.Any()) return query;
            return query.Where(p => p.ProductVariants.Any(v => sizeIds.Contains(v.SizeId)));
        }

        public static IQueryable<Product> ApplyColorId(this IQueryable<Product> query, List<int?>? colorIds)
        {
            if (colorIds == null || !colorIds.Any()) return query;
            return query.Where(p => p.ProductVariants.Any(v => colorIds.Contains(v.ColorId)));
        }

        public static IQueryable<Product> ApplyProductId(this IQueryable<Product> query, int? productId)
        {
            if (!productId.HasValue) return query;
            return query.Where(p => p.Id == productId.Value);
        }

        public static IQueryable<Product> ApplyPriceRange(this IQueryable<Product> query, decimal? min, decimal? max)
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

        public static IQueryable<Product> ApplySort(this IQueryable<Product> query, string? sort)
        {
            return sort switch
            {
                "price_asc" => query.OrderBy(p => p.ProductVariants
                            .Where(v => v.IsSelling && !v.IsDeleted)
                            .Min(v => (decimal?)v.Price)),
                "price_desc" => query.OrderByDescending(p => p.ProductVariants
                                    .Where(v => v.IsSelling && !v.IsDeleted)
                                    .Max(v => (decimal?)v.Price)),
                _ => query.OrderBy(p => p.ProductName)
            };
        }

        public static IQueryable<Product> ApplyPaging(this IQueryable<Product> query, int pageIndex, int pageSize)
        {
            return query.Skip((pageIndex - 1) *  pageSize).Take(pageSize);
        }
        public static IQueryable<Product> ApplyStock(this IQueryable<Product> query, ProductAdminRequestDto request)
        {
            // 1. Kiểm tra xem có bất kỳ filter nào được chọn không
            bool isFilterSelected = request.InStock == true || request.LowStock == true || request.OutOfStock == true;

            if (isFilterSelected)
            {
                // Sử dụng Any để kiểm tra: "Chỉ lấy Sản phẩm có ít nhất một Biến thể rơi vào nhóm trạng thái đang chọn"
                query = query.Where(p => p.ProductVariants.Any(v =>
                    (request.InStock == true && v.Stock >= 10) ||
                    (request.LowStock == true && v.Stock < 10 && v.Stock > 0) ||
                    (request.OutOfStock == true && v.Stock <= 0)
                ));
            }

            // 2. Kết hợp với Search Keyword (Lưu ý: Phải gán lại query)
            if (!string.IsNullOrWhiteSpace(request.KeyWord))
            {
                // Giả sử ApplySearch là một extension method bạn đã viết
                query = query.ApplySearch(request.KeyWord);
            }

            return query;
        }
    }
}
