using API.Models;
using System.ComponentModel.DataAnnotations;

namespace API.DTOs
{
    public class PlantResponseDto
    {
        public int PlantId { get; set; }
        public int UserId { get; set; }
        public int? DeviceId { get; set; }
        public string Name { get; set; } = string.Empty;
        public string? Species { get; set; }
        public decimal? OptimalMoistureMin { get; set; }
        public decimal? OptimalMoistureMax { get; set; }
        public decimal? OptimalLightMin { get; set; }
        public decimal? OptimalLightMax { get; set; }
        public string? Notes { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
        public bool IsActive { get; set; }
    }

    public class CreatePlantDto
    {
        [Required]
        [MaxLength(100)]
        public string Name { get; set; } = string.Empty;

        public int? DeviceId { get; set; }

        [MaxLength(100)]
        public string? Species { get; set; }

        public decimal? OptimalMoistureMin { get; set; }
        public decimal? OptimalMoistureMax { get; set; }
        public decimal? OptimalLightMin { get; set; }
        public decimal? OptimalLightMax { get; set; }
        public string? Notes { get; set; }
    }

    public class UpdatePlantDto
    {
        [Required]
        [MaxLength(100)]
        public string Name { get; set; } = string.Empty;

        public int? DeviceId { get; set; }

        [MaxLength(100)]
        public string? Species { get; set; }

        public decimal? OptimalMoistureMin { get; set; }
        public decimal? OptimalMoistureMax { get; set; }
        public decimal? OptimalLightMin { get; set; }
        public decimal? OptimalLightMax { get; set; }
        public string? Notes { get; set; }
        public bool IsActive { get; set; }
    }
}
