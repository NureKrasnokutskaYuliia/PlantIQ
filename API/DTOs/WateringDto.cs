using API.Models;
using System.ComponentModel.DataAnnotations;

namespace API.DTOs
{
    // Watering Schedules
    public class WateringScheduleResponseDto
    {
        public int ScheduleId { get; set; }
        public int PlantId { get; set; }
        public TimeSpan StartTime { get; set; }
        public int IntervalHours { get; set; }
        public int AmountMl { get; set; }
        public bool Enabled { get; set; }
        public int[] DaysOfWeek { get; set; } = Array.Empty<int>();
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }

    public class CreateWateringScheduleDto
    {
        [Required]
        public int PlantId { get; set; }

        [Required]
        public TimeSpan StartTime { get; set; }

        public int IntervalHours { get; set; }
        public int AmountMl { get; set; }
        public bool Enabled { get; set; } = true;
        public int[] DaysOfWeek { get; set; } = Array.Empty<int>();
    }

    public class UpdateWateringScheduleDto
    {
        [Required]
        public TimeSpan StartTime { get; set; }

        public int IntervalHours { get; set; }
        public int AmountMl { get; set; }
        public bool Enabled { get; set; }
        public int[] DaysOfWeek { get; set; } = Array.Empty<int>();
    }

    // Watering Events
    public class WateringEventResponseDto
    {
        public int EventId { get; set; }
        public int PlantId { get; set; }
        public int? ScheduleId { get; set; }
        public DateTime Timestamp { get; set; }
        public int AmountMl { get; set; }
        public WateringMode Mode { get; set; }
        public int? DurationSeconds { get; set; }
        public WateringStatus Status { get; set; }
        public string? Notes { get; set; }
    }

    public class CreateWateringEventDto
    {
        [Required]
        public int PlantId { get; set; }

        public int? ScheduleId { get; set; }
        public int AmountMl { get; set; }

        [Required]
        public WateringMode Mode { get; set; } = WateringMode.Automatic;

        public int? DurationSeconds { get; set; }
        public WateringStatus Status { get; set; } = WateringStatus.Completed;
        public string? Notes { get; set; }
    }
}
