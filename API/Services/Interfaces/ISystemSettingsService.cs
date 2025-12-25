using API.Models;

namespace API.Services.Interfaces
{
    public interface ISystemSettingsService
    {
        Task<IEnumerable<SystemSetting>> GetAllSettingsAsync();
        Task<string?> GetValueAsync(string key);
        Task SetValueAsync(string key, string? value, int? updatedByUserId);
    }
}