using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Notification;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Worker;

public class NotifyNewVoucherService(
    IServiceScopeFactory scopeFactory,
    ILogger<NotifyNewVoucherService> logger,
    INotificationQueue notificationQueue)
    : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        logger.LogInformation("Send Email service starting");
        const string senderEmail = "tranquangha2006@gmail.com";
        while (!stoppingToken.IsCancellationRequested)
        {
            var notification = await notificationQueue.DequeueAsync(stoppingToken);
            using var scope = scopeFactory.CreateScope();
            var userVoucherRepository = scope.ServiceProvider.GetRequiredService<IUserVoucherRepository>();
            var uow = scope.ServiceProvider.GetRequiredService<IUnitOfWork>();
            var emailService = scope.ServiceProvider.GetRequiredService<IEmailService>();
            var userVouchers = notification.TargetUsers.Select(u => new UserVoucher
            {
                UserId = u.UserId,
                VoucherId = notification.VoucherId
            }).ToList();
            userVoucherRepository.AddListUserVoucher(userVouchers);
            await uow.SaveChangesAsync(stoppingToken);
            foreach (var targetUser in notification.TargetUsers)
                try
                {
                    var emailBody = $$"""
                                      <!DOCTYPE html>
                                      <html lang="en">
                                      <head>
                                          <meta charset="UTF-8">
                                          <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                      </head>
                                      <body style="font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f4f4f4;">
                                          <div style="background-color: #ffffff; border-radius: 12px; padding: 40px 30px; text-align: center; box-shadow: 0 4px 10px rgba(0,0,0,0.05); border-top: 6px solid #111;">
                                              <h2 style="color: #111; margin-top: 0; margin-bottom: 25px; font-size: 28px; font-weight: 800; text-transform: uppercase; letter-spacing: 1px;">
                                                  Shoe Store
                                              </h2>

                                              <h3 style="color: #444; font-size: 20px; margin-bottom: 15px; font-weight: 600;">
                                                  Hi {{targetUser.Username}},
                                              </h3>

                                              <p style="font-size: 16px; margin-bottom: 30px; color: #666;">
                                                  Great news! A brand new exclusive voucher has just dropped into your wallet.
                                              </p>

                                              <div style="background-color: #fcfcfc; border: 2px dashed #333; border-radius: 12px; padding: 25px; margin-bottom: 35px; position: relative;">
                                                  <p style="font-size: 28px; font-weight: 900; color: #e74c3c; margin: 0; letter-spacing: 2px; text-transform: uppercase;">
                                                      🎁 {{notification.VoucherName.ToUpper()}}
                                                  </p>

                                                  <div style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee;">
                                                      <p style="font-size: 14px; color: #777; margin: 0;">
                                                          📅 Valid until: <strong style="color: #333;">{{notification.ValidTo:MMMM dd, yyyy}}</strong>
                                                      </p>
                                                  </div>
                                              </div>

                                              <p style="font-size: 16px; margin-bottom: 35px; color: #555;">
                                                  Don't let this deal slip away! Check your wallet and grab your favorite kicks now.
                                              </p>

                                              <a href="#" style="display: inline-block; background-color: #111; color: #ffffff; text-decoration: none; padding: 14px 36px; border-radius: 8px; font-weight: bold; font-size: 16px; text-transform: uppercase; letter-spacing: 1px;">
                                                  Shop Now
                                              </a>

                                              <hr style="border: none; border-top: 1px solid #eaeaea; margin: 40px 0 20px;">

                                              <p style="font-size: 13px; color: #999; margin: 0;">
                                                  Best regards,<br>
                                                  <strong style="color: #555; display: inline-block; margin-top: 5px;">Shoe Store Team</strong>
                                              </p>
                                          </div>
                                      </body>
                                      </html>
                                      """;
                    await emailService.SendEmailAsync(
                        senderEmail,
                        targetUser.Email,
                        "🎁 New Voucher Received!",
                        emailBody,
                        stoppingToken
                    );
                }
                catch (Exception ex)
                {
                    logger.LogError(ex, "Failed to send email to user {UserId} for voucher {VoucherId}",
                        targetUser.UserId, notification.VoucherId);
                }
        }
    }
}