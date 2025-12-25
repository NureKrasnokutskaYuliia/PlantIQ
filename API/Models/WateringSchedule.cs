using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace API.Models
{
    [Table("watering_schedules")]
    public class WateringSchedule
    {
        [Key]
        [Column("schedule_id")]
        public int ScheduleId { get; set; }

        [Column("plant_id")]
        public int PlantId { get; set; }

        [ForeignKey(nameof(PlantId))]
        [JsonIgnore]
        public Plant? Plant { get; set; }

        [Required]
        [Column("start_time")]
        public TimeSpan StartTime { get; set; }

        [Column("interval_hours")]
        public int IntervalHours { get; set; }

        [Column("amount_ml")]
        public int AmountMl { get; set; }

        [Column("enabled")]
        public bool Enabled { get; set; } = true;

        [Column("days_of_week")]
        public int[] DaysOfWeek { get; set; } = Array.Empty<int>();

        [Column("created_at")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [Column("updated_at")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }
}
