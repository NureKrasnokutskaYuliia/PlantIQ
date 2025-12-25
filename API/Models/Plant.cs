using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace API.Models
{
    [Table("plants")]
    public class Plant
    {
        [Key]
        [Column("plant_id")]
        public int PlantId { get; set; }

        [Column("user_id")]
        public int UserId { get; set; }

        [ForeignKey(nameof(UserId))]
        [JsonIgnore]
        public User? User { get; set; }

        [Column("device_id")]
        public int? DeviceId { get; set; }

        [ForeignKey(nameof(DeviceId))]
        [JsonIgnore]
        public Device? Device { get; set; }

        [Required]
        [MaxLength(100)]
        [Column("name")]
        public string Name { get; set; } = string.Empty;

        [MaxLength(100)]
        [Column("species")]
        public string? Species { get; set; }

        [Column("optimal_moisture_min")]
        public decimal? OptimalMoistureMin { get; set; }

        [Column("optimal_moisture_max")]
        public decimal? OptimalMoistureMax { get; set; }

        [Column("optimal_light_min")]
        public decimal? OptimalLightMin { get; set; }

        [Column("optimal_light_max")]
        public decimal? OptimalLightMax { get; set; }

        [Column("notes")]
        public string? Notes { get; set; }

        [Column("created_at")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [Column("updated_at")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        [Column("is_active")]
        public bool IsActive { get; set; } = true;

        [JsonIgnore]
        public ICollection<WateringSchedule> WateringSchedules { get; set; } = new List<WateringSchedule>();
        [JsonIgnore]
        public ICollection<WateringEvent> WateringEvents { get; set; } = new List<WateringEvent>();
    }
}
