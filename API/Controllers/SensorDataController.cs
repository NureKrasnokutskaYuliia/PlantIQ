using API.DTOs;
using API.Models;
using API.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    [Produces("application/json")]
    public class SensorDataController : ControllerBase
    {
        private readonly ISensorService _sensorService;
        private readonly IPlantService _plantService;
        private readonly IDeviceService _deviceService;
        private readonly INotificationService _notificationService;

        public SensorDataController(ISensorService sensorService, IPlantService plantService, IDeviceService deviceService, INotificationService notificationService)
        {
            _sensorService = sensorService;
            _plantService = plantService;
            _deviceService = deviceService;
            _notificationService = notificationService;
        }

        /// <summary>
        /// Retrieve sensor data for a specific plant.
        /// </summary>
        /// <param name="plantId">The ID of the plant to retrieve data for</param>
        /// <param name="limit">The maximum number of records to return (default is 100)</param>
        /// <returns>A list of sensor data entries</returns>
        [HttpGet("Plant/{plantId}")]
        [ProducesResponseType(typeof(IEnumerable<SensorDataResponseDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<SensorDataResponseDto>>> GetSensorDataByPlant(int plantId, [FromQuery] int limit = 100)
        {
            var data = await _sensorService.GetHistoryByPlantIdAsync(plantId, limit);
            var dtos = data.Select(d => MapToResponseDto(d));
            return Ok(dtos);
        }

        /// <summary>
        /// Upload new sensor data.
        /// </summary>
        /// <param name="dto">The sensor data object</param>
        /// <returns>The created sensor data object</returns>
        [HttpPost]
        [ProducesResponseType(typeof(SensorDataResponseDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<SensorDataResponseDto>> PostSensorData(CreateSensorDataDto dto)
        {
            if (!await _plantService.PlantExistsAsync(dto.PlantId))
            {
                return BadRequest($"Plant with ID {dto.PlantId} does not exist.");
            }

            if (!await _deviceService.DeviceExistsAsync(dto.DeviceId))
            {
                return BadRequest($"Device with ID {dto.DeviceId} does not exist.");
            }

            var sensorData = new SensorData
            {
                PlantId = dto.PlantId,
                DeviceId = dto.DeviceId,
                SoilMoisture = dto.SoilMoisture,
                LightIntensity = dto.LightIntensity,
                BatteryLevel = dto.BatteryLevel,
                Timestamp = DateTime.UtcNow
            };

            await _sensorService.AddReadingAsync(sensorData);

            var plant = await _plantService.GetByIdAsync(dto.PlantId);
            if (plant != null)
            {
                if (plant.OptimalMoistureMin.HasValue && dto.SoilMoisture < plant.OptimalMoistureMin.Value)
                {
                    await _notificationService.CreateNotificationAsync(new Notification
                    {
                        UserId = plant.UserId,
                        PlantId = plant.PlantId,
                        DeviceId = dto.DeviceId,
                        Type = NotificationType.LowMoisture,
                        Priority = NotificationPriority.High,
                        Message = $"Soil moisture is low ({dto.SoilMoisture}%). Optimal minimum is {plant.OptimalMoistureMin.Value}%.",
                        Timestamp = DateTime.UtcNow
                    });
                }
                else if (plant.OptimalMoistureMax.HasValue && dto.SoilMoisture > plant.OptimalMoistureMax.Value)
                {
                    await _notificationService.CreateNotificationAsync(new Notification
                    {
                        UserId = plant.UserId,
                        PlantId = plant.PlantId,
                        DeviceId = dto.DeviceId,
                        Type = NotificationType.HighMoisture,
                        Priority = NotificationPriority.Normal,
                        Message = $"Soil moisture is too high ({dto.SoilMoisture}%). Optimal maximum is {plant.OptimalMoistureMax.Value}%.",
                        Timestamp = DateTime.UtcNow
                    });
                }
                if (plant.OptimalLightMin.HasValue && dto.LightIntensity < plant.OptimalLightMin.Value)
                {
                    await _notificationService.CreateNotificationAsync(new Notification
                    {
                        UserId = plant.UserId,
                        PlantId = plant.PlantId,
                        DeviceId = dto.DeviceId,
                        Type = NotificationType.LowLight,
                        Priority = NotificationPriority.Normal,
                        Message = $"Light intensity is too low ({dto.LightIntensity}%). Optimal minimum is {plant.OptimalLightMin.Value}%.",
                        Timestamp = DateTime.UtcNow
                    });
                }
                else if (plant.OptimalLightMax.HasValue && dto.LightIntensity > plant.OptimalLightMax.Value)
                {
                    await _notificationService.CreateNotificationAsync(new Notification
                    {
                        UserId = plant.UserId,
                        PlantId = plant.PlantId,
                        DeviceId = dto.DeviceId,
                        Type = NotificationType.HighLight,
                        Priority = NotificationPriority.Normal,
                        Message = $"Light intensity is too high ({dto.LightIntensity}%). Optimal maximum is {plant.OptimalLightMax.Value}%.",
                        Timestamp = DateTime.UtcNow
                    });
                }

                if (dto.BatteryLevel.HasValue && dto.BatteryLevel.Value < 20)
                {
                    await _notificationService.CreateNotificationAsync(new Notification
                    {
                        UserId = plant.UserId,
                        PlantId = plant.PlantId,
                        DeviceId = dto.DeviceId,
                        Type = NotificationType.BatteryLow,
                        Priority = NotificationPriority.Critical,
                        Message = $"Device battery is low ({dto.BatteryLevel.Value}%). Please recharge.",
                        Timestamp = DateTime.UtcNow
                    });
                }
            }

            return CreatedAtAction(nameof(GetSensorDataByPlant), new { plantId = sensorData.PlantId }, MapToResponseDto(sensorData));
        }

        private static SensorDataResponseDto MapToResponseDto(SensorData data)
        {
            return new SensorDataResponseDto
            {
                DataId = data.DataId,
                PlantId = data.PlantId,
                DeviceId = data.DeviceId,
                Timestamp = data.Timestamp,
                SoilMoisture = data.SoilMoisture,
                LightIntensity = data.LightIntensity,
                BatteryLevel = data.BatteryLevel
            };
        }
    }
}