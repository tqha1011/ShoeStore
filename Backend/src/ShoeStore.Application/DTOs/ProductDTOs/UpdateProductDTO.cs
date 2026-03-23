using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Application.DTOs.ProducVariantDTOs;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class UpdateProductDto : ProductBaseDto
    {
        public Guid PublicId { get; set; }
        public List<CreateProductVariantDto> Variants { get; set; } = new List<CreateProductVariantDto>();
    }
}
