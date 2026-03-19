namespace ShoeStore.Application.Interface;

public interface IPasswordHash
{
    string HashPassword(string password);
    bool VerifyPassword(string password, string hashedPassword);
}