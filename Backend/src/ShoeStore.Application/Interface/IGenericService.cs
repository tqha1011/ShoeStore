using System;
using System.Collections.Generic;
using System.Text;

namespace ShoeStore.Application.Interface
{
    internal interface IGenericService<TEntity, TEntityId>
    where TEntity : Entity<TEntityId>
    {

    }
}
