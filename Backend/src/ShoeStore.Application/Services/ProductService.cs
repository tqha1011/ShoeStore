using System;
using System.Collections.Generic;
using System.Text;
using ShoeStore.Application.Interface;

namespace ShoeStore.Application.Services
{
    internal class ProductService<Product> : IGenericRepository<Product>
    {
        private readonly IUnitOfWork _uow;
        private readonly IGenericRepository<Product> _productRepository;
        public ProductService(IUnitOfWork uow, IGenericRepository<Product> productRepository)
        {
            _uow = uow;
            _productRepository = productRepository;
        }

        public async Task<Product> AddASync(Product product)
        {
            _productRepository.AddAsync(product);
            await _uow.SaveChangesAsync();
            return product;
        }

        public async Task<IEnumerable<TEntity>> GetByIdAsync(Guild id)
        {
            return await _productRepository.GetByIdAsync(id);
        }

        public async Task<Product> UpdateAsync(Product product)
        {
            return await _productRepository.Update(product);
        }

        public async Task<Product> DeleteAsync(Product product)
        {
            return await _productRepository.Delete(product);
        }

    }
}
