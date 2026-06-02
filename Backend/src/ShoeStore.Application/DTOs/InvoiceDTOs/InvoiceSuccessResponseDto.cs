using ShoeStore.Application.DTOs.InvoiceDetailDTOs;

namespace ShoeStore.Application.DTOs.InvoiceDTOs;

public sealed class InvoiceListSuccessResponseDto
{
    public string Message { get; init; } = string.Empty;
    public PageResult<InvoiceResponseDto> Data { get; init; } = new();
}

public sealed class InvoiceDetailsSuccessResponseDto
{
    public string Message { get; init; } = string.Empty;
    public IEnumerable<InvoiceDetailResponseDto> Data { get; init; } = [];
}