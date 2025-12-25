using API.DTOs;
using API.Models;
using API.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    [Produces("application/json")]
    public class PlantsController : ControllerBase
    {
        private readonly IPlantService _plantService;
        private readonly IDeviceService _deviceService;

        public PlantsController(IPlantService plantService, IDeviceService deviceService)
        {
            _plantService = plantService;
            _deviceService = deviceService;
        }

        /// <summary>
        /// Get a list of all plants (Admin only).
        /// </summary>
        /// <remarks>
        /// Returns all plants available in the database. Restricted to administrators.
        /// </remarks>
        /// <returns>A list of plants</returns>
        [HttpGet]
        [Authorize(Roles = "admin")]
        [ProducesResponseType(typeof(IEnumerable<PlantResponseDto>), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status403Forbidden)]
        public async Task<ActionResult<IEnumerable<PlantResponseDto>>> GetPlants()
        {
            var plants = await _plantService.GetAllAsync();
            var plantDtos = plants.Select(p => MapToResponseDto(p));
            return Ok(plantDtos);
        }

        /// <summary>
        /// Get a specific plant by ID.
        /// </summary>
        /// <param name="id">Unique plant identifier</param>
        /// <returns>The plant object</returns>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(PlantResponseDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<PlantResponseDto>> GetPlant(int id)
        {
            var plant = await _plantService.GetByIdAsync(id);

            if (plant == null)
            {
                return NotFound();
            }

            return Ok(MapToResponseDto(plant));
        }

        /// <summary>
        /// Get all plants belonging to the current user (from JWT).
        /// </summary>
        /// <returns>A list of plants for the authenticated user</returns>
        [HttpGet("User")]
        [ProducesResponseType(typeof(IEnumerable<PlantResponseDto>), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status401Unauthorized)]
        public async Task<ActionResult<IEnumerable<PlantResponseDto>>> GetPlantsByUser()
        {
            var userId = GetUserIdFromClaims();
            if (userId == null)
            {
                return Unauthorized();
            }

            var plants = await _plantService.GetByUserIdAsync(userId.Value);
            var plantDtos = plants.Select(p => MapToResponseDto(p));
            return Ok(plantDtos);
        }

        /// <summary>
        /// Get the plant associated with a specific device.
        /// </summary>
        /// <param name="deviceId">The ID of the device</param>
        /// <returns>The plant linked to the device</returns>
        [HttpGet("Device/{deviceId}")]
        [ProducesResponseType(typeof(PlantResponseDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<PlantResponseDto>> GetPlantsByDevice(int deviceId)
        {
            var plant = await _plantService.GetByDeviceIdAsync(deviceId);

            if (plant == null)
            {
                return NotFound();
            }

            var dto = MapToResponseDto(plant);
            return Ok(dto);
        }

        /// <summary>
        /// Create a new plant.
        /// </summary>
        /// <param name="dto">The new plant data</param>
        /// <returns>The created plant object</returns>
        [HttpPost]
        [ProducesResponseType(typeof(PlantResponseDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status401Unauthorized)]
        public async Task<ActionResult<PlantResponseDto>> PostPlant(CreatePlantDto dto)
        {
            var userId = GetUserIdFromClaims();
            if (userId == null)
            {
                return Unauthorized();
            }

            if (dto.DeviceId.HasValue)
            {
                if (!await _deviceService.DeviceExistsAsync(dto.DeviceId.Value))
                {
                    return BadRequest($"Device with ID {dto.DeviceId.Value} does not exist.");
                }

                var assignedPlant = await _plantService.GetByDeviceIdAsync(dto.DeviceId.Value);
                if (assignedPlant != null)
                {
                    return BadRequest($"Device with ID {dto.DeviceId.Value} is already assigned to another plant.");
                }
            }

            var plant = new Plant
            {
                UserId = userId.Value,
                DeviceId = dto.DeviceId,
                Name = dto.Name,
                Species = dto.Species,
                OptimalMoistureMin = dto.OptimalMoistureMin,
                OptimalMoistureMax = dto.OptimalMoistureMax,
                OptimalLightMin = dto.OptimalLightMin,
                OptimalLightMax = dto.OptimalLightMax,
                Notes = dto.Notes
            };

            var createdPlant = await _plantService.AddPlantAsync(plant);

            return CreatedAtAction("GetPlant", new { id = createdPlant.PlantId }, MapToResponseDto(createdPlant));
        }

        /// <summary>
        /// Update an existing plant.
        /// </summary>
        /// <param name="id">The ID of the plant to update</param>
        /// <param name="dto">The updated plant data</param>
        [HttpPut("{id}")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> PutPlant(int id, UpdatePlantDto dto)
        {
            var existingPlant = await _plantService.GetByIdAsync(id);
            if (existingPlant == null)
            {
                return NotFound();
            }

            if (dto.DeviceId.HasValue)
            {
                if (!await _deviceService.DeviceExistsAsync(dto.DeviceId.Value))
                {
                    return BadRequest($"Device with ID {dto.DeviceId.Value} does not exist.");
                }

                var assignedPlant = await _plantService.GetByDeviceIdAsync(dto.DeviceId.Value);
                if (assignedPlant != null && assignedPlant.PlantId != id)
                {
                    return BadRequest($"Device with ID {dto.DeviceId.Value} is already assigned to another plant.");
                }
            }

            existingPlant.Name = dto.Name;
            existingPlant.DeviceId = dto.DeviceId;
            existingPlant.Species = dto.Species;
            existingPlant.OptimalMoistureMin = dto.OptimalMoistureMin;
            existingPlant.OptimalMoistureMax = dto.OptimalMoistureMax;
            existingPlant.OptimalLightMin = dto.OptimalLightMin;
            existingPlant.OptimalLightMax = dto.OptimalLightMax;
            existingPlant.Notes = dto.Notes;
            existingPlant.IsActive = dto.IsActive;
            existingPlant.UpdatedAt = DateTime.UtcNow;

            await _plantService.UpdatePlantAsync(existingPlant);

            return NoContent();
        }

        /// <summary>
        /// Delete a plant.
        /// </summary>
        /// <param name="id">The ID of the plant to delete</param>
        [HttpDelete("{id}")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> DeletePlant(int id)
        {
            if (!await _plantService.PlantExistsAsync(id))
            {
                return NotFound();
            }

            await _plantService.DeletePlantAsync(id);

            return NoContent();
        }

        // Helper methods
        private int? GetUserIdFromClaims()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value
                           ?? User.FindFirst("sub")?.Value
                           ?? User.FindFirst("userId")?.Value;

            if (int.TryParse(userIdClaim, out int userId))
            {
                return userId;
            }

            return null;
        }

        private static PlantResponseDto MapToResponseDto(Plant plant)
        {
            return new PlantResponseDto
            {
                PlantId = plant.PlantId,
                UserId = plant.UserId,
                DeviceId = plant.DeviceId,
                Name = plant.Name,
                Species = plant.Species,
                OptimalMoistureMin = plant.OptimalMoistureMin,
                OptimalMoistureMax = plant.OptimalMoistureMax,
                OptimalLightMin = plant.OptimalLightMin,
                OptimalLightMax = plant.OptimalLightMax,
                Notes = plant.Notes,
                CreatedAt = plant.CreatedAt,
                UpdatedAt = plant.UpdatedAt,
                IsActive = plant.IsActive
            };
        }
    }
}
