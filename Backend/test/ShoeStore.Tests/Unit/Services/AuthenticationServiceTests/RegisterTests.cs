using Microsoft.Extensions.DependencyInjection;
using Moq;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Tests.Unit.Services.AuthenticationServiceTests;

public class RegisterTests
{
    private readonly AuthService _authService;
    private readonly Mock<IPasswordHash> _passwordHash = new();
    private readonly Mock<IKeyedServiceProvider> _serviceProvider = new();
    private readonly Mock<ITokenService> _tokenService = new();
    private readonly Mock<IUnitOfWork> _unitOfWork = new();
    private readonly Mock<IUserRepository> _userRepository = new();

    public RegisterTests()
    {
        _authService = new AuthService(
            _passwordHash.Object,
            _userRepository.Object,
            _unitOfWork.Object,
            _tokenService.Object,
            _serviceProvider.Object);
    }

    [Fact]
    public async Task RegisterAsync_WhenEmailAlreadyExists_ReturnsConflict()
    {
        // Arrange
        var request = new RegisterDto("existing@gmail.com", "StrongPass123", "StrongPass123");
        _userRepository
            .Setup(repo => repo.IsEmailExistAsync(request.Email, It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);

        // Act
        var result = await _authService.RegisterAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Email.Exist", result.FirstError.Code);
        _passwordHash.Verify(hash => hash.HashPassword(It.IsAny<string>()), Times.Never);
        _userRepository.Verify(repo => repo.Add(It.IsAny<User>()), Times.Never);
        _unitOfWork.Verify(uow => uow.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task RegisterAsync_WhenEmailDoesNotExist_CreatesUserAndReturnsCreated()
    {
        // Arrange
        var request = new RegisterDto("newuser@gmail.com", "StrongPass123", "StrongPass123");
        const string hashedPassword = "hashed-password";
        _userRepository
            .Setup(repo => repo.IsEmailExistAsync(request.Email, It.IsAny<CancellationToken>()))
            .ReturnsAsync(false);
        _passwordHash
            .Setup(hash => hash.HashPassword(It.IsAny<string>()))
            .Returns(hashedPassword);

        // Act
        var result = await _authService.RegisterAsync(request, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        _userRepository.Verify(repo => repo.Add(It.Is<User>(u =>
            u.Email == request.Email &&
            u.Password == hashedPassword &&
            u.UserName == "newuser" &&
            u.Role == UserRole.User)), Times.Once);
        _unitOfWork.Verify(uow => uow.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }
}