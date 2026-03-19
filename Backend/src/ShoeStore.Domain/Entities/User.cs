using ShoeStore.Domain.Common;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Domain.Entities;

public class User : Entity<int>
{
    public required string UserName { get; set; }
    public required string Password { get; set; }
    public required string Email { get; set; }
    public DateTime? DateOfBirth { get; set; }
    public string? Address { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UpdatedAt { get; set; }
    
    public UserRole Role { get; set; } = UserRole.Customer;
    
    /// <summary>
    /// Gets the collection of cart items in user's shopping cart
    /// </summary>
    public ICollection<CartItem> CartItems { get; set; } =  new List<CartItem>();
    
    /// <summary>
    /// Gets the collection of invoices associated with the user, representing their purchase history and transactions.
    /// </summary>
    public ICollection<Invoice> Invoices { get; set; } = new List<Invoice>();
    
    /// <summary>
    /// Gets the collection of voucher associated with user
    /// </summary>
    public ICollection<UserVoucher> UserVouchers { get; set; } = new List<UserVoucher>();
}
