using System.Text.Json.Serialization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.DTOs.CartItemDTOs;
using ShoeStore.Application.DTOs.RestorePasswordDto;

namespace ShoeStore.Api.JsonSerialize;

/// <summary>
///     Apply JSON Source Generator to enhance the performance of JSON serialization and deserialization for the specified
///     types.
///     This context will be used by the System.Text.Json serializer to generate optimized code at compile time.
///     Improving the efficiency of handling JSON data for these types.
/// </summary>
[JsonSerializable(typeof(LoginDto))]
[JsonSerializable(typeof(RegisterDto))]
[JsonSerializable(typeof(ProblemDetails))]
[JsonSerializable(typeof(ValidationProblemDetails))]
[JsonSerializable(typeof(IDictionary<string, string[]>))]
[JsonSerializable(typeof(string[]))]
[JsonSerializable(typeof(HttpValidationProblemDetails))]
[JsonSerializable(typeof(GoogleLoginDto))]
[JsonSerializable(typeof(EmailVerifyDto))]
[JsonSerializable(typeof(OtpVerifyDto))]
[JsonSerializable(typeof(UpdatePasswordDto))]
public partial class AppJsonSerializeContext : JsonSerializerContext
{
}