using API.Data;
using API.Models;
using API.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace API.Services
{
    public class SystemSettingsService : ISystemSettingsService
    {
        private readonly AppDbContext _context;

        public SystemSettingsService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<SystemSetting>> GetAllSettingsAsync()
        {
            return await _context.SystemSettings.ToListAsync();
        }

        public async Task<string?> GetValueAsync(string key)
        {
            var setting = await _context.SystemSettings.FirstOrDefaultAsync(s => s.SettingKey == key);
            return setting?.SettingValue;
        }

        public async Task SetValueAsync(string key, string? value, int? updatedByUserId)
        {
            var setting = await _context.SystemSettings.FirstOrDefaultAsync(s => s.SettingKey == key);
            if (setting == null)
            {
                setting = new SystemSetting { SettingKey = key };
                _context.SystemSettings.Add(setting);
            }

            setting.SettingValue = value;
            setting.UpdatedBy = updatedByUserId;
            setting.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();
        }
    }
}