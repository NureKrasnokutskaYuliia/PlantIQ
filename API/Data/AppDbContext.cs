using API.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;

namespace API.Data
{
    public class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
    {
        public DbSet<User> Users { get; set; } = null!;
        public DbSet<Device> Devices { get; set; } = null!;
        public DbSet<Plant> Plants { get; set; } = null!;
        public DbSet<SensorData> SensorData { get; set; } = null!;
        public DbSet<WateringSchedule> WateringSchedules { get; set; } = null!;
        public DbSet<WateringEvent> WateringEvents { get; set; } = null!;
        public DbSet<Notification> Notifications { get; set; } = null!;

        public DbSet<PlantSpecies> PlantSpecies { get; set; } = null!;
        public DbSet<SystemSetting> SystemSettings { get; set; } = null!;

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            var notificationTypeConverter = new ValueConverter<NotificationType, string>(
                v => NotificationTypeToString(v),
                v => StringToNotificationType(v));

            modelBuilder.Entity<User>()
               .Property(u => u.Role)
               .HasConversion(
                    v => v.ToString().ToLower(),
                    v => (UserRole)Enum.Parse(typeof(UserRole), v, true));

            modelBuilder.Entity<User>().Property(u => u.CreatedAt).HasDefaultValueSql("CURRENT_TIMESTAMP");
            modelBuilder.Entity<User>().Property(u => u.UpdatedAt).HasDefaultValueSql("CURRENT_TIMESTAMP");
            modelBuilder.Entity<User>().Property(u => u.IsActive).HasDefaultValue(true);
            modelBuilder.Entity<User>().HasIndex(u => u.Email).IsUnique();

            modelBuilder.Entity<Device>()
                .Property(d => d.Status)
                .HasDefaultValue(DeviceStatus.Offline)
                .HasConversion(
                    v => v.ToString().ToLower(),
                    v => (DeviceStatus)Enum.Parse(typeof(DeviceStatus), v, true));

            modelBuilder.Entity<Device>().Property(d => d.CreatedAt).HasDefaultValueSql("CURRENT_TIMESTAMP");
            modelBuilder.Entity<Device>().Property(d => d.UpdatedAt).HasDefaultValueSql("CURRENT_TIMESTAMP");
            modelBuilder.Entity<Device>().HasIndex(d => d.SerialNumber).IsUnique();

            modelBuilder.Entity<Plant>()
                .Property(p => p.WateringMode)
                .HasDefaultValue(WateringMode.Manual)
                .HasConversion(
                    v => v.ToString(),
                    v => (WateringMode)Enum.Parse(typeof(WateringMode), v, true));

            modelBuilder.Entity<WateringEvent>()
                .Property(e => e.Mode)
                .HasDefaultValue(WateringMode.Automatic)
                .HasConversion(
                    v => v.ToString(),
                    v => (WateringMode)Enum.Parse(typeof(WateringMode), v, true));

            modelBuilder.Entity<WateringEvent>()
                .Property(e => e.Status)
                .HasDefaultValue(WateringStatus.Completed)
                .HasConversion(
                    v => v.ToString(),
                    v => (WateringStatus)Enum.Parse(typeof(WateringStatus), v, true));

            modelBuilder.Entity<WateringEvent>().Property(we => we.Timestamp).HasDefaultValueSql("CURRENT_TIMESTAMP");

            modelBuilder.Entity<Notification>()
                .Property(n => n.Type)
                .HasConversion(notificationTypeConverter);
            
            modelBuilder.Entity<Notification>()
                .Property(n => n.Priority)
                .HasDefaultValue(NotificationPriority.Normal)
                .HasConversion(
                    v => v.ToString().ToLower(),
                    v => (NotificationPriority)Enum.Parse(typeof(NotificationPriority), v, true));

            modelBuilder.Entity<Notification>().Property(n => n.Timestamp).HasDefaultValueSql("CURRENT_TIMESTAMP");
            modelBuilder.Entity<Notification>().Property(n => n.Read).HasDefaultValue(false);
            modelBuilder.Entity<Plant>().Property(p => p.CreatedAt).HasDefaultValueSql("CURRENT_TIMESTAMP");
            modelBuilder.Entity<Plant>().Property(p => p.UpdatedAt).HasDefaultValueSql("CURRENT_TIMESTAMP");
            modelBuilder.Entity<Plant>().Property(p => p.IsActive).HasDefaultValue(true);
            modelBuilder.Entity<SensorData>().Property(sd => sd.Timestamp).HasDefaultValueSql("CURRENT_TIMESTAMP");

            modelBuilder.Entity<WateringSchedule>().Property(ws => ws.Enabled).HasDefaultValue(true);
            modelBuilder.Entity<WateringSchedule>().Property(ws => ws.DaysOfWeek).HasDefaultValue(new[] { 0, 1, 2, 3, 4, 5, 6 });
            modelBuilder.Entity<WateringSchedule>().Property(ws => ws.CreatedAt).HasDefaultValueSql("CURRENT_TIMESTAMP");

            modelBuilder.Entity<SystemSetting>().HasIndex(s => s.SettingKey).IsUnique();
            modelBuilder.Entity<SystemSetting>().Property(s => s.UpdatedAt).HasDefaultValueSql("CURRENT_TIMESTAMP");
            modelBuilder.Entity<PlantSpecies>().Property(ps => ps.CreatedAt).HasDefaultValueSql("CURRENT_TIMESTAMP");

            modelBuilder.Entity<Device>().HasOne(d => d.User).WithMany(u => u.Devices).HasForeignKey(d => d.UserId).OnDelete(DeleteBehavior.Cascade);
            modelBuilder.Entity<Plant>().HasOne(p => p.User).WithMany(u => u.Plants).HasForeignKey(p => p.UserId).OnDelete(DeleteBehavior.Cascade);
            modelBuilder.Entity<Plant>().HasOne(p => p.Device).WithMany(d => d.Plants).HasForeignKey(p => p.DeviceId).OnDelete(DeleteBehavior.SetNull);
            modelBuilder.Entity<SensorData>().HasOne(sd => sd.Plant).WithMany().HasForeignKey(sd => sd.PlantId).OnDelete(DeleteBehavior.Cascade);
            modelBuilder.Entity<SensorData>().HasOne(sd => sd.Device).WithMany(d => d.SensorReadings).HasForeignKey(sd => sd.DeviceId).OnDelete(DeleteBehavior.Cascade);
            modelBuilder.Entity<WateringSchedule>().HasOne(ws => ws.Plant).WithMany(p => p.WateringSchedules).HasForeignKey(ws => ws.PlantId).OnDelete(DeleteBehavior.Cascade);
            modelBuilder.Entity<WateringEvent>().HasOne(we => we.Plant).WithMany(p => p.WateringEvents).HasForeignKey(we => we.PlantId).OnDelete(DeleteBehavior.Cascade);
            modelBuilder.Entity<WateringEvent>().HasOne(we => we.Schedule).WithMany().HasForeignKey(we => we.ScheduleId).OnDelete(DeleteBehavior.SetNull);
            modelBuilder.Entity<Notification>().HasOne(n => n.User).WithMany(u => u.Notifications).HasForeignKey(n => n.UserId).OnDelete(DeleteBehavior.Cascade);
            modelBuilder.Entity<Notification>().HasOne(n => n.Plant).WithMany().HasForeignKey(n => n.PlantId).OnDelete(DeleteBehavior.Cascade);
            modelBuilder.Entity<Notification>().HasOne(n => n.Device).WithMany().HasForeignKey(n => n.DeviceId).OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<SystemSetting>().HasOne(ss => ss.User).WithMany().HasForeignKey(ss => ss.UpdatedBy).OnDelete(DeleteBehavior.SetNull);

            // Seeding Plant Species with static date
            var seedDate = new DateTime(2024, 1, 1, 0, 0, 0, DateTimeKind.Utc);
            modelBuilder.Entity<PlantSpecies>().HasData(
                new PlantSpecies { SpeciesId = 1, Name = "Алое Вера", DefaultMoistureMin = 15, DefaultMoistureMax = 40, DefaultLightMin = 60, DefaultLightMax = 100, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 2, Name = "Сансев'єрія (Тещин язик)", DefaultMoistureMin = 10, DefaultMoistureMax = 30, DefaultLightMin = 20, DefaultLightMax = 80, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 3, Name = "Фікус Бенджаміна", DefaultMoistureMin = 40, DefaultMoistureMax = 70, DefaultLightMin = 50, DefaultLightMax = 90, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 4, Name = "Монстера Деліціоза", DefaultMoistureMin = 50, DefaultMoistureMax = 80, DefaultLightMin = 40, DefaultLightMax = 70, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 5, Name = "Спатифілум (Жіноче щастя)", DefaultMoistureMin = 60, DefaultMoistureMax = 90, DefaultLightMin = 30, DefaultLightMax = 60, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 6, Name = "Заміокулькас", DefaultMoistureMin = 15, DefaultMoistureMax = 35, DefaultLightMin = 20, DefaultLightMax = 70, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 7, Name = "Орхідея Фаленопсис", DefaultMoistureMin = 30, DefaultMoistureMax = 60, DefaultLightMin = 40, DefaultLightMax = 75, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 8, Name = "Кактус (різні види)", DefaultMoistureMin = 5, DefaultMoistureMax = 20, DefaultLightMin = 70, DefaultLightMax = 100, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 9, Name = "Драцена", DefaultMoistureMin = 30, DefaultMoistureMax = 60, DefaultLightMin = 30, DefaultLightMax = 80, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 10, Name = "Хлорофітум", DefaultMoistureMin = 40, DefaultMoistureMax = 80, DefaultLightMin = 30, DefaultLightMax = 90, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 11, Name = "Сукуленти (Ехерверія)", DefaultMoistureMin = 10, DefaultMoistureMax = 30, DefaultLightMin = 60, DefaultLightMax = 100, CreatedAt = seedDate },
                new PlantSpecies { SpeciesId = 12, Name = "Бегонія", DefaultMoistureMin = 50, DefaultMoistureMax = 85, DefaultLightMin = 40, DefaultLightMax = 70, CreatedAt = seedDate }
            );
        }
        private static string NotificationTypeToString(NotificationType type)
        {
            return type switch
            {
                NotificationType.LowMoisture => "low_moisture",
                NotificationType.HighMoisture => "high_moisture",
                NotificationType.LowLight => "low_light",
                NotificationType.HighLight => "high_light",
                NotificationType.DeviceOffline => "device_offline",
                NotificationType.WateringComplete => "watering_complete",
                NotificationType.WateringFailed => "watering_failed",
                NotificationType.BatteryLow => "battery_low",
                _ => "system"
            };
        }

        private static NotificationType StringToNotificationType(string type)
        {
            return type switch
            {
                "low_moisture" => NotificationType.LowMoisture,
                "high_moisture" => NotificationType.HighMoisture,
                "low_light" => NotificationType.LowLight,
                "high_light" => NotificationType.HighLight,
                "device_offline" => NotificationType.DeviceOffline,
                "watering_complete" => NotificationType.WateringComplete,
                "watering_failed" => NotificationType.WateringFailed,
                "battery_low" => NotificationType.BatteryLow,
                _ => NotificationType.System
            };
        }
    }
}