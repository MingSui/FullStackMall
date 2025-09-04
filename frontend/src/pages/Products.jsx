const Products = () => {
  // 模拟商品数据
  const products = [
    {
      id: 1,
      name: 'iPhone 15 Pro',
      price: 8999,
      image: 'https://via.placeholder.com/300x300',
      description: '最新款iPhone，性能强劲'
    },
    {
      id: 2,
      name: 'MacBook Pro',
      price: 12999,
      image: 'https://via.placeholder.com/300x300',
      description: '专业级笔记本电脑'
    },
    {
      id: 3,
      name: 'AirPods Pro',
      price: 1999,
      image: 'https://via.placeholder.com/300x300',
      description: '降噪无线耳机'
    },
    {
      id: 4,
      name: 'iPad Air',
      price: 4399,
      image: 'https://via.placeholder.com/300x300',
      description: '轻薄便携平板电脑'
    }
  ]

  const addToCart = (product) => {
    // TODO: 实现添加到购物车功能
    console.log('添加到购物车:', product)
    alert(`${product.name} 已添加到购物车`)
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">商品列表</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {products.map(product => (
          <div key={product.id} className="card">
            <img 
              src={product.image} 
              alt={product.name}
              className="w-full h-48 object-cover rounded-lg mb-4"
            />
            <h3 className="text-lg font-semibold mb-2">{product.name}</h3>
            <p className="text-gray-600 mb-3">{product.description}</p>
            <div className="flex justify-between items-center">
              <span className="text-xl font-bold text-primary">
                ¥{product.price}
              </span>
              <button 
                onClick={() => addToCart(product)}
                className="btn-primary"
              >
                加入购物车
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default Products