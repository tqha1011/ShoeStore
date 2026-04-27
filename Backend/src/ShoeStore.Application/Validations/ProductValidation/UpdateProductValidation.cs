using FluentValidation;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Validations.ProductValidation;

public class UpdateProductValidation : ProductBaseValidation<UpdateProductDto>
{
    public UpdateProductValidation()
    {
        
    }
}