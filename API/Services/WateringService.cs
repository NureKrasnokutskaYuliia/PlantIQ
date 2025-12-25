using API.Data;
using API.Models;
using API.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace API.Services
{
    public class WateringService : IWateringService
    {
        private readonly AppDbContext _context;

        public WateringService(AppDbContext context)
        {
            _context = context;
        }
        public async Task<IEnumerable<WateringSchedule>> GetSchedulesByPlantIdAsync(int plantId)
        {
            return await _context.WateringSchedules.Where(ws => ws.PlantId == plantId).ToListAsync();
        }

        public async Task<WateringSchedule?> GetScheduleByIdAsync(int id)
        {
            return await _context.WateringSchedules.FindAsync(id);
        }

        public async Task<WateringSchedule> CreateScheduleAsync(WateringSchedule schedule)
        {
            _context.WateringSchedules.Add(schedule);
            await _context.SaveChangesAsync();
            return schedule;
        }

        public async Task UpdateScheduleAsync(WateringSchedule schedule)
        {
            _context.Entry(schedule).State = EntityState.Modified;
            await _context.SaveChangesAsync();
        }

        public async Task DeleteScheduleAsync(int id)
        {
            var schedule = await _context.WateringSchedules.FindAsync(id);
            if (schedule != null)
            {
                _context.WateringSchedules.Remove(schedule);
                await _context.SaveChangesAsync();
            }
        }

        // Events
        public async Task<IEnumerable<WateringEvent>> GetEventsByPlantIdAsync(int plantId, int limit = 50)
        {
            return await _context.WateringEvents
                .Where(we => we.PlantId == plantId)
                .OrderByDescending(we => we.Timestamp)
                .Take(limit)
                .ToListAsync();
        }

        public async Task LogWateringEventAsync(WateringEvent wateringEvent)
        {
            _context.WateringEvents.Add(wateringEvent);
            await _context.SaveChangesAsync();
        }
    }
}