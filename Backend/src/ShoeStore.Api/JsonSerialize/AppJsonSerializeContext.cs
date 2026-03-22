using System.Text.Json.Serialization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.AuthDTOs;

namespace ShoeStore.Api.JsonSerialize;

[JsonSerializable(typeof(LoginDto))]
[JsonSerializable(typeof(RegisterDto))]
[JsonSerializable(typeof(ProblemDetails))]
[JsonSerializable(typeof(ValidationProblemDetails))]
[JsonSerializable(typeof(IDictionary<string, string[]>))]
[JsonSerializable(typeof(string[]))]
[JsonSerializable(typeof(HttpValidationProblemDetails))]
[JsonSerializable(typeof(GoogleLoginDto))]
public partial class AppJsonSerializeContext : JsonSerializerContext 
{
    
}