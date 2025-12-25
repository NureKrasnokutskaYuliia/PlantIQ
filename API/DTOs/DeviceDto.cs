using API.Models;
using System.ComponentModel.DataAnnotations;

namespace API.DTOs
{
    public class DeviceResponseDto
    {
        public int DeviceId { get; set; }
        public int UserId { get; set; }
        public string Name { get; set; } = string.Empty;
        public string? Model { get; set; }
        public DeviceStatus Status { get; set; }
        public string? FirmwareVersion { get; set; }
        public DateTime? LastSync { get; set; }
        public string? SerialNumber { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }

    public class CreateDeviceDto
    {
        [Required]
        [MaxLength(100)]
        public string Name { get; set; } = string.Empty;

        [MaxLength(100)]
        public string? Model { get; set; }

        [MaxLength(100)]
        public string? SerialNumber { get; set; }
    }

    public class UpdateDeviceDto
    {
        [Required]
        [MaxLength(100)]
        public string Name { get; set; } = string.Empty;

        [MaxLength(100)]
        public string? Model { get; set; }

        public DeviceStatus Status { get; set; }

        [MaxLength(50)]
        public string? FirmwareVersion { get; set; }
    }
}
