using System.Text.Json;
using Microsoft.Extensions.Logging;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;

namespace ShoeStore.Infrastructure.ExternalServices;

public class ProvinceApiService(HttpClient httpClient, ILogger<ProvinceApiService> logger) : IProvinceApiService
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

            return result?.Name is { } name ? (true, name) : (false, string.Empty);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Failed to fetch province with code {Code} from the external API.", code);
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

            return result?.Name is { } name ? (true, name, result.ProvinceCode) : (false, string.Empty, 0);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Failed to fetch ward with code {Code} from the external API.", code);
            return (false, string.Empty, 0);
        }
    }
}
