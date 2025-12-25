using API.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize(Roles = "admin")]
    [Produces("application/json")]
    public class AdminController : ControllerBase
    {
        private readonly IAdminService _adminService;

        public AdminController(IAdminService adminService)
        {
            _adminService = adminService;
        }

        /// <summary>
        /// Get system-wide statistics.
        /// </summary>
        /// <remarks>
        /// Returns aggregated data about users, devices, plants, and alerts.
        /// </remarks>
        /// <returns>System statistics DTO</returns>
        /// <response code="200">Successfully returned statistics</response>
        [HttpGet("stats")]
        [ProducesResponseType(typeof(SystemStatsDto), StatusCodes.Status200OK)]
        public async Task<ActionResult<SystemStatsDto>> GetStats()
        {
            var stats = await _adminService.GetSystemStatisticsAsync();
            return Ok(stats);
        }

        /// <summary>
        /// Ban a user.
        /// </summary>
        /// <remarks>
        /// Deactivates a user account (IsActive = false). The user will not be able to login.
        /// </remarks>
        /// <param name="userId">The ID of the user to ban</param>
        /// <response code="204">User successfully banned</response>
        [HttpPost("users/{userId}/ban")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        public async Task<IActionResult> BanUser(int userId)
        {
            await _adminService.BanUserAsync(userId);
            return NoContent();
        }

        /// <summary>
        /// Unban a user.
        /// </summary>
        /// <remarks>
        /// Reactivates a user account (IsActive = true).
        /// </remarks>
        /// <param name="userId">The ID of the user to unban</param>
        /// <response code="204">User successfully unbanned</response>
        [HttpPost("users/{userId}/unban")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        public async Task<IActionResult> UnbanUser(int userId)
        {
            await _adminService.UnbanUserAsync(userId);
            return NoContent();
        }

        /// <summary>
        /// Send a global notification to ALL users.
        /// </summary>
        /// <remarks>
        /// Creates a system notification for every active user in the database.
        /// Useful for maintenance announcements or critical system alerts.
        /// </remarks>
        /// <param name="request">The notification details</param>
        /// <response code="204">Notifications queued/sent</response>
        /// <response code="400">Invalid priority or message</response>
        [HttpPost("notifications/global")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<IActionResult> SendGlobalNotification([FromBody] GlobalNotificationRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.Message))
            {
                return BadRequest("Message cannot be empty.");
            }

            // Передаємо рядковий пріоритет, сервіс розпарсить його в Enum
            await _adminService.SendGlobalNotificationAsync(request.Message, request.Priority);

            return NoContent();
        }
    }

    // DTO для запиту (можна винести в окремий файл, але для стислості залишив тут)
    public class GlobalNotificationRequest
    {
        public string Message { get; set; } = string.Empty;

        /// <summary>
        /// Priority level: Low, Normal, High, Critical
        /// </summary>
        public string Priority { get; set; } = "Normal";
    }
}