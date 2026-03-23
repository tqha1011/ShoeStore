using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Application.DTOs.ProducVariantDTOs;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class UpdateProductDTO : ProductBaseDTO
    {
        public Guid PublicId { get; set; }
        public List<CreateProductVariantDTO> Variants { get; set; } = new List<CreateProductVariantDTO>();
    }
}
