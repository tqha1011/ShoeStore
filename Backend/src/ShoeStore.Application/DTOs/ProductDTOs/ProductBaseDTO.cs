using System;
using System.Collections.Generic;
using System.Text;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class ProductBaseDto
    {
        public required int Id { get; set; }
        public required string ProductName { get; set; }
        public string? Brand { get; set; }
    }
}
