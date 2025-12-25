using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace API.Models
{
    [Table("system_settings")]
    public class SystemSetting
    {
        [Key]
        [Column("setting_id")]
        public int SettingId { get; set; }

        [Required]
        [MaxLength(100)]
        [Column("setting_key")]
        public string SettingKey { get; set; } = string.Empty;

        [Column("setting_value")]
        public string? SettingValue { get; set; }

        [Column("description")]
        public string? Description { get; set; }

        [Column("updated_by")]
        public int? UpdatedBy { get; set; }

        [ForeignKey(nameof(UpdatedBy))]
        public User? User { get; set; }

        [Column("updated_at")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }
}
