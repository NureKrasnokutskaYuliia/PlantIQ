using API.Models;

namespace API.Services.Interfaces
{
    public interface IUserService
    {
        Task<IEnumerable<User>> GetAllAsync();
        Task<User?> GetByIdAsync(int id);
        Task<User?> GetByEmailAsync(string email);
        Task<User> RegisterUserAsync(User user, string password);
        Task<User?> AuthenticateAsync(string email, string password);
        string GenerateJwtToken(User user);
        Task UpdateUserAsync(User user);
        Task DeleteUserAsync(int id);
        Task<bool> UserExistsAsync(int id);
    }
}