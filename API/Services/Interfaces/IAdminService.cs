namespace API.Services.Interfaces
{
    public interface IAdminService
    {
        // Статистика системи (Dashboard)
        Task<SystemStatsDto> GetSystemStatisticsAsync();

        // Керування доступом
        Task BanUserAsync(int userId);
        Task UnbanUserAsync(int userId);

        // Глобальні дії
        Task SendGlobalNotificationAsync(string message, string priority);
    }

    // DTO можна залишити тут або винести в окремий файл
    public class SystemStatsDto
    {
        public int TotalUsers { get; set; }
        public int ActiveDevices { get; set; }
        public int TotalPlants { get; set; }
        public int CriticalAlertsLast24h { get; set; }
        public double DatabaseSizeMb { get; set; }
    }
}