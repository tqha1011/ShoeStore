using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ShoeStore.Domain.Entities;

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

    }
}
