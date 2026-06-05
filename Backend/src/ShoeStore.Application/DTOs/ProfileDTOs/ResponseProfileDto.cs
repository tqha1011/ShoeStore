namespace ShoeStore.Application.DTOs.ProfileDTOs;

public class ResponseProfileDto
{
    public Guid UserGuid { get; set; }
    public string UserName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;

    public string AvatarUrl { get; set; } = string.Empty;
    public DateTime? DateOfBirth { get; set; }
}