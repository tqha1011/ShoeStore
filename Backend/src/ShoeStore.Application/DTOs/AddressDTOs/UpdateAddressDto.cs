using System;
using System.Collections.Generic;
using System.Text;

namespace ShoeStore.Application.DTOs.AddressDTOs
{
    public class UpdateAddressDto
    {
        public string Province { get; set; } = string.Empty;
        public string District { get; set; } = string.Empty;
        public string DetailAddress { get; set; } = string.Empty;
    }
}
