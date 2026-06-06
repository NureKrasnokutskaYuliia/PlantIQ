using API.Services.Interfaces;
using System.Text;
using System.Text.Json;

namespace API.Services
{
    public class EmailService : IEmailService
    {
        private readonly IConfiguration _config;
        private readonly IHttpClientFactory _httpClientFactory;

        public EmailService(IConfiguration config, IHttpClientFactory httpClientFactory)
        {
            _config = config;
            _httpClientFactory = httpClientFactory;
        }

        public async Task SendPasswordResetCodeAsync(string toEmail, string code)
        {
            var apiKey = _config["Resend:ApiKey"]!;
            var senderEmail = _config["Resend:SenderEmail"] ?? "onboarding@resend.dev";
            var senderName = _config["Resend:SenderName"] ?? "PlantIQ";

            var body = $@"
<html>
<body style='font-family: Arial, sans-serif; max-width: 480px; margin: auto;'>
  <h2 style='color: #2E7D32;'>PlantIQ — Password Reset</h2>
  <p>You requested a password reset. Use the 6-digit code below:</p>
  <div style='font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #2E7D32; padding: 16px 0;'>{code}</div>
  <p>This code expires in <strong>15 minutes</strong>.</p>
  <p style='color: #888; font-size: 12px;'>If you did not request this, you can safely ignore this email.</p>
</body>
</html>";

            var payload = new
            {
                from = $"{senderName} <{senderEmail}>",
                to = new[] { toEmail },
                subject = "PlantIQ — Your password reset code",
                html = body
            };

            var client = _httpClientFactory.CreateClient();
            client.DefaultRequestHeaders.Add("Authorization", $"Bearer {apiKey}");

            var json = JsonSerializer.Serialize(payload);
            var content = new StringContent(json, Encoding.UTF8, "application/json");

            var response = await client.PostAsync("https://api.resend.com/emails", content);
            response.EnsureSuccessStatusCode();
        }
    }
}
