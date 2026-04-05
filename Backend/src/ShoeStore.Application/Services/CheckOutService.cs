using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.ProductInterface;

namespace ShoeStore.Application.Services;

public class CheckOutService(IProductVariantRepository productVariantRepository) : ICheckOutService
{
    public async Task<ErrorOr<CheckOutResponseDto>> CheckOut(List<CheckOutRequestDto> checkOutList,
        CancellationToken token)
    {
        // get variant list by variant id in check out list
        var variantIdList = checkOutList.Select(x => x.VariantId)
            .Distinct()
            .ToList();
        var variantsList = await productVariantRepository.GetListVariantsAsync(variantIdList, token);

        if (variantsList.Count < variantIdList.Count)
            return Error.NotFound("Variant.NotFound", "One or more variants are deleted.");

        // transform a list to a dictionary to optimize performance
        // Use GroupBy to sum the quantity if it has duplicate variantId in check out list
        var variantQuantityById = checkOutList.GroupBy(x => x.VariantId)
            .ToDictionary(x => x.Key, x => x.Sum(y => y.Quantity));

        var items = variantsList.Select(variant =>
            {
                var quantity = variantQuantityById.GetValueOrDefault(variant.PublicId, 0);
                var isOutOfStock = variant.Stock <= 0 || quantity > variant.Stock;
                var subTotal = variant.Price * quantity;

                return new CheckOutItemDto(
                    variant.PublicId,
                    variant.Product?.ProductName ?? string.Empty,
                    variant.Color?.ColorName ?? string.Empty,
                    variant.Size?.Size ?? 0,
                    variant.Price,
                    quantity,
                    variant.Stock,
                    isOutOfStock,
                    subTotal
                );
            })
            .ToList();

        var total = items.Sum(x => x.SubTotal);
        var summary = new CheckOutSummaryDto(total, total);

        var warnings = items.Where(x => x.IsOutOfStock)
            .Select(x => $"{x.ProductName} is only have {x.StockAvailable} items.")
            .ToList();
        var response = new CheckOutResponseDto(items, summary, warnings);
        return response;
    }
}