namespace API.DTOs
{
    public class PlantSpeciesDto
    {
        public int SpeciesId { get; set; }
        public string Name { get; set; } = string.Empty;
        public decimal? DefaultMoistureMin { get; set; }
        public decimal? DefaultMoistureMax { get; set; }
        public decimal? DefaultLightMin { get; set; }
        public decimal? DefaultLightMax { get; set; }
        public int? CreatedByUserId { get; set; }
    }

    public class CreatePlantSpeciesDto
    {
        public string Name { get; set; } = string.Empty;
        public decimal? DefaultMoistureMin { get; set; }
        public decimal? DefaultMoistureMax { get; set; }
        public decimal? DefaultLightMin { get; set; }
        public decimal? DefaultLightMax { get; set; }
    }
}
