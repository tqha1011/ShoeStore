namespace ShoeStore.Application.DTOs.AddressDTOs;

public class UpdateAddressDto
{
    public int ProvinceId { get; set; }
    public int DistrictId { get; set; }
    public int WardId { get; set; }
    public string DetailAddress { get; set; } = string.Empty;
    public bool IsDefault { get; set; } = false;
}
