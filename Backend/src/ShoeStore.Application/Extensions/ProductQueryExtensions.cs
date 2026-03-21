using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Extensions
{
    public static class ProductQueryExtensions
    {
        public static IQueryable<Product> ApplySearch(this IQueryable<Product> query, string? keyWord)
        {
            if(string.IsNullOrEmpty(keyWord)) return query;
            return query.Where(p => p.ProductName.Contains(keyWord));
        }

        public static IQueryable<Product> ApplyBrand(this IQueryable<Product> query, string? brand)
        {
            if (string.IsNullOrEmpty(brand)) return query;
            return query.Where(p => p.Brand.Contains(brand));
        }

        public static IQueryable<ProductSize> ApplySize(this IQueryable<ProductSize> query, int? size)
        {
            if(!size.HasValue) return query;
            return query.Where(s => s.Size == size); 
        }

        public static IQueryable<ProductVariant> ApplyProductId(this IQueryable<ProductVariant> query, int? id)
        {
            if (!id.HasValue) return query;
            return query.Where(i => i.ProductId == id);
        }

        public static IQueryable<ProductVariant> ApplyColorId(this IQueryable<ProductVariant> query, int? id)
        {
            if (!id.HasValue) return query;
            return query.Where(i => i.ProductId == id);
        }

        public static IQueryable<ProductVariant> ApplySizeId(this IQueryable<ProductVariant> query, int? id)
        {
            if (!id.HasValue) return query;
            return query.Where(i => i.ProductId == id);
        }

        public static IQueryable<Color> ApplyColor(this IQueryable<Color> query, string? color)
        {
            if(string.IsNullOrEmpty(color)) return query;
            return query.Where(c => c.ColorName.Contains(color));
        }

        public static IQueryable<ProductVariant> ApplyPriceRange(this IQueryable<ProductVariant> query, decimal? min, decimal? max)
        {
            if(min.HasValue && max.HasValue && min > max)
            {
                // Error
            }
            if (min.HasValue) query = query.Where(p => p.Price >= min.Value);
            if (max.HasValue) query = query.Where(p => p.Price <= max.Value);
            return query;
        }

        public static IQueryable<ProductVariant> ApplySort(this IQueryable<ProductVariant> query, string? sort)
        {
            return sort switch
            {
                "name_asc" => query.OrderBy(v => v.Product.ProductName),
                "name_desc" => query.OrderByDescending(v => v.Product.ProductName),
                "price_asc" => query.OrderBy(v => v.Price),
                "price_desc" => query.OrderByDescending(v => v.Price),
                _ => query.OrderBy(v => v.Price) // default 
            };
        }
    }
}
