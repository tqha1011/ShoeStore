namespace ShoeStore.Application.DTOs.InvoiceDTOs
{
    public interface ICurrentUser
    {
        int? Id { get; }
        bool IsAdmin { get; }
        bool IsAuthenticated { get; }
    }
}
