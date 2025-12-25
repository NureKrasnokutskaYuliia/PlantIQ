using API.DTOs;
using API.Models;
using API.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    [Produces("application/json")]
    public class NotificationsController : ControllerBase
    {
        private readonly INotificationService _notificationService;
        private readonly IPlantService _plantService;
        private readonly IDeviceService _deviceService;

        public NotificationsController(INotificationService notificationService, IPlantService plantService, IDeviceService deviceService)
        {
            _notificationService = notificationService;
            _plantService = plantService;
            _deviceService = deviceService;
        }

        /// <summary>
        /// Get notifications for a specific user.
        /// </summary>
        /// <param name="userId">The ID of the user</param>
        /// <param name="unreadOnly">If true, returns only unread notifications</param>
        /// <returns>A list of notifications</returns>
        [HttpGet("User/{userId}")]
        [ProducesResponseType(typeof(IEnumerable<NotificationResponseDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<NotificationResponseDto>>> GetUserNotifications(int userId, [FromQuery] bool unreadOnly = false)
        {
            var notifications = await _notificationService.GetUserNotificationsAsync(userId, unreadOnly);
            var dtos = notifications.Select(n => MapToResponseDto(n));
            return Ok(dtos);
        }

        /// <summary>
        /// Get the count of unread notifications for a user.
        /// </summary>
        /// <param name="userId">The ID of the user</param>
        /// <returns>Integer count of unread items</returns>
        [HttpGet("User/{userId}/count")]
        [ProducesResponseType(typeof(int), StatusCodes.Status200OK)]
        public async Task<ActionResult<int>> GetUnreadCount(int userId)
        {
            var count = await _notificationService.GetUnreadCountAsync(userId);
            return Ok(count);
        }

        /// <summary>
        /// Mark a notification as read.
        /// </summary>
        /// <param name="id">The ID of the notification</param>
        [HttpPut("{id}/read")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> MarkAsRead(int id)
        {
            await _notificationService.MarkAsReadAsync(id);
            return NoContent();
        }

        /// <summary>
        /// Mark ALL notifications as read for a specific user.
        /// </summary>
        /// <param name="userId">The ID of the user</param>
        [HttpPut("User/{userId}/read-all")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        public async Task<IActionResult> MarkAllAsRead(int userId)
        {
            await _notificationService.MarkAllAsReadAsync(userId);
            return NoContent();
        }

        /// <summary>
        /// Create a new notification.
        /// </summary>
        /// <param name="dto">The notification data</param>
        /// <returns>The created notification object</returns>
        [HttpPost]
        [ProducesResponseType(typeof(NotificationResponseDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<NotificationResponseDto>> CreateNotification(CreateNotificationDto dto)
        {
            var userId = GetUserIdFromClaims();
            if (userId == null) return Unauthorized();

            if (dto.PlantId.HasValue && !await _plantService.PlantExistsAsync(dto.PlantId.Value))
            {
                return BadRequest($"Plant with ID {dto.PlantId.Value} does not exist.");
            }

            if (dto.DeviceId.HasValue && !await _deviceService.DeviceExistsAsync(dto.DeviceId.Value))
            {
                return BadRequest($"Device with ID {dto.DeviceId.Value} does not exist.");
            }

            var notification = new Notification
            {
                UserId = userId.Value,
                PlantId = dto.PlantId,
                DeviceId = dto.DeviceId,
                Type = dto.Type,
                Message = dto.Message,
                Priority = dto.Priority,
                Timestamp = DateTime.UtcNow,
                Read = false
            };

            await _notificationService.CreateNotificationAsync(notification);

            return CreatedAtAction(nameof(GetUserNotifications), new { userId = notification.UserId }, MapToResponseDto(notification));
        }

        private int? GetUserIdFromClaims()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value
                           ?? User.FindFirst("sub")?.Value
                           ?? User.FindFirst("userId")?.Value;

            if (int.TryParse(userIdClaim, out int userId))
                return userId;

            return null;
        }

        private static NotificationResponseDto MapToResponseDto(Notification n)
        {
            return new NotificationResponseDto
            {
                NotificationId = n.NotificationId,
                UserId = n.UserId,
                PlantId = n.PlantId,
                DeviceId = n.DeviceId,
                Timestamp = n.Timestamp,
                Type = n.Type,
                Message = n.Message,
                Read = n.Read,
                Priority = n.Priority,
                ReadAt = n.ReadAt
            };
        }
    }
}