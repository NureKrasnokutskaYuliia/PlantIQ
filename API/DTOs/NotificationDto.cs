using API.Models;
using System.ComponentModel.DataAnnotations;

namespace API.DTOs
{
    public class NotificationResponseDto
    {
        public int NotificationId { get; set; }
        public int UserId { get; set; }
        public int? PlantId { get; set; }
        public int? DeviceId { get; set; }
        public DateTime Timestamp { get; set; }
        public NotificationType Type { get; set; }
        public string Message { get; set; } = string.Empty;
        public bool Read { get; set; }
        public NotificationPriority Priority { get; set; }
        public DateTime? ReadAt { get; set; }
    }

    public class CreateNotificationDto
    {
        public int? PlantId { get; set; }
        public int? DeviceId { get; set; }

        [Required]
        public NotificationType Type { get; set; }

        [Required]
        public string Message { get; set; } = string.Empty;

        public NotificationPriority Priority { get; set; } = NotificationPriority.Normal;
    }
}
