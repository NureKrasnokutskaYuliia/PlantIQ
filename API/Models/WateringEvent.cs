using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace API.Models
{
    [Table("watering_events")]
    public class WateringEvent
    {
        [Key]
        [Column("event_id")]
        public int EventId { get; set; }

        [Column("plant_id")]
        public int PlantId { get; set; }

        [ForeignKey(nameof(PlantId))]
        [JsonIgnore]
        public Plant? Plant { get; set; }

        [Column("schedule_id")]
        public int? ScheduleId { get; set; }

        [ForeignKey(nameof(ScheduleId))]
        [JsonIgnore]
        public WateringSchedule? Schedule { get; set; }

        [Column("timestamp")]
        public DateTime Timestamp { get; set; } = DateTime.UtcNow;

        [Column("amount_ml")]
        public int AmountMl { get; set; }

        [Required]
        [Column("mode")]
        public WateringMode Mode { get; set; } = WateringMode.Automatic;

        [Column("duration_seconds")]
        public int? DurationSeconds { get; set; }

        [Column("status")]
        public WateringStatus Status { get; set; } = WateringStatus.Completed;

        [Column("notes")]
        public string? Notes { get; set; }
    }
}
