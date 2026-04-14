namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class ProductAdminRequestDto
    {
        public string KeyWord { get; set; } = string.Empty;
        public bool InStock = false;
        public bool LowStock = false;
        public bool OutOfStock = false;

        private int _pageIndex = 1;
        public int PageIndex
        {
            get => _pageIndex;
            set => _pageIndex = value < 1 ? 1 : value;
        }

        private int _pageSize = 10;
        public int PageSize
        {
            get => _pageSize;
            set => _pageSize = Math.Clamp(value, 0, 50);
        }
    }
}
