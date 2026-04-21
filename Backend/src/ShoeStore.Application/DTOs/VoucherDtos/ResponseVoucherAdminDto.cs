using System;
using System.Collections.Generic;
using System.Text;

namespace ShoeStore.Application.DTOs.VoucherDtos
{
    public class ResponseVoucherAdminDto
    {
        public string? VoucherName { get; set; }
        public decimal Discount { get; set; } = 0;
        public int? VoucherScope { get; set; }
        public int? DiscountType { get; set; }
        public decimal? MaxPriceDiscount { get; set; }
        public decimal? MinOrderPrice { get; set; }
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
