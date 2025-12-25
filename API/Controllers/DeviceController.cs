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
    public class DevicesController : ControllerBase
    {
        private readonly IDeviceService _deviceService;

        public DevicesController(IDeviceService deviceService)
        {
            _deviceService = deviceService;
        }

        /// <summary>
        /// Get a list of all devices.
        /// </summary>
        /// <returns>A list of devices</returns>
        /// <response code="200">Successfully returned the list</response>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<DeviceResponseDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<DeviceResponseDto>>> GetDevices()
        {
            var devices = await _deviceService.GetAllAsync();
            var deviceDtos = devices.Select(d => MapToResponseDto(d));
            return Ok(deviceDtos);
        }

        /// <summary>
        /// Get a specific device by ID.
        /// </summary>
        /// <param name="id">Unique device identifier</param>
        /// <returns>The device object</returns>
        /// <response code="200">Device found</response>
        /// <response code="404">Device with this ID does not exist</response>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(DeviceResponseDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<DeviceResponseDto>> GetDevice(int id)
        {
            var device = await _deviceService.GetByIdAsync(id);

            if (device == null)
            {
                return NotFound();
            }

            return Ok(MapToResponseDto(device));
        }

        /// <summary>
        /// Create a new device.
        /// </summary>
        /// <remarks>
        /// Adds a new device to the database. UserId is extracted from JWT token.
        /// </remarks>
        /// <param name="dto">The new device data</param>
        /// <returns>The created device object</returns>
        /// <response code="201">Device successfully created</response>
        /// <response code="400">Invalid input data</response>
        /// <response code="401">User not authenticated</response>
        [HttpPost]
        [ProducesResponseType(typeof(DeviceResponseDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status401Unauthorized)]
        public async Task<ActionResult<DeviceResponseDto>> PostDevice(CreateDeviceDto dto)
        {
            var userId = GetUserIdFromClaims();
            if (userId == null)
            {
                return Unauthorized();
            }

            var device = new Device
            {
                UserId = userId.Value,
                Name = dto.Name,
                Model = dto.Model,
                SerialNumber = dto.SerialNumber
            };

            var createdDevice = await _deviceService.CreateDeviceAsync(device);

            return CreatedAtAction("GetDevice", new { id = createdDevice.DeviceId }, MapToResponseDto(createdDevice));
        }

        /// <summary>
        /// Update an existing device.
        /// </summary>
        /// <param name="id">The ID of the device to update</param>
        /// <param name="dto">The updated device data</param>
        /// <response code="204">Successful update (no content)</response>
        /// <response code="404">Device not found</response>
        [HttpPut("{id}")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> PutDevice(int id, UpdateDeviceDto dto)
        {
            var existingDevice = await _deviceService.GetByIdAsync(id);
            if (existingDevice == null)
            {
                return NotFound();
            }

            existingDevice.Name = dto.Name;
            existingDevice.Model = dto.Model;
            existingDevice.Status = dto.Status;
            existingDevice.FirmwareVersion = dto.FirmwareVersion;
            existingDevice.UpdatedAt = DateTime.UtcNow;

            await _deviceService.UpdateDeviceAsync(existingDevice);

            return NoContent();
        }

        /// <summary>
        /// Delete a device.
        /// </summary>
        /// <param name="id">The ID of the device to delete</param>
        /// <response code="204">Successful deletion</response>
        /// <response code="404">Device not found</response>
        [HttpDelete("{id}")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> DeleteDevice(int id)
        {
            if (!await _deviceService.DeviceExistsAsync(id))
            {
                return NotFound();
            }

            await _deviceService.DeleteDeviceAsync(id);

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

        private static DeviceResponseDto MapToResponseDto(Device device)
        {
            return new DeviceResponseDto
            {
                DeviceId = device.DeviceId,
                UserId = device.UserId,
                Name = device.Name,
                Model = device.Model,
                Status = device.Status,
                FirmwareVersion = device.FirmwareVersion,
                LastSync = device.LastSync,
                SerialNumber = device.SerialNumber,
                CreatedAt = device.CreatedAt,
                UpdatedAt = device.UpdatedAt
            };
        }
    }
}