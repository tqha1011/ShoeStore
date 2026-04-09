using System.Text.Json;
using ErrorOr;
using Microsoft.Extensions.Configuration;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface.Strategies;

namespace ShoeStore.Infrastructure.Authentication.Strategies;

/// <summary>
/// Apply strategy pattern to handle different social authentication providers.
/// This class implements the ISocialAuthStrategy interface for Facebook authentication.
/// </summary>
public class FacebookAuthStrategy(
    IConfiguration configuration,
    IHttpClientFactory httpClientFactory) : ISocialAuthStrategy
{
    public async Task<ErrorOr<SocialUserDto>> VerifySocialToken(string accessToken,CancellationToken token)
    {
        var url = configuration["FacebookAuthentication:Url"];
        if (string.IsNullOrWhiteSpace(url))
        {
            return Error.Unexpected("Facebook.MissingUrl", "Facebook url is not configured");
        }
        var graphApi = $"{url}me?fields=id,name,email&access_token={accessToken}";
        try
        {
            var response = await httpClientFactory.CreateClient().GetAsync(graphApi, token);
            if (!response.IsSuccessStatusCode)
            {
                return Error.Unauthorized("Facebook.InvalidToken", "Invalid Facebook access token");
            }

            // deserialize into the FacebookLoginDto to get the email
            var json = await JsonSerializer.DeserializeAsync<FacebookLoginDto>(
                await response.Content.ReadAsStreamAsync(token), cancellationToken: token);

            // validate if user don't give permission to access email, if so, return error message to ask for email access permission
            if (json == null || string.IsNullOrEmpty(json.Email))
            {
                return Error.Validation("Facebook.EmailMissing",
                    "This app needs permission to access your email address. Please allow email access and try again.");
            }

            return new SocialUserDto
            {
                Email = json.Email,
                Username = string.IsNullOrEmpty(json.Name) ? json.Email.Split('@')[0] : json.Name,
            };
        }
        catch (Exception ex)
        {
            return Error.Failure("Facebook.Exception", $"Exception occurred while verifying Facebook token: {ex.Message}");
        }
    }
}