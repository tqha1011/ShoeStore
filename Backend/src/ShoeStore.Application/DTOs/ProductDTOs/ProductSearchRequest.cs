using System;
using System.Collections.Generic;
using System.Text;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class ProductSearchRequest
    {
        public string? Keyword { get; set; }
        public string? Brand { get; set; }
        public int? ProductId { get; set; }
        public int? ColorId { get; set; }
        public int? SizeId { get; set; }
        public decimal? MinPrice { get; set; }
        public decimal? MaxPrice { get; set; }
        public string? Sort { get; set; }

        private int _pageIndex = 1;
        public int PageIndex
        {
            get => _pageIndex;
            set => _pageIndex = value < 1 ? 1 : value;
        }

        private int _pageSize = 10;
        public int PageSize
        {
            get => _pageSize;
            set => _pageSize = Math.Clamp(value, 0, 50);
        }
    }
}
