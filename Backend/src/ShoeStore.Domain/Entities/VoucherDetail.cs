using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class VoucherDetail(int id) : Entity<int>(id)
{
    public required int InvoiceId { get; set; }
    public required Invoice Invoice { get; set; }
    public int? VoucherId { get; set; }
    public Voucher? Voucher { get; set; }
    // snapshot how much of discount the voucher applied to the invoice at the time of purchase
    public decimal MoneyDiscount { get; set; } = 0;
}