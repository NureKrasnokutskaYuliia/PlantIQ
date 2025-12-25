using System.ComponentModel.DataAnnotations;

namespace API.DTOs
{
    public class SensorDataResponseDto
    {
        public int DataId { get; set; }
        public int PlantId { get; set; }
        public int DeviceId { get; set; }
        public DateTime Timestamp { get; set; }
        public decimal? SoilMoisture { get; set; }
        public decimal? LightIntensity { get; set; }
        public decimal? BatteryLevel { get; set; }
    }

    public class CreateSensorDataDto
    {
        [Required]
        public int PlantId { get; set; }

        [Required]
        public int DeviceId { get; set; }

        public decimal? SoilMoisture { get; set; }
        public decimal? LightIntensity { get; set; }
        public decimal? BatteryLevel { get; set; }
    }
}
