using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.DTOs.ProducVariantDTOs
{
    public class CreateProductVariantDTO
    {
        public int SizeId { get; set; }
        public int? ColorId { get; set; }
        public int Stock { get; set; }
        public bool IsSelling { get; set; }
        public string? ImageUrl { get; set; }
        public decimal Price { get; set; }

    }
}
