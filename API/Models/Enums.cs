namespace API.Models
{
    public enum UserRole
    {
        Owner,
        Admin
    }

    public enum DeviceStatus
    {
        Online,
        Offline,
        Error,
        Maintenance
    }

    public enum WateringMode
    {
        Automatic,
        Manual,
        Scheduled
    }

    public enum WateringStatus
    {
        Completed,
        Failed,
        Cancelled
    }

    public enum NotificationType
    {
        LowMoisture,
        HighMoisture,
        LowLight,
        HighLight,
        DeviceOffline,
        WateringComplete,
        WateringFailed,
        BatteryLow,
        System
    }

    public enum NotificationPriority
    {
        Low,
        Normal,
        High,
        Critical
    }


}