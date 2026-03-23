using System;
using System.Collections.Generic;
using System.Text;

namespace ShoeStore.Application.DTOs.ProductVariantDTOs
{
    public class UpdateProductVariantDto
    {

        public int SizeId { get; set; }
        public int? ColorId { get; set; }
        public int Stock { get; set; }
        public bool IsSelling { get; set; }
        public string? ImageUrl { get; set; }
        public decimal Price { get; set; }
    }
}
