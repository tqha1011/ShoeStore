using System.Text.Json.Serialization;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.AuthDTOs;

namespace ShoeStore.Api.JsonSerialize;

[JsonSerializable(typeof(LoginDto))]
[JsonSerializable(typeof(RegisterDto))]
public partial class AppJsonSerializeContext : JsonSerializerContext 
{
    
}