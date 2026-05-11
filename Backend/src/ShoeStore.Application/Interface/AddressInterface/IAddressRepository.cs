using ShoeStore.Domain.Entities;
using ShoeStore.Application.Interface.Common;

namespace ShoeStore.Application.Interface.AddressInterface
{
    public interface IAddressRepository : IGenericRepository<UserAddress, int>
    {
        IQueryable<UserAddress> GetAll(Guid userGuid);
    }
}
