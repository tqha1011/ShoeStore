
namespace ShoeStore.Application.DTOs.VoucherDtos
{
    public class ResponseVoucherUserDto
    {
        public Guid VoucherGuid { get; set; }
        public string VoucherName { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
        public decimal Discount { get; set; } = 0;
        private DateTime? _validFrom;
        public DateTime? ValidFrom
        {
            get => _validFrom;
            set => _validFrom = value.HasValue ? DateTime.SpecifyKind(value.Value, DateTimeKind.Utc) : null;
        }

        private DateTime? _validTo;
        public DateTime? ValidTo
        {
            get => _validTo;
            set => _validTo = value.HasValue ? DateTime.SpecifyKind(value.Value, DateTimeKind.Utc) : null;
        }
    }
}
