using System.Text.Json.Serialization;
using ShoeStore.Application.DTOs;

namespace ShoeStore.Api.JsonSerialize;

[JsonSerializable(typeof(LoginDto))]
public partial class AppJsonSerializeContext : JsonSerializerContext 
{
    
}