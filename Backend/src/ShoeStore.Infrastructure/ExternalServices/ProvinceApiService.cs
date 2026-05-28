using System.Text.Json;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;

namespace ShoeStore.Infrastructure.ExternalServices;

public class ProvinceApiService(HttpClient httpClient) : IProvinceApiService
{
    public async Task<(bool IsValid, string Name)> GetProvinceAsync(int code, CancellationToken token)
    {
        try
        {
            var response = await httpClient.GetAsync($"p/{code}", token);
            if (!response.IsSuccessStatusCode)
                return (false, string.Empty);

            var result = await JsonSerializer.DeserializeAsync<ProvinceApiResponseDto>(
                await response.Content.ReadAsStreamAsync(token), cancellationToken: token);

            return result is not null ? (true, result.Name) : (false, string.Empty);
        }
        catch
        {
            return (false, string.Empty);
        }
    }

    public async Task<(bool IsValid, string Name, int ProvinceCode)> GetWardAsync(int code, CancellationToken token)
    {
        try
        {
            var response = await httpClient.GetAsync($"w/{code}", token);
            if (!response.IsSuccessStatusCode)
                return (false, string.Empty, 0);

            var result = await JsonSerializer.DeserializeAsync<WardApiResponseDto>(
                await response.Content.ReadAsStreamAsync(token), cancellationToken: token);

            return result is not null ? (true, result.Name, result.ProvinceCode) : (false, string.Empty, 0);
        }
        catch
        {
            return (false, string.Empty, 0);
        }
    }
}
