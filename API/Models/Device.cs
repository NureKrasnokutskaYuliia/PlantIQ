using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace API.Models
{
    [Table("devices")]
    public class Device
    {
        [Key]
        [Column("device_id")]
        public int DeviceId { get; set; }

        [Column("user_id")]
        public int UserId { get; set; }

        [ForeignKey(nameof(UserId))]
        [JsonIgnore]
        public User? User { get; set; }

        [Required]
        [MaxLength(100)]
        [Column("name")]
        public string Name { get; set; } = string.Empty;

        [MaxLength(100)]
        [Column("model")]
        public string? Model { get; set; }

        [Required]
        [Column("status")]
        public DeviceStatus Status { get; set; } = DeviceStatus.Offline;

        [MaxLength(50)]
        [Column("firmware_version")]
        public string? FirmwareVersion { get; set; }

        [Column("last_sync")]
        public DateTime? LastSync { get; set; }

        [MaxLength(100)]
        [Column("serial_number")]
        public string? SerialNumber { get; set; }

        [Column("created_at")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [Column("updated_at")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        [JsonIgnore]
        public ICollection<Plant> Plants { get; set; } = new List<Plant>();
        [JsonIgnore]
        public ICollection<SensorData> SensorReadings { get; set; } = new List<SensorData>();
    }
}
