using ShoeStore.Application.Interface.AddressInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories
{
    public class AddressRepository(AppDbContext context) : GenericRepository<UserAddress, int>(context), IAddressRepository
    {
        public IQueryable<UserAddress> GetAll()
        {
            throw new NotImplementedException();
        }
    }
}
