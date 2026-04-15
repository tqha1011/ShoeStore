using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface;

public interface IColorRepository : IGenericRepository<Color, int>
{
    Task<List<Color>> GetColorsAsync(CancellationToken token);

    Task<bool> ColorNameExistAsync(string name, CancellationToken token);
}