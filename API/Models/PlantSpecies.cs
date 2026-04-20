using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace API.Models
{
    [Table("plant_species")]
    public class PlantSpecies
    {
        [Key]
        [Column("species_id")]
        public int SpeciesId { get; set; }

        [Required]
        [MaxLength(100)]
        [Column("name")]
        public string Name { get; set; } = string.Empty;

        [Column("default_moisture_min")]
        public decimal? DefaultMoistureMin { get; set; }

        [Column("default_moisture_max")]
        public decimal? DefaultMoistureMax { get; set; }

        [Column("default_light_min")]
        public decimal? DefaultLightMin { get; set; }

        [Column("default_light_max")]
        public decimal? DefaultLightMax { get; set; }

        [Column("created_at")]
        public DateTime CreatedAt { get; set; }

        [Column("created_by_user_id")]
        public int? CreatedByUserId { get; set; }

        [ForeignKey(nameof(CreatedByUserId))]
        public User? Creator { get; set; }
    }
}
