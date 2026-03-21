using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class InvoiceDetail : Entity<int>
{
    public Guid PublicId { get; set; }
    public int InvoiceId { get; set; }
    public required Invoice Invoice { get; set; }
    public int ProductVariantId { get; set; }
    public required ProductVariant ProductVariant { get; set; }
    public int Quantity { get; set; }
    public decimal UnitPrice { get; set; }
}