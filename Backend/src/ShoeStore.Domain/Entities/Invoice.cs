using ShoeStore.Domain.Common;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Domain.Entities;

public class Invoice : Entity<int>
{
    public Guid PublicId { get; set; }
    public required int UserId { get; set; }
    public required User User { get; set; }
    public required InvoiceStatus Status { get; set; }
    public required int PaymentId { get; set; }
    public required Payment Payment { get; set; }
    public required decimal FinalPrice { get; set; }
    public required string ShippingAddress { get; set; }
    public required string Phone { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } 
    
    /// <summary>
    /// Gets the collection of all invoice details associated with this invoice.
    /// This collection represents the individual items or products included in the invoice, along with their quantities and unit prices.
    /// Each InvoiceDetail entry corresponds to a specific product variant and its associated information, allowing for detailed tracking of the items purchased in this invoice.
    /// </summary>
    public ICollection<InvoiceDetail> InvoiceDetails { get; set; } =  new List<InvoiceDetail>();
    
    
    /// <summary>
    /// Gets the collection of all vouchers that was applyed to this invoice, along with the details of their usage.
    /// </summary>
    public ICollection<VoucherDetail> VoucherDetails { get; set; } =  new List<VoucherDetail>();
}