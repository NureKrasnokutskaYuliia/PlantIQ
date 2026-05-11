using API.Data;
using API.Models;
using API.DTOs;
using API.Services.Interfaces;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace API.Services
{
    public class AdminService : IAdminService
    {
        private readonly AppDbContext _context;
        private readonly IFirebaseService _firebaseService;
        private readonly ILogger<AdminService> _logger;

        public AdminService(AppDbContext context, IFirebaseService firebaseService, ILogger<AdminService> logger)
        {
            _context = context;
            _firebaseService = firebaseService;
            _logger = logger;
        }

        public async Task<API.DTOs.SystemStatisticsDto> GetSystemStatisticsAsync()
        {
            await CleanupAdminDataInternalAsync();

            return new API.DTOs.SystemStatisticsDto
            {
                TotalUsers = await _context.Users.CountAsync(),
                TotalDevices = await _context.Devices.CountAsync(),
                TotalPlants = await _context.Plants.CountAsync(),
                CriticalAlertsLast24h = await _context.Notifications
                    .CountAsync(n => n.Priority == NotificationPriority.Critical && n.Timestamp > DateTime.UtcNow.AddDays(-1)),
                DatabaseSizeMb = 0
            };
        }

        private async Task CleanupAdminDataInternalAsync()
        {
            var admins = await _context.Users.Where(u => u.Role == UserRole.Admin).ToListAsync();
            foreach (var admin in admins)
            {
                var adminPlants = await _context.Plants.Where(p => p.UserId == admin.UserId).ToListAsync();
                if (adminPlants.Any())
                {
                    _context.Plants.RemoveRange(adminPlants);
                    _logger.LogInformation("Cleanup: Removed {Count} plants from Admin ID {Id}", adminPlants.Count, admin.UserId);
                }

                var adminDevices = await _context.Devices.Where(d => d.UserId == admin.UserId).ToListAsync();
                if (adminDevices.Any())
                {
                    _context.Devices.RemoveRange(adminDevices);
                    _logger.LogInformation("Cleanup: Removed {Count} devices from Admin ID {Id}", adminDevices.Count, admin.UserId);
                }
            }
            await _context.SaveChangesAsync();
        }

        public async Task BanUserAsync(int userId)
        {
            var user = await _context.Users.FindAsync(userId);
            if (user != null)
            {
                user.IsActive = false;
                await _context.SaveChangesAsync();
                _logger.LogInformation("User {UserId} has been banned.", userId);
            }
        }

        public async Task UnbanUserAsync(int userId)
        {
            var user = await _context.Users.FindAsync(userId);
            if (user != null)
            {
                user.IsActive = true;
                await _context.SaveChangesAsync();
                _logger.LogInformation("User {UserId} has been unbanned.", userId);
            }
        }

        public async Task SendGlobalNotificationAsync(string message, string priorityString)
        {
            if (!Enum.TryParse<NotificationPriority>(priorityString, true, out var priority))
            {
                priority = NotificationPriority.Normal;
            }

            var users = await _context.Users.Where(u => u.IsActive).ToListAsync();
            _logger.LogInformation("Starting global notification broadcast to {Count} active users.", users.Count);

            var notifications = users.Select(u => new Notification
            {
                UserId = u.UserId,
                Type = NotificationType.System,
                Priority = priority,
                Message = message,
                Timestamp = DateTime.UtcNow,
                Read = false
            });

            _context.Notifications.AddRange(notifications);
            await _context.SaveChangesAsync();
            _logger.LogInformation("Notifications saved to database for {Count} users.", users.Count);

            var usersWithToken = users.Where(u => !string.IsNullOrEmpty(u.FcmToken)).ToList();
            _logger.LogInformation("Found {Count} users with valid FCM device tokens.", usersWithToken.Count);

            foreach (var user in usersWithToken)
            {
                try
                {
                    await _firebaseService.SendNotificationAsync(
                        user.FcmToken!,
                        "PlantIQ",
                        message
                    );
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Exception in AdminService during push send to User ID: {UserId}", user.UserId);
                }
            }

            if (usersWithToken.Count == 0)
            {
                _logger.LogWarning("Global notification sent to 0 devices because no FCM tokens were found in the database. Ask users to Re-Login in the mobile app.");
            }
        }
    }
}