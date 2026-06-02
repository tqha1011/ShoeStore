using Google.Apis.Auth;
using Microsoft.Extensions.Configuration;
using Moq;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface.Strategies;
using ShoeStore.Infrastructure.Authentication.Strategies;

namespace ShoeStore.Tests.Unit.Services.AuthenticationServiceTests;

public class LoginGoogleTests
{
    private const string FakeGoogleToken = "ya29.fake_google_access_token_xyz_123";
    private readonly Mock<IConfiguration> _configuration = new();
    private readonly GoogleAuthStrategy _googleAuthStrategy;
    private readonly Mock<IGoogleValidator> _googleValidator = new();

    public LoginGoogleTests()
    {
        _googleAuthStrategy = new GoogleAuthStrategy(_configuration.Object, _googleValidator.Object);
    }

    [Fact]
    public async Task VerifySocialToken_WhenClientIdIsMissing_ReturnsUnexpected()
    {
        // Arrange
        _configuration.Setup(conf => conf["GoogleAuthentication:ClientId"]).Returns(" ");

        // Act
        var result = await _googleAuthStrategy.VerifySocialToken(FakeGoogleToken, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Google.MissingClientId", result.FirstError.Code);
        _googleValidator.Verify(
            g => g.ValidateTokenAsync(It.IsAny<string>(), It.IsAny<string>(), It.IsAny<CancellationToken>()),
            Times.Never);
    }

    [Fact]
    public async Task VerifySocialToken_WhenPayloadIsNull_ReturnUnauthorized()
    {
        // Arrange
        _configuration.Setup(conf => conf["GoogleAuthentication:ClientId"]).Returns("GoogleClientId");
        _googleValidator
            .Setup(googleValidator =>
                googleValidator.ValidateTokenAsync(It.IsAny<string>(), It.IsAny<string>(),
                    It.IsAny<CancellationToken>())).ReturnsAsync((GooglePayloadDto?)null);

        // Act
        var result = await _googleAuthStrategy.VerifySocialToken(FakeGoogleToken, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Google.EmptyPayload", result.FirstError.Code);
    }

    [Fact]
    public async Task VerifySocialToken_WhenTokenIsInvalid_ReturnsUnauthorized()
    {
        // Arrange
        _configuration.Setup(conf => conf["GoogleAuthentication:ClientId"]).Returns("GoogleClientId");
        var exception = new InvalidJwtException("Invalid token");
        _googleValidator
            .Setup(googleValidator =>
                googleValidator.ValidateTokenAsync(It.IsAny<string>(), It.IsAny<string>(),
                    It.IsAny<CancellationToken>())).ThrowsAsync(exception);

        // Act
        var result = await _googleAuthStrategy.VerifySocialToken(FakeGoogleToken, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Google.InvalidToken", result.FirstError.Code);
    }

    [Fact]
    public async Task VerifySocialToken_WhenServerError_ReturnsUnexpected()
    {
        // Arrange
        _configuration.Setup(conf => conf["GoogleAuthentication:ClientId"]).Returns("GoogleClientId");
        var exception = new Exception("Server error");
        _googleValidator
            .Setup(googleValidator =>
                googleValidator.ValidateTokenAsync(It.IsAny<string>(), It.IsAny<string>(),
                    It.IsAny<CancellationToken>())).ThrowsAsync(exception);

        // Act
        var result = await _googleAuthStrategy.VerifySocialToken(FakeGoogleToken, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Google.VerificationFailed", result.FirstError.Code);
    }

    [Fact]
    public async Task VerifySocialToken_WhenPayloadIsValid_ReturnsSocialUser()
    {
        // Arrange
        _configuration.Setup(conf => conf["GoogleAuthentication:ClientId"]).Returns("GoogleClientId");
        var payload = new GooglePayloadDto("john.doe@gmail.com", "John Doe", "sub-123", null);
        _googleValidator
            .Setup(googleValidator =>
                googleValidator.ValidateTokenAsync(It.IsAny<string>(), It.IsAny<string>(),
                    It.IsAny<CancellationToken>())).ReturnsAsync(payload);

        // Act
        var result = await _googleAuthStrategy.VerifySocialToken(FakeGoogleToken, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        Assert.Equal(payload.Email, result.Value.Email);
        Assert.Equal(payload.Name, result.Value.Username);
    }
}