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
    public class UsersController : ControllerBase
    {
        private readonly IUserService _userService;

        public UsersController(IUserService userService)
        {
            _userService = userService;
        }

        /// <summary>
        /// Get a list of all users.
        /// </summary>
        /// <returns>A list of users</returns>
        /// <response code="200">Successfully returned the list</response>
        [HttpGet]
        [Authorize(Roles = "admin")]
        [ProducesResponseType(typeof(IEnumerable<UserResponseDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<UserResponseDto>>> GetUsers()
        {
            var users = await _userService.GetAllAsync();
            var userDtos = users.Select(u => MapToResponseDto(u));
            return Ok(userDtos);
        }

        /// <summary>
        /// Get a specific user by ID.
        /// </summary>
        /// <param name="id">Unique user identifier</param>
        /// <returns>The user object</returns>
        /// <response code="200">User found</response>
        /// <response code="404">User with this ID does not exist</response>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(UserResponseDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<UserResponseDto>> GetUser(int id)
        {
            var user = await _userService.GetByIdAsync(id);
            if (user == null) return NotFound();
            return Ok(MapToResponseDto(user));
        }

        /// <summary>
        /// Create (Register) a new user.
        /// </summary>
        /// <param name="dto">The new user data</param>
        /// <returns>The created user object</returns>
        /// <response code="201">User successfully created</response>
        /// <response code="400">Invalid input data</response>
        [HttpPost]
        [AllowAnonymous]
        [ProducesResponseType(typeof(UserResponseDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status409Conflict)]
        public async Task<ActionResult<UserResponseDto>> PostUser(CreateUserDto dto)
        {
            var user = new User
            {
                Name = dto.Name,
                Email = dto.Email,
                Role = dto.Role
            };

            try
            {
                var createdUser = await _userService.RegisterUserAsync(user, dto.Password);
                return CreatedAtAction("GetUser", new { id = createdUser.UserId }, MapToResponseDto(createdUser));
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Update an existing user.
        /// </summary>
        /// <param name="id">The ID of the user to update</param>
        /// <param name="dto">The updated user data</param>
        /// <response code="204">Successful update (no content)</response>
        /// <response code="400">The ID in the URL does not match the ID in the body</response>
        /// <response code="404">User not found</response>
        [HttpPut("{id}")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> PutUser(int id, UpdateUserDto dto)
        {
            // Note: In a real app, we should probably check if the current user has permission to update this user

            var existingUser = await _userService.GetByIdAsync(id);
            if (existingUser == null) return NotFound();

            existingUser.Name = dto.Name;
            existingUser.Email = dto.Email;
            existingUser.Role = dto.Role;
            existingUser.IsActive = dto.IsActive;
            existingUser.UpdatedAt = DateTime.UtcNow;

            try
            {
                await _userService.UpdateUserAsync(existingUser);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }

            return NoContent();
        }

        /// <summary>
        /// Delete a user.
        /// </summary>
        /// <param name="id">The ID of the user to delete</param>
        /// <response code="204">Successful deletion</response>
        /// <response code="404">User not found</response>
        [HttpDelete("{id}")]
        [ProducesResponseType(StatusCodes.Status204NoContent)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> DeleteUser(int id)
        {
            if (!await _userService.UserExistsAsync(id)) return NotFound();
            await _userService.DeleteUserAsync(id);
            return NoContent();
        }

        /// <summary>
        /// Authenticate a user (Login) and return JWT Token.
        /// </summary>
        /// <param name="request">Login credentials (email, password)</param>
        /// <returns>JSON containing the Token and User details</returns>
        /// <response code="200">Login successful, token returned</response>
        /// <response code="401">Invalid email or password</response>
        [HttpPost("login")]
        [AllowAnonymous]
        [ProducesResponseType(typeof(LoginResponseDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status401Unauthorized)]
        public async Task<ActionResult<LoginResponseDto>> Login([FromBody] LoginRequestDto request)
        {
            var user = await _userService.AuthenticateAsync(request.Email, request.Password);
            if (user == null) return Unauthorized("Invalid email or password.");

            var tokenString = _userService.GenerateJwtToken(user);

            return Ok(new LoginResponseDto
            {
                Token = tokenString,
                User = MapToResponseDto(user)
            });
        }

        private static UserResponseDto MapToResponseDto(User user)
        {
            return new UserResponseDto
            {
                UserId = user.UserId,
                Name = user.Name,
                Email = user.Email,
                Role = user.Role,
                CreatedAt = user.CreatedAt,
                UpdatedAt = user.UpdatedAt,
                IsActive = user.IsActive,
                LastLogin = user.LastLogin
            };
        }
    }
}