using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class UpdateProductDto : ProductBaseDto
    {
        public Guid PublicId { get; set; }
        public List<UpdateProductVariantDto> Variants { get; set; } = new List<UpdateProductVariantDto>();
    }
}
