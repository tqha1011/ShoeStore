using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Diagnostics;
using Microsoft.Extensions.Configuration;
using System.Data;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.DTOs.CheckOutDTOs;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;
using ShoeStore.Infrastructure.Data;
using ShoeStore.Infrastructure.Repositories;

namespace ShoeStore.Tests.Unit.Services.CheckOutServiceTests;

public class CheckoutRetryPersistenceTests
{
    [Theory]
    [InlineData(false)]
    [InlineData(true)]
    public async Task Retry_RevalidatesCart_AndDiscardsFailedTrackedChanges(bool cartConsumed)
    {
        await using var connection = new SqliteConnection("Data Source=:memory:");
        await connection.OpenAsync();
        var conflict = new OneSaveConflict();
        var options = new DbContextOptionsBuilder<AppDbContext>()
            .UseSqlite(connection).AddInterceptors(conflict).Options;
        await using var context = new AppDbContext(options);
        await context.Database.EnsureCreatedAsync();

        var user = new User { UserName = "retry-user", Password = "hash", Email = "retry@example.com" };
        var variant = new ProductVariant
        {
            ProductId = 0, SizeId = 0, ColorId = 0, Stock = 5, Price = 100, IsSelling = true,
            Product = new Product { ProductName = "Retry shoe", Category = new Category { Name = "Retry" } },
            Size = new ProductSize { Size = 42 }, Color = new Color { ColorName = "Black" }
        };
        var voucher = new Voucher
        {
            VoucherName = "Retry voucher", Discount = 10, DiscountType = DiscountType.FixedAmount,
            MaxUsagePerUser = 1, IsDeleted = false,
            ValidFrom = DateTime.UtcNow.AddDays(-1), ValidTo = DateTime.UtcNow.AddDays(1)
        };
        context.AddRange(user, variant, voucher);
        context.Payments.Add(new Payment { Id = 2, Name = "COD" });
        await context.SaveChangesAsync();
        context.CartItems.Add(new CartItem
            { UserId = user.Id, ProductVariantId = variant.Id, Quantity = 2 });
        context.UserVouchers.Add(new UserVoucher { UserId = user.Id, VoucherId = voucher.Id });
        await context.SaveChangesAsync();
        context.ChangeTracker.Clear();
        conflict.Armed = true;

        IUnitOfWork unitOfWork = cartConsumed
            ? new ConsumeCartOnRollback(context) : new UnitOfWork(context);
        var service = new CheckOutService(new ProductVariantRepository(context), unitOfWork,
            new CartItemRepository(context), new InvoiceRepository(context), new UserRepository(context),
            new VoucherRepository(context), new ConfigurationBuilder().Build(), new UserVoucherRepository(context));
        var result = await service.PlaceOrderAsync(new PlaceOrderRequestDto(
                [new CheckOutRequestDto(variant.PublicId, 2)], [voucher.Id], "Retry user", "Ha Noi", 2, "0909"),
            user.PublicId, true, CancellationToken.None);

        // Inspect persisted rows from a separate context, as the next HTTP request would.
        await using var persisted = new AppDbContext(new DbContextOptionsBuilder<AppDbContext>()
            .UseSqlite(connection).Options);

        if (cartConsumed)
        {
            Assert.True(result.IsError);
            Assert.Equal("CartItem.NotFound", result.FirstError.Code);
            Assert.Empty(await persisted.Invoices.ToListAsync());
            Assert.Equal(5, (await persisted.ProductVariants.SingleAsync()).Stock);
            Assert.Equal(0, (await persisted.UserVouchers.SingleAsync()).ReservedCount);
            Assert.Empty(await persisted.CartItems.ToListAsync());
            return;
        }

        Assert.False(result.IsError);
        var invoice = Assert.Single(await persisted.Invoices.Include(x => x.InvoiceDetails)
            .Include(x => x.VoucherDetails).ToListAsync());
        Assert.Equal(2, Assert.Single(invoice.InvoiceDetails).Quantity);
        Assert.Equal(10, Assert.Single(invoice.VoucherDetails).MoneyDiscount);
        Assert.Equal(3, (await persisted.ProductVariants.SingleAsync()).Stock);
        Assert.Empty(await persisted.CartItems.ToListAsync());
        Assert.Equal(1, (await persisted.UserVouchers.SingleAsync()).ReservedCount);
        Assert.Equal(result.Value.OrderCode, invoice.OrderCode);
    }

    private sealed class ConsumeCartOnRollback(AppDbContext context) : IUnitOfWork
    {
        private readonly UnitOfWork _inner = new(context);
        public Task SaveChangesAsync(CancellationToken token = default) => _inner.SaveChangesAsync(token);
        public Task<IDbTransaction?> BeginTransactionAsync(CancellationToken token = default)
            => _inner.BeginTransactionAsync(token);
        public Task CommitTransactionAsync(CancellationToken token = default) => _inner.CommitTransactionAsync(token);
        public async Task RollbackTransactionAsync(CancellationToken token = default)
        {
            await _inner.RollbackTransactionAsync(token);
            // Emulate a competing checkout consuming this cart after our transaction ends.
            await context.CartItems.ExecuteDeleteAsync(token);
        }
        public void ClearTracking() => _inner.ClearTracking();
        public Task<T> ExecuteWithStrategyAsync<T>(Func<Task<T>> action, CancellationToken token = default)
            => _inner.ExecuteWithStrategyAsync(action, token);
    }

    // SQLite verifies transaction/tracking cleanup; PostgreSQL/k6 exercises real xmin conflicts.
    private sealed class OneSaveConflict : SaveChangesInterceptor
    {
        public bool Armed { get; set; }

        public override ValueTask<InterceptionResult<int>> SavingChangesAsync(
            DbContextEventData eventData, InterceptionResult<int> result,
            CancellationToken cancellationToken = default)
        {
            if (Armed)
            {
                Armed = false;
                throw new DbUpdateConcurrencyException("Another buyer changed the variant.");
            }
            return ValueTask.FromResult(result);
        }
    }
}
