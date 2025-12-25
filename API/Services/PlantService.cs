using API.Data;
using API.Models;
using API.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace API.Services
{
    public class PlantService : IPlantService
    {
        private readonly AppDbContext _context;

        public PlantService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<Plant>> GetAllAsync()
        {
            return await _context.Plants.ToListAsync();
        }

        public async Task<IEnumerable<Plant>> GetByUserIdAsync(int userId)
        {
            return await _context.Plants.Where(p => p.UserId == userId).ToListAsync();
        }

        public async Task<Plant?> GetByDeviceIdAsync(int deviceId)
        {
            return await _context.Plants.Where(p => p.DeviceId == deviceId).FirstOrDefaultAsync();
        }

        public async Task<Plant?> GetByIdAsync(int id)
        {
            return await _context.Plants
                .Include(p => p.WateringSchedules)
                .FirstOrDefaultAsync(p => p.PlantId == id);
        }

        public async Task<Plant> AddPlantAsync(Plant plant)
        {
            _context.Plants.Add(plant);
            await _context.SaveChangesAsync();
            return plant;
        }

        public async Task UpdatePlantAsync(Plant plant)
        {
            _context.Entry(plant).State = EntityState.Modified;
            await _context.SaveChangesAsync();
        }

        public async Task DeletePlantAsync(int id)
        {
            var plant = await _context.Plants.FindAsync(id);
            if (plant != null)
            {
                _context.Plants.Remove(plant);
                await _context.SaveChangesAsync();
            }
        }

        public async Task<bool> PlantExistsAsync(int id)
        {
            return await _context.Plants.AnyAsync(p => p.PlantId == id);
        }
    }
}