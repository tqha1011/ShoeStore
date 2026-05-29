namespace ShoeStore.Application.Interface.AddressInterface;

public interface IProvinceApiService
{
    Task<(bool IsValid, string Name)> GetProvinceAsync(int code, CancellationToken token);
    Task<(bool IsValid, string Name, int ProvinceCode)> GetWardAsync(int code, CancellationToken token);
}
