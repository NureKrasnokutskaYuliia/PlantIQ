using System.ComponentModel.DataAnnotations;

namespace API.DTOs
{

    public class SystemStatisticsDto
    {
        public int TotalUsers { get; set; }
        public int TotalDevices { get; set; }
        public int TotalPlants { get; set; }
        public int CriticalAlertsLast24h { get; set; }
        public double DatabaseSizeMb { get; set; }
    }
}