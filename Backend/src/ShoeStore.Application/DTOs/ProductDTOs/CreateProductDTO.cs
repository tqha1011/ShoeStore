using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Application.DTOs.ProducVariantDTOs;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class CreateProductDto : ProductBaseDto
    {
        public List<CreateProductVariantDto> Variants { get; set; } = new List<CreateProductVariantDto>();
    }
}
