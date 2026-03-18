using System.Text.Json.Serialization;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.AuthDTOs;

namespace ShoeStore.Api.JsonSerialize;

[JsonSerializable(typeof(LoginDto))]
public partial class AppJsonSerializeContext : JsonSerializerContext 
{
    
}