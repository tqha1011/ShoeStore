
namespace ShoeStore.Application.DTOs.AddressDTOs
{
    public class CreateAddressDto
    {
        public string Province { get; set; } = string.Empty;
        public string District { get; set; } = string.Empty;
        public string DetailAddress { get; set; } = string.Empty;
        public bool IsDefault { get; set; } = false;
    }
}
