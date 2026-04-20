using API.Data;
using API.Models;
using API.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace API.Services
{
    public class NotificationService : INotificationService
    {
        private readonly AppDbContext _context;
        private readonly IFirebaseService _firebaseService;

        public NotificationService(AppDbContext context, IFirebaseService firebaseService)
        {
            _context = context;
            _firebaseService = firebaseService;
        }

        public async Task<IEnumerable<Notification>> GetUserNotificationsAsync(int userId, bool unreadOnly = false)
        {
            var query = _context.Notifications.Where(n => n.UserId == userId);
            if (unreadOnly)
            {
                query = query.Where(n => !n.Read);
            }
            return await query.OrderByDescending(n => n.Timestamp).ToListAsync();
        }

        public async Task<int> GetUnreadCountAsync(int userId)
        {
            return await _context.Notifications.CountAsync(n => n.UserId == userId && !n.Read);
        }

        public async Task CreateNotificationAsync(Notification notification)
        {
            _context.Notifications.Add(notification);
            await _context.SaveChangesAsync();

            // Send Push Notification
            var user = await _context.Users.FindAsync(notification.UserId);
            if (user != null && !string.IsNullOrEmpty(user.FcmToken))
            {
                var title = mapTypeToTitle(notification.Type);
                var data = new Dictionary<string, string>
                {
                    { "notificationId", notification.NotificationId.ToString() },
                    { "type", notification.Type.ToString() },
                    { "plantId", notification.PlantId?.ToString() ?? "" }
                };

                await _firebaseService.SendNotificationAsync(user.FcmToken, title, notification.Message, data);
            }
        }

        private static string mapTypeToTitle(NotificationType type)
        {
            return type switch
            {
                NotificationType.LowMoisture => "💧 Низька вологість",
                NotificationType.HighMoisture => "💧 Висока вологість",
                NotificationType.LowLight => "☀️ Мало світла",
                NotificationType.HighLight => "☀️ Забагато світла",
                NotificationType.BatteryLow => "🔋 Низький заряд батареї",
                NotificationType.DeviceOffline => "⚠️ Пристрій офлайн",
                NotificationType.WateringComplete => "✅ Полив завершено",
                NotificationType.WateringFailed => "❌ Помилка поливу",
                _ => "🌿 PlantIQ"
            };
        }

        public async Task MarkAsReadAsync(int notificationId)
        {
            var notif = await _context.Notifications.FindAsync(notificationId);
            if (notif != null)
            {
                notif.Read = true;
                notif.ReadAt = DateTime.UtcNow;
                await _context.SaveChangesAsync();
            }
        }

        public async Task MarkAllAsReadAsync(int userId)
        {
            var unread = await _context.Notifications.Where(n => n.UserId == userId && !n.Read).ToListAsync();
            foreach (var n in unread)
            {
                n.Read = true;
                n.ReadAt = DateTime.UtcNow;
            }
            await _context.SaveChangesAsync();
        }

        public async Task DeleteNotificationAsync(int id)
        {
            var notif = await _context.Notifications.FindAsync(id);
            if (notif != null)
            {
                _context.Notifications.Remove(notif);
                await _context.SaveChangesAsync();
            }
        }
    }
}