using API.Data;
using API.Models;
using API.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace API.Services
{
    public class AdminService : IAdminService
    {
        private readonly AppDbContext _context;

        public AdminService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<SystemStatsDto> GetSystemStatisticsAsync()
        {
            return new SystemStatsDto
            {
                TotalUsers = await _context.Users.CountAsync(),
                ActiveDevices = await _context.Devices.CountAsync(d => d.Status == DeviceStatus.Online),
                TotalPlants = await _context.Plants.CountAsync(),
                CriticalAlertsLast24h = await _context.Notifications
                    .CountAsync(n => n.Priority == NotificationPriority.Critical && n.Timestamp > DateTime.UtcNow.AddDays(-1)),
                DatabaseSizeMb = 0
            };
        }

        public async Task BanUserAsync(int userId)
        {
            var user = await _context.Users.FindAsync(userId);
            if (user != null)
            {
                user.IsActive = false;
                await _context.SaveChangesAsync();
            }
        }

        public async Task UnbanUserAsync(int userId)
        {
            var user = await _context.Users.FindAsync(userId);
            if (user != null)
            {
                user.IsActive = true;
                await _context.SaveChangesAsync();
            }
        }

        public async Task SendGlobalNotificationAsync(string message, string priorityString)
        {
            if (!Enum.TryParse<NotificationPriority>(priorityString, true, out var priority))
            {
                priority = NotificationPriority.Normal;
            }

            var users = await _context.Users.Where(u => u.IsActive).ToListAsync();
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
        }
    }
}