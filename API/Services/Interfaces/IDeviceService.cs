using API.Models;

namespace API.Services.Interfaces
{
    public interface IDeviceService
    {
        Task<IEnumerable<Device>> GetAllAsync();
        Task<IEnumerable<Device>> GetByUserIdAsync(int userId);
        Task<Device?> GetByIdAsync(int id);
        Task<Device?> GetBySerialNumberAsync(string serialNumber);
        Task<Device> CreateDeviceAsync(Device device);
        Task UpdateDeviceAsync(Device device);
        Task UpdateDeviceStatusAsync(int deviceId, DeviceStatus status); // Специфічний метод
        Task DeleteDeviceAsync(int id);
        Task<bool> DeviceExistsAsync(int id);
    }
}