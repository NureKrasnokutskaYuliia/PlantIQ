using API.Services.Interfaces;
using System.Net;
using System.Net.Mail;

namespace API.Services
{
    public class EmailService : IEmailService
    {
        private readonly IConfiguration _config;

        public EmailService(IConfiguration config)
        {
            _config = config;
        }

        public async Task SendPasswordResetCodeAsync(string toEmail, string code)
        {
            var section = _config.GetSection("Email");
            var host = section["SmtpHost"] ?? "smtp.gmail.com";
            var port = int.Parse(section["SmtpPort"] ?? "587");
            var senderEmail = section["SenderEmail"]!;
            var senderName = section["SenderName"] ?? "PlantIQ";
            var password = section["Password"]!;

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

            using var client = new SmtpClient(host, port)
            {
                Credentials = new NetworkCredential(senderEmail, password),
                EnableSsl = true
            };

            var message = new MailMessage
            {
                From = new MailAddress(senderEmail, senderName),
                Subject = "PlantIQ — Your password reset code",
                Body = body,
                IsBodyHtml = true
            };
            message.To.Add(toEmail);

            await client.SendMailAsync(message);
        }
    }
}
