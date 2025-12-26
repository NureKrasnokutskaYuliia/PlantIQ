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
    public class WateringEventsController : ControllerBase
    {
        private readonly IWateringService _wateringService;
        private readonly IPlantService _plantService;
        private readonly INotificationService _notificationService;

        public WateringEventsController(IWateringService wateringService, IPlantService plantService, INotificationService notificationService)
        {
            _wateringService = wateringService;
            _plantService = plantService;
            _notificationService = notificationService;
        }

        /// <summary>
        /// Get the watering history for a specific plant.
        /// </summary>
        /// <param name="plantId">The ID of the plant</param>
        /// <returns>A list of watering events</returns>
        [HttpGet("Plant/{plantId}")]
        [ProducesResponseType(typeof(IEnumerable<WateringEventResponseDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<WateringEventResponseDto>>> GetEventsByPlant(int plantId)
        {
            var events = await _wateringService.GetEventsByPlantIdAsync(plantId);
            var dtos = events.Select(e => MapToResponseDto(e));
            return Ok(dtos);
        }

        /// <summary>
        /// Record a new watering event.
        /// </summary>
        /// <param name="dto">The event data</param>
        /// <returns>The recorded event object</returns>
        [HttpPost]
        [AllowAnonymous]
        [ProducesResponseType(typeof(WateringEventResponseDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<WateringEventResponseDto>> PostEvent(CreateWateringEventDto dto)
        {
            if (!await _plantService.PlantExistsAsync(dto.PlantId))
            {
                return BadRequest($"Plant with ID {dto.PlantId} does not exist.");
            }

            if (dto.ScheduleId.HasValue)
            {
                var schedule = await _wateringService.GetScheduleByIdAsync(dto.ScheduleId.Value);
                if (schedule == null)
                {
                    return BadRequest($"Watering Schedule with ID {dto.ScheduleId.Value} does not exist.");
                }
            }

            var wateringEvent = new WateringEvent
            {
                PlantId = dto.PlantId,
                ScheduleId = dto.ScheduleId,
                AmountMl = dto.AmountMl,
                Mode = dto.Mode,
                DurationSeconds = dto.DurationSeconds,
                Status = dto.Status,
                Notes = dto.Notes,
                Timestamp = DateTime.UtcNow
            };

            await _wateringService.LogWateringEventAsync(wateringEvent);

            if (dto.Status == WateringStatus.Completed || dto.Status == WateringStatus.Failed)
            {
                var plant = await _plantService.GetByIdAsync(dto.PlantId);
                if (plant != null)
                {
                    if (dto.Status == WateringStatus.Completed)
                    {
                        await _notificationService.CreateNotificationAsync(new Notification
                        {
                            UserId = plant.UserId,
                            PlantId = plant.PlantId,
                            Type = NotificationType.WateringComplete,
                            Priority = NotificationPriority.Low,
                            Message = $"Watering completed for plant '{plant.Name}'. Amount: {dto.AmountMl}ml.",
                            Timestamp = DateTime.UtcNow,
                            Read = false
                        });
                    }
                    else if (dto.Status == WateringStatus.Failed)
                    {
                        await _notificationService.CreateNotificationAsync(new Notification
                        {
                            UserId = plant.UserId,
                            PlantId = plant.PlantId,
                            Type = NotificationType.WateringFailed,
                            Priority = NotificationPriority.High,
                            Message = $"Watering failed for plant '{plant.Name}'. Check system/device.",
                            Timestamp = DateTime.UtcNow,
                            Read = false
                        });
                    }
                }
            }

            return CreatedAtAction(nameof(GetEventsByPlant), new { plantId = wateringEvent.PlantId }, MapToResponseDto(wateringEvent));
        }

        private static WateringEventResponseDto MapToResponseDto(WateringEvent e)
        {
            return new WateringEventResponseDto
            {
                EventId = e.EventId,
                PlantId = e.PlantId,
                ScheduleId = e.ScheduleId,
                Timestamp = e.Timestamp,
                AmountMl = e.AmountMl,
                Mode = e.Mode,
                DurationSeconds = e.DurationSeconds,
                Status = e.Status,
                Notes = e.Notes
            };
        }
    }
}