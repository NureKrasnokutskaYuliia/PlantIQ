using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace API.Models
{
    [Table("sensor_data")]
    public class SensorData
    {
        [Key]
        [Column("data_id")]
        public int DataId { get; set; }

        [Column("plant_id")]
        public int PlantId { get; set; }

        [ForeignKey(nameof(PlantId))]
        [JsonIgnore]
        public Plant? Plant { get; set; }

        [Column("device_id")]
        public int DeviceId { get; set; }

        [ForeignKey(nameof(DeviceId))]
        [JsonIgnore]
        public Device? Device { get; set; }

        [Column("timestamp")]
        public DateTime Timestamp { get; set; } = DateTime.UtcNow;

        [Column("soil_moisture")]
        public decimal? SoilMoisture { get; set; }

        [Column("light_intensity")]
        public decimal? LightIntensity { get; set; }

        [Column("battery_level")]
        public decimal? BatteryLevel { get; set; }
    }
}
