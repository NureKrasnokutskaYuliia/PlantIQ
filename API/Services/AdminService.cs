using API.Data;
using API.Models;
using API.DTOs;
using API.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace API.Services
{
    public class AdminService : IAdminService
    {
        private readonly AppDbContext _context;
        private readonly IFirebaseService _firebaseService;

        public AdminService(AppDbContext context, IFirebaseService firebaseService)
        {
            _context = context;
            _firebaseService = firebaseService;
        }

        public async Task<API.DTOs.SystemStatisticsDto> GetSystemStatisticsAsync()
        {
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
            
            // 1. Зберігаємо в базу даних для внутрішнього перегляду
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

            // 2. Відправляємо реальні PUSH сповіщення через Firebase
            foreach (var user in users.Where(u => !string.IsNullOrEmpty(u.FcmToken)))
            {
                await _firebaseService.SendNotificationAsync(
                    user.FcmToken!,
                    "PlantIQ Global Alert",
                    message
                );
            }
        }
    }
}