using API.Services.Interfaces;
using FirebaseAdmin.Messaging;

namespace API.Services
{
    public class FirebaseService : IFirebaseService
    {
        private readonly ILogger<FirebaseService> _logger;

        public FirebaseService(ILogger<FirebaseService> logger)
        {
            _logger = logger;
        }

        public async Task SendNotificationAsync(string token, string title, string body, Dictionary<string, string>? data = null)
        {
            try
            {
                var messaging = FirebaseMessaging.DefaultInstance;
                if (messaging == null)
                {
                    _logger.LogError("Firebase Error: FirebaseMessaging.DefaultInstance is null. FirebaseApp was not initialized correctly at startup. Please check your FIREBASE_CONFIG or JSON key file.");
                    return;
                }

                var message = new Message()
                {
                    Token = token,
                    Notification = new Notification()
                    {
                        Title = title,
                        Body = body
                    },
                    Data = data
                };

                string response = await messaging.SendAsync(message);
                _logger.LogInformation("Successfully sent message to Firebase. Response: {Response}", response);
            }
            catch (FirebaseAdmin.FirebaseException ex)
            {
                _logger.LogError("Firebase API error: {ErrorCode}, Message: {Message}", ex.ErrorCode, ex.Message);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "General error sending Firebase notification to token {Token}. Message: {Msg}", token, ex.Message);
            }
        }
    }
}
