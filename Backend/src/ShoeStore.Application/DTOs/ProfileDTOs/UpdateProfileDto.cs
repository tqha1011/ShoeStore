namespace ShoeStore.Application.DTOs.ProfileDTOs;

public class UpdateProfileDto
{
    public string UserName { get; set; } = string.Empty;
    public DateTime? DateOfBirth { get; set; }
    public string? AvatarUrl { get; set; }
}