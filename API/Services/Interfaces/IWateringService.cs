using API.Models;

namespace API.Services.Interfaces
{
    public interface IWateringService
    {
        // Розклади (Schedules)
        Task<IEnumerable<WateringSchedule>> GetSchedulesByPlantIdAsync(int plantId);
        Task<WateringSchedule?> GetScheduleByIdAsync(int id);
        Task<WateringSchedule> CreateScheduleAsync(WateringSchedule schedule);
        Task UpdateScheduleAsync(WateringSchedule schedule);
        Task DeleteScheduleAsync(int id);

        // Події (Events - Logs)
        Task<IEnumerable<WateringEvent>> GetEventsByPlantIdAsync(int plantId, int limit = 50);
        Task LogWateringEventAsync(WateringEvent wateringEvent);
    }
}