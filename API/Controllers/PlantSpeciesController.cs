using API.Data;
using API.DTOs;
using API.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class PlantSpeciesController(AppDbContext context) : ControllerBase
    {
        [HttpGet]
        public async Task<ActionResult<IEnumerable<PlantSpeciesDto>>> GetSpecies()
        {
            return await context.PlantSpecies
                .Select(s => new PlantSpeciesDto
                {
                    SpeciesId = s.SpeciesId,
                    Name = s.Name,
                    DefaultMoistureMin = s.DefaultMoistureMin,
                    DefaultMoistureMax = s.DefaultMoistureMax,
                    DefaultLightMin = s.DefaultLightMin,
                    DefaultLightMax = s.DefaultLightMax,
                    CreatedByUserId = s.CreatedByUserId
                })
                .ToListAsync();
        }

        [HttpPost]
        public async Task<ActionResult<PlantSpeciesDto>> CreateSpecies(CreatePlantSpeciesDto dto)
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            int? userId = int.TryParse(userIdStr, out var id) ? id : null;

            var species = new PlantSpecies
            {
                Name = dto.Name,
                DefaultMoistureMin = dto.DefaultMoistureMin,
                DefaultMoistureMax = dto.DefaultMoistureMax,
                DefaultLightMin = dto.DefaultLightMin,
                DefaultLightMax = dto.DefaultLightMax,
                CreatedByUserId = userId
            };

            context.PlantSpecies.Add(species);
            await context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetSpecies), new { }, new PlantSpeciesDto
            {
                SpeciesId = species.SpeciesId,
                Name = species.Name,
                DefaultMoistureMin = species.DefaultMoistureMin,
                DefaultMoistureMax = species.DefaultMoistureMax,
                DefaultLightMin = species.DefaultLightMin,
                DefaultLightMax = species.DefaultLightMax,
                CreatedByUserId = species.CreatedByUserId
            });
        }
    }
}
