namespace ShoeStore.Application.Interface.Notification;

public interface IEmailService
{
    Task SendEmailAsync(string from, string to,string subject, string body, CancellationToken token);
}