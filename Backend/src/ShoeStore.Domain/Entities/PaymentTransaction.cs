using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class PaymentTransaction : Entity<int>
{
    public string RemoteTransactionId { get; set; } = string.Empty;
    public string OrderCode { get; set; } = string.Empty;
    public required int InvoiceId { get; set; }
    public Invoice? Invoice { get; set; }
    public decimal Amount { get; set; }
    public string Content { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public required int PaymentId { get; set; }
    public Payment? Payment { get; set; }
}