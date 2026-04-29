using Microsoft.SemanticKernel.ChatCompletion;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.StatisticsInterface;

namespace ShoeStore.Application.Services;

public class ChatBotService(IStatisticsService statisticsService, IChatCompletionService chatCompletionService)
    : IChatBotService
{
    public async Task<string> GenerateCampaignAsync(StatisticsDataDto data, CancellationToken token)
    {
        var summaryData = await statisticsService.GetStatisticsSummaryAsync(token);
        var top3Products = await statisticsService.GetProductsHighestStatisticsAsync(token);

        var totalRevenue = summaryData.Value.TotalRevenue;
        var totalOrders = summaryData.Value.TotalOrders;

        var top1 = top3Products.Value.Count > 0
            ? $"{top3Products.Value[0].ProductName} - Doanh thu: {top3Products.Value[0].TotalRevenue} VND - Tổng hóa đơn của sản phẩm: {top3Products.Value[0].TotalInvoices}"
            : "Đang cập nhật ";
        var top2 = top3Products.Value.Count > 1
            ? $"{top3Products.Value[1].ProductName} - Doanh thu: {top3Products.Value[1].TotalRevenue} VND - Tổng hóa đơn của sản phẩm: {top3Products.Value[1].TotalInvoices}"
            : "Đang cập nhật";
        var top3 = top3Products.Value.Count > 2
            ? $"{top3Products.Value[2].ProductName} - Doanh thu: {top3Products.Value[2].TotalRevenue} VND - Tổng hóa đơn của sản phẩm   : {top3Products.Value[2].TotalInvoices}"
            : "Đang cập nhật";

        var systemPrompt = $"""
                            Bạn là Giám đốc Marketing và Hoạch định chiến lược (CMO) xuất chúng của hệ thống cửa hàng. Bạn có khả năng đọc vị dữ liệu tuyệt vời và luôn tạo ra những chiến dịch thực chiến, mang lại tỷ lệ chuyển đổi cao.

                            Dưới đây là BÁO CÁO KINH DOANH HIỆN TẠI do hệ thống cung cấp:
                            - Tổng doanh thu đạt được: {totalRevenue} VND
                            - Tổng số đơn hàng đã chốt: {totalOrders} đơn hàng
                            - Top 3 sản phẩm bán chạy nhất: 
                              1. {top1}
                              2. {top2}
                              3. {top3}

                            Nhiệm vụ của bạn:
                            Dựa vào các số liệu trên, hãy phân tích nhanh tình hình và đề xuất MỘT (01) chiến dịch kinh doanh/marketing phù hợp nhất cho tháng tới để tiếp tục tăng trưởng hoặc bùng nổ doanh số.

                            Yêu cầu định dạng đầu ra (Trình bày rõ ràng, không giải thích dài dòng):
                            1. TÊN CHIẾN DỊCH: [Đặt một cái tên thật giật tít, bắt tai]
                            2. THÔNG ĐIỆP CỐT LÕI (Slogan): [1 câu duy nhất]
                            3. PHÂN TÍCH NHANH: [Giải thích trong 2 dòng vì sao chiến dịch này lại hợp với số liệu trên]
                            4. HÀNH ĐỘNG THỰC THI:
                               - [Gạch đầu dòng 1: Làm gì với Top 3 sản phẩm hot?]
                               - [Gạch đầu dòng 2: Có chương trình khuyến mãi/combo gì để tăng tổng đơn hàng không?]
                            """;
        var chat = new ChatHistory(systemPrompt);

        var response = await chatCompletionService.GetChatMessageContentAsync(chat, cancellationToken: token);
        return response.Content ?? "Không thể lên campaign ngay bây giờ";
    }
}