using API.DTOs;

namespace API.Services.Interfaces
{
    public interface IAdminService
    {
        Task<SystemStatisticsDto> GetSystemStatisticsAsync();

        Task BanUserAsync(int userId);
        Task UnbanUserAsync(int userId);

        Task SendGlobalNotificationAsync(string message, string priority);
    }

}