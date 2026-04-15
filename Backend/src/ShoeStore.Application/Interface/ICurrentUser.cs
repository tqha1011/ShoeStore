namespace ShoeStore.Application.Interface
{
    public interface ICurrentUser
    {
        Guid? Id { get; }
        bool IsAdmin { get; }
        bool IsAuthenticated { get; }
    }
}
