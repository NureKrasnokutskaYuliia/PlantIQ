using API.Data;
using API.Models;
using API.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace API.Services
{
    public class DeviceService : IDeviceService
    {
        private readonly AppDbContext _context;

        public DeviceService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<Device>> GetAllAsync()
        {
            return await _context.Devices.Include(d => d.User).ToListAsync();
        }

        public async Task<IEnumerable<Device>> GetByUserIdAsync(int userId)
        {
            return await _context.Devices.Where(d => d.UserId == userId).ToListAsync();
        }

        public async Task<Device?> GetByIdAsync(int id)
        {
            return await _context.Devices.Include(d => d.Plants).FirstOrDefaultAsync(d => d.DeviceId == id);
        }

        public async Task<Device?> GetBySerialNumberAsync(string serialNumber)
        {
            return await _context.Devices.FirstOrDefaultAsync(d => d.SerialNumber == serialNumber);
        }

        public async Task<Device> CreateDeviceAsync(Device device)
        {
            _context.Devices.Add(device);
            await _context.SaveChangesAsync();
            return device;
        }

        public async Task UpdateDeviceAsync(Device device)
        {
            _context.Entry(device).State = EntityState.Modified;
            await _context.SaveChangesAsync();
        }

        public async Task UpdateDeviceStatusAsync(int deviceId, DeviceStatus status)
        {
            var device = await _context.Devices.FindAsync(deviceId);
            if (device != null)
            {
                device.Status = status;
                device.LastSync = DateTime.UtcNow;
                await _context.SaveChangesAsync();
            }
        }

        public async Task DeleteDeviceAsync(int id)
        {
            var device = await _context.Devices.FindAsync(id);
            if (device != null)
            {
                _context.Devices.Remove(device);
                await _context.SaveChangesAsync();
            }
        }

        public async Task<bool> DeviceExistsAsync(int id)
        {
            return await _context.Devices.AnyAsync(d => d.DeviceId == id);
        }
    }
}