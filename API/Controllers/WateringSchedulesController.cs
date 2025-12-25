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
    public class WateringSchedulesController : ControllerBase
    {
        private readonly IWateringService _wateringService;
        private readonly IPlantService _plantService;

        public WateringSchedulesController(IWateringService wateringService, IPlantService plantService)
        {
            _wateringService = wateringService;
            _plantService = plantService;
        }

        /// <summary>
        /// Get all watering schedules for a specific plant.
        /// </summary>
        /// <param name="plantId">The ID of the plant</param>
        /// <returns>A list of watering schedules</returns>
        [AllowAnonymous]
        [HttpGet("Plant/{plantId}")]
        [ProducesResponseType(typeof(IEnumerable<WateringScheduleResponseDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<WateringScheduleResponseDto>>> GetSchedulesByPlant(int plantId)
        {
            var schedules = await _wateringService.GetSchedulesByPlantIdAsync(plantId);
            var dtos = schedules.Select(s => MapToResponseDto(s));
            return Ok(dtos);
        }

        /// <summary>
        /// Get a specific watering schedule by ID.
        /// </summary>
        /// <param name="id">Unique schedule identifier</param>
        /// <returns>The watering schedule object</returns>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(WateringScheduleResponseDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<WateringScheduleResponseDto>> GetSchedule(int id)
        {
            var schedule = await _wateringService.GetScheduleByIdAsync(id);
            if (schedule == null)
            {
                return NotFound();
            }
            return Ok(MapToResponseDto(schedule));
        }

        /// <summary>
        /// Create a new watering schedule.
        /// </summary>
        /// <param name="dto">The schedule data</param>
        /// <returns>The created schedule object</returns>
        [HttpPost]
        [ProducesResponseType(typeof(WateringScheduleResponseDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<WateringScheduleResponseDto>> PostSchedule(CreateWateringScheduleDto dto)
        {
            if (!await _plantService.PlantExistsAsync(dto.PlantId))
            {
                return BadRequest($"Plant with ID {dto.PlantId} does not exist.");
            }

            var schedule = new WateringSchedule
            {
                PlantId = dto.PlantId,
                StartTime = dto.StartTime,
                IntervalHours = dto.IntervalHours,
                AmountMl = dto.AmountMl,
                Enabled = dto.Enabled,
                DaysOfWeek = dto.DaysOfWeek
            };

            var createdSchedule = await _wateringService.CreateScheduleAsync(schedule);

            return CreatedAtAction(nameof(GetSchedule), new { id = createdSchedule.ScheduleId }, MapToResponseDto(createdSchedule));
        }

        /// <summary>
        /// Update an existing watering schedule.
        /// </summary>
        /// <param name="id">The ID of the schedule to update</param>
        /// <param name="dto">The updated schedule data</param>
        [HttpPut("{id}")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> PutSchedule(int id, UpdateWateringScheduleDto dto)
        {
            var existingSchedule = await _wateringService.GetScheduleByIdAsync(id);
            if (existingSchedule == null)
            {
                return NotFound();
            }

            existingSchedule.StartTime = dto.StartTime;
            existingSchedule.IntervalHours = dto.IntervalHours;
            existingSchedule.AmountMl = dto.AmountMl;
            existingSchedule.Enabled = dto.Enabled;
            existingSchedule.DaysOfWeek = dto.DaysOfWeek;
            existingSchedule.UpdatedAt = DateTime.UtcNow;

            await _wateringService.UpdateScheduleAsync(existingSchedule);

            return NoContent();
        }

        /// <summary>
        /// Delete a watering schedule.
        /// </summary>
        /// <param name="id">The ID of the schedule to delete</param>
        [HttpDelete("{id}")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> DeleteSchedule(int id)
        {
            var existingSchedule = await _wateringService.GetScheduleByIdAsync(id);
            if (existingSchedule == null)
            {
                return NotFound();
            }

            await _wateringService.DeleteScheduleAsync(id);
            return NoContent();
        }

        private static WateringScheduleResponseDto MapToResponseDto(WateringSchedule s)
        {
            return new WateringScheduleResponseDto
            {
                ScheduleId = s.ScheduleId,
                PlantId = s.PlantId,
                StartTime = s.StartTime,
                IntervalHours = s.IntervalHours,
                AmountMl = s.AmountMl,
                Enabled = s.Enabled,
                DaysOfWeek = s.DaysOfWeek,
                CreatedAt = s.CreatedAt,
                UpdatedAt = s.UpdatedAt
            };
        }
    }
}