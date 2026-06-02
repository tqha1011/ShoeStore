namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record MasterDataResultDto(string Status, string Message);

public sealed record ColorResultDto(int ColorId, string ColorName);

public sealed record SizeResultDto(int SizeId, decimal Size);

public sealed record ColorDataResultDto(string Status, string Message, List<ColorResultDto> Colors);

public sealed record SizeDataResultDto(string Status, string Message, List<SizeResultDto> Sizes);