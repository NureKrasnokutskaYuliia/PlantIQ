using API.Data;
using API.Models;
using API.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace API.Services
{
    public class SensorService : ISensorService
    {
        private readonly AppDbContext _context;

        public SensorService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<SensorData>> GetHistoryByPlantIdAsync(int plantId, int limit = 100)
        {
            return await _context.SensorData
                .Where(s => s.PlantId == plantId)
                .OrderByDescending(s => s.Timestamp)
                .Take(limit)
                .ToListAsync();
        }

        public async Task<SensorData?> GetLatestReadingAsync(int plantId)
        {
            return await _context.SensorData
                .Where(s => s.PlantId == plantId)
                .OrderByDescending(s => s.Timestamp)
                .FirstOrDefaultAsync();
        }

        public async Task AddReadingAsync(SensorData data)
        {
            _context.SensorData.Add(data);
            await _context.SaveChangesAsync();
        }
    }
}