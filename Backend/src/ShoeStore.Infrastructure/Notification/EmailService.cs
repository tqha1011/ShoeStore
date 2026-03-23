using MailKit.Net.Smtp;
using Microsoft.Extensions.Configuration;
using ShoeStore.Application.Interface;
using MimeKit;
using MimeKit.Text;

namespace ShoeStore.Infrastructure.Notification;

public class EmailService(IConfiguration configuration) : IEmailService
{
    public async Task SendEmailAsync(string from, string to, string subject, string body, CancellationToken token)
    {
        var appPassword = configuration["GOOGLEAUTHENTICATION_APPPASSWORD"]
            ?? throw new InvalidOperationException("GOOGLEAUTHENTICATION_APPPASSWORD is not configured.");
        var message = new MimeMessage();
        message.From.Add(new MailboxAddress("Shoe Store admin",from));
        message.To.Add(new MailboxAddress("You",to));
        message.Subject = subject;
        message.Body = new TextPart(TextFormat.Html) { Text = body };

        using var smtp = new SmtpClient();
        await smtp.ConnectAsync("smtp.gmail.com", 587, MailKit.Security.SecureSocketOptions.StartTls, token);
        await smtp.AuthenticateAsync(from, appPassword, token);
        await ((Task)smtp.SendAsync(message, token));
        await smtp.DisconnectAsync(true, token);
    }
}