using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace API.Models
{
    [Table("notifications")]
    public class Notification
    {
        [Key]
        [Column("notification_id")]
        public int NotificationId { get; set; }

        [Column("user_id")]
        public int UserId { get; set; }

        [ForeignKey(nameof(UserId))]
        [JsonIgnore]
        public User? User { get; set; }

        [Column("plant_id")]
        public int? PlantId { get; set; }

        [ForeignKey(nameof(PlantId))]
        [JsonIgnore]
        public Plant? Plant { get; set; }

        [Column("device_id")]
        public int? DeviceId { get; set; }

        [ForeignKey(nameof(DeviceId))]
        [JsonIgnore]
        public Device? Device { get; set; }

        [Column("timestamp")]
        public DateTime Timestamp { get; set; } = DateTime.UtcNow;

        [Required]
        [Column("type")]
        public NotificationType Type { get; set; } = NotificationType.System;

        [Required]
        [Column("message")]
        public string Message { get; set; } = string.Empty;

        [Column("read")]
        public bool Read { get; set; } = false;

        [Column("priority")]
        public NotificationPriority Priority { get; set; } = NotificationPriority.Normal;

        [Column("read_at")]
        public DateTime? ReadAt { get; set; }
    }
}
