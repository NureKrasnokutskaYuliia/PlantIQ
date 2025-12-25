using API.Models;

namespace API.Services.Interfaces
{
    public interface ISensorService
    {
        // Отримати історію для графіка
        Task<IEnumerable<SensorData>> GetHistoryByPlantIdAsync(int plantId, int limit = 100);

        // Останній показник (для dashboard)
        Task<SensorData?> GetLatestReadingAsync(int plantId);

        // Основний метод: зберігає дані + перевіряє тригери сповіщень
        Task AddReadingAsync(SensorData data);
    }
}