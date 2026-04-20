using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace API.DTOs
{
    public class UpdateDeviceTokenDto
    {
        [Required]
        [JsonPropertyName("token")]
        public string Token { get; set; } = string.Empty;
    }
}
