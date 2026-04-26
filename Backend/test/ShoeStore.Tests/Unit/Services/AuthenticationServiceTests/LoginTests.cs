using ErrorOr;
using Microsoft.Extensions.DependencyInjection;
using Moq;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Strategies;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Tests.Unit.Services.AuthenticationServiceTests;

public class LoginTests
{
    private const string ProviderName = "Google";
    private const string FakeGoogleToken = "ya29.fake_google_access_token_xyz_123";
    private readonly AuthService _authService;
    private readonly Mock<ISocialAuthStrategy> _googleAuthStrategy = new();
    private readonly Mock<IPasswordHash> _passwordHash = new();
    private readonly Mock<IKeyedServiceProvider> _serviceProvider = new();
    private readonly Mock<ITokenService> _tokenService = new();
    private readonly Mock<IUnitOfWork> _unitOfWork = new();
    private readonly Mock<IUserRepository> _userRepository = new();

    public LoginTests()
    {
        _authService = new AuthService(_passwordHash.Object, _userRepository.Object, _unitOfWork.Object,
            _tokenService.Object, _serviceProvider.Object);
    }

    [Fact]
    public async Task LoginAsync_WhenEmailOrPasswordIsIncorrect_ReturnUnauthorized()
    {
        var fakeUser = BuildUser();
        var fakeRequest = new LoginDto("t@gmail.com", "fakePass1");

        _userRepository.Setup(repo => repo.GetUserByEmailAsync(fakeRequest.Email, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeUser);

        _passwordHash.Setup(hash => hash.VerifyPassword(fakeRequest.Password, fakeUser.Password)).Returns(false);

        var result = await _authService.LoginAsync(fakeRequest, It.IsAny<CancellationToken>());

        Assert.True(result.IsError);
        Assert.Equal("Invalid.Credential", result.FirstError.Code);
        VerifyDatabaseSafe(Times.Never);
    }

    [Fact]
    public async Task LoginAsync_WhenUserDoesNotExist_ReturnUnauthorized()
    {
        var fakeRequest = new LoginDto("t@gmail.com", "fakePass");

        _userRepository.Setup(repo => repo.GetUserByEmailAsync(fakeRequest.Email, It.IsAny<CancellationToken>()))
            .ReturnsAsync((User?)null);

        var result = await _authService.LoginAsync(fakeRequest, It.IsAny<CancellationToken>());

        Assert.True(result.IsError);
        Assert.Equal("Invalid.Credential", result.FirstError.Code);
        VerifyDatabaseSafe(Times.Never);
    }

    [Fact]
    public async Task LoginWithSocialAsync_WhenStrategyReturnsError_PropagatesError()
    {
        // Arrange
        _serviceProvider
            .Setup(sp => sp.GetRequiredKeyedService(typeof(ISocialAuthStrategy), ProviderName))
            .Returns(_googleAuthStrategy.Object);
        _googleAuthStrategy
            .Setup(strategy => strategy.VerifySocialToken(It.IsAny<string>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(Error.Unauthorized("Google.InvalidToken", "Token Google is invalid or expired"));

        // Act
        var result = await _authService.LoginWithSocialAsync(ProviderName, FakeGoogleToken, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Google.InvalidToken", result.FirstError.Code);
        _userRepository.Verify(repo => repo.GetUserByEmailAsync(It.IsAny<string>(), It.IsAny<CancellationToken>()),
            Times.Never);
        _tokenService.Verify(
            tokenService => tokenService.GenerateToken(It.IsAny<Guid>(), It.IsAny<string>(), It.IsAny<UserRole>()),
            Times.Never);
        VerifyDatabaseSafe(Times.Never);
    }

    // 2 test cases happy path with login by Google
    [Theory]
    [InlineData(UserRole.User)]
    [InlineData(UserRole.Admin)]
    public async Task LoginWithSocialAsync_WhenUserAlreadyExist_ReturnsJwtToken(UserRole role)
    {
        var fakePayLoad = new GooglePayloadDto("t@gmail.com", "tqha", "", "");
        var fakeSocialDto = new SocialUserDto
        {
            Email = fakePayLoad.Email,
            Username = fakePayLoad.Name
        };
        var fakeUser = BuildUser();
        fakeUser.Role = role;
        const string fakeJwtToken = "jwtTokenvalid";
        _serviceProvider
            .Setup(sp => sp.GetRequiredKeyedService(typeof(ISocialAuthStrategy), ProviderName))
            .Returns(_googleAuthStrategy.Object);

        _googleAuthStrategy.Setup(strategy =>
                strategy.VerifySocialToken(It.IsAny<string>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeSocialDto);

        _userRepository.Setup(repo => repo.GetUserByEmailAsync(fakeSocialDto.Email, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeUser);

        _tokenService.Setup(tokenService => tokenService.GenerateToken(It.IsAny<Guid>(), It.IsAny<string>(), role))
            .Returns(fakeJwtToken);

        var result = await _authService.LoginWithSocialAsync(ProviderName, FakeGoogleToken, CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Equal(fakeJwtToken, result.Value);
        VerifyDatabaseSafe(Times.Never);
    }

    [Fact]
    public async Task LoginWithSocialAsync_WhenUserDoesNotExist_ReturnsJwtToken()
    {
        var fakePayLoad = new GooglePayloadDto("t@gmail.com", "tqha", "", "");
        var fakeSocialDto = new SocialUserDto
        {
            Email = fakePayLoad.Email,
            Username = fakePayLoad.Name
        };
        const string fakeJwtToken = "jwtTokenvalid";
        _serviceProvider
            .Setup(sp => sp.GetRequiredKeyedService(typeof(ISocialAuthStrategy), ProviderName))
            .Returns(_googleAuthStrategy.Object);

        _googleAuthStrategy.Setup(strategy =>
                strategy.VerifySocialToken(It.IsAny<string>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeSocialDto);

        _userRepository.Setup(repo => repo.GetUserByEmailAsync(It.IsAny<string>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync((User?)null);

        _tokenService.Setup(tokenService =>
                tokenService.GenerateToken(It.IsAny<Guid>(), It.IsAny<string>(), UserRole.User))
            .Returns(fakeJwtToken);

        var result = await _authService.LoginWithSocialAsync(ProviderName, FakeGoogleToken, CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Equal(fakeJwtToken, result.Value);
        VerifyDatabaseSafe(Times.Once);
    }

    private static User BuildUser()
    {
        return new User
        {
            UserName = "hatran",
            Password = "fakePass",
            Email = "t@gmail.com"
        };
    }

    private void VerifyDatabaseSafe(Func<Times> times)
    {
        _userRepository.Verify(repo => repo.Add(It.IsAny<User>()), times);
        _unitOfWork.Verify(uow => uow.SaveChangesAsync(It.IsAny<CancellationToken>()), times);
    }
}