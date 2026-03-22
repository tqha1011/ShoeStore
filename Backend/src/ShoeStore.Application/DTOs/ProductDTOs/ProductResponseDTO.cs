using System;
using System.Collections.Generic;
using System.Text;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class ProductResponseDTO
    {
        public int Id { get; set; }
        public string ProductName { get; set; } = string.Empty;
        public string Brand { get; set; } = string.Empty;

        public List<string> AvailableColors { get; set; } = new();
        public List<int> AvailableSizes { get; set; } = new();

        public decimal MinPrice { get; set; }

        public string? ThumbnailUrl { get; set; }
    }
}
