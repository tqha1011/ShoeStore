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
        public required int ProductId { get; set; }
        public required int Stock { get; set; }
        public required bool IsSelling { get; set; }
        public string? ImageUrl { get; set; }
        public required decimal Price { get; set; }
        public required bool IsDeleted { get; set; }

    }
}
