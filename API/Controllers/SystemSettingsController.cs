using API.Models;
using API.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize(Roles = "admin")]
    [Produces("application/json")]
    public class SystemSettingsController : ControllerBase
    {
        private readonly ISystemSettingsService _settingsService;

        public SystemSettingsController(ISystemSettingsService settingsService)
        {
            _settingsService = settingsService;
        }

        /// <summary>
        /// Get all system settings.
        /// </summary>
        /// <remarks>
        /// Returns a list of global configuration key-value pairs.
        /// </remarks>
        /// <returns>A list of system settings</returns>
        /// <response code="200">Successfully returned the settings</response>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<SystemSetting>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<SystemSetting>>> GetSettings()
        {
            var settings = await _settingsService.GetAllSettingsAsync();
            return Ok(settings);
        }

        /// <summary>
        /// Get a specific system setting value by its key.
        /// </summary>
        /// <param name="key">The unique key of the setting (e.g., "MaxUploadSize")</param>
        /// <returns>The string value of the setting</returns>
        /// <response code="200">Setting found, returns the value</response>
        /// <response code="404">Setting with this key does not exist</response>
        [HttpGet("{key}")]
        [ProducesResponseType(typeof(string), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<string>> GetSettingByKey(string key)
        {
            var value = await _settingsService.GetValueAsync(key);

            if (value == null)
            {
                return NotFound();
            }

            return Ok(value);
        }

        /// <summary>
        /// Update or create a system setting.
        /// </summary>
        /// <remarks>
        /// Updates the value of a specific setting identified by its key. The service automatically updates the 'UpdatedAt' timestamp.
        /// </remarks>
        /// <param name="key">The unique key of the setting to update</param>
        /// <param name="settingUpdate">The object containing the new value and updater information</param>
        /// <response code="204">Successful update (no content)</response>
        /// <response code="400">Invalid input</response>
        [HttpPut("{key}")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<IActionResult> UpdateSetting(string key, [FromBody] SystemSetting settingUpdate)
        {
            // Ми використовуємо SystemSetting як DTO для отримання значення
            // Сервіс сам обробить логіку: якщо ключа немає - створить, якщо є - оновить.

            await _settingsService.SetValueAsync(key, settingUpdate.SettingValue, settingUpdate.UpdatedBy);

            return NoContent();
        }
    }
}