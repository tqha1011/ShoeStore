using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services
{
    public class ProductService : IProductService
    {
        private readonly IUnitOfWork _uow;
        private readonly IProductRepository _productRepository;

        public ProductService(IUnitOfWork uow, IProductRepository productRepository)
        {
            _uow = uow;
            _productRepository = productRepository;
        }

    }

}
