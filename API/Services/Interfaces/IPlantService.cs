using API.Models;

namespace API.Services.Interfaces
{
    public interface IPlantService
    {
        Task<IEnumerable<Plant>> GetAllAsync();
        Task<IEnumerable<Plant>> GetByUserIdAsync(int userId);
        Task<Plant?> GetByDeviceIdAsync(int deviceId);
        Task<Plant?> GetByIdAsync(int id);
        Task<Plant> AddPlantAsync(Plant plant);
        Task UpdatePlantAsync(Plant plant);
        Task DeletePlantAsync(int id);
        Task<bool> PlantExistsAsync(int id);
    }
}