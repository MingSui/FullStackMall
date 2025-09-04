import { useState } from 'react'

const Cart = () => {
  // 模拟购物车数据
  const [cartItems, setCartItems] = useState([
    {
      id: 1,
      name: 'iPhone 15 Pro',
      price: 8999,
      quantity: 1,
      image: 'https://via.placeholder.com/100x100'
    },
    {
      id: 2,
      name: 'AirPods Pro',
      price: 1999,
      quantity: 2,
      image: 'https://via.placeholder.com/100x100'
    }
  ])

  const updateQuantity = (id, newQuantity) => {
    if (newQuantity <= 0) {
      removeItem(id)
      return
    }
    setCartItems(cartItems.map(item => 
      item.id === id ? { ...item, quantity: newQuantity } : item
    ))
  }

  const removeItem = (id) => {
    setCartItems(cartItems.filter(item => item.id !== id))
  }

  const getTotalPrice = () => {
    return cartItems.reduce((total, item) => total + (item.price * item.quantity), 0)
  }

  const handleCheckout = () => {
    // TODO: 实现结算功能
    console.log('结算商品:', cartItems)
    alert('结算功能待实现')
  }

  if (cartItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-20">
          <h2 className="text-2xl font-bold mb-4">购物车为空</h2>
          <p className="text-gray-600 mb-6">快去挑选您喜欢的商品吧</p>
          <a href="/products" className="btn-primary">
            去购物
          </a>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">购物车</h1>
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          {cartItems.map(item => (
            <div key={item.id} className="card mb-4">
              <div className="flex items-center">
                <img 
                  src={item.image} 
                  alt={item.name}
                  className="w-20 h-20 object-cover rounded mr-4"
                />
                <div className="flex-1">
                  <h3 className="text-lg font-semibold">{item.name}</h3>
                  <p className="text-primary font-bold">¥{item.price}</p>
                </div>
                <div className="flex items-center space-x-2">
                  <button 
                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                    className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center"
                  >
                    -
                  </button>
                  <span className="w-8 text-center">{item.quantity}</span>
                  <button 
                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                    className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center"
                  >
                    +
                  </button>
                  <button 
                    onClick={() => removeItem(item.id)}
                    className="ml-4 text-red-500 hover:text-red-700"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
        
        <div className="lg:col-span-1">
          <div className="card">
            <h3 className="text-xl font-semibold mb-4">订单总计</h3>
            <div className="space-y-2 mb-4">
              <div className="flex justify-between">
                <span>商品小计:</span>
                <span>¥{getTotalPrice()}</span>
              </div>
              <div className="flex justify-between">
                <span>运费:</span>
                <span>免费</span>
              </div>
              <hr />
              <div className="flex justify-between text-lg font-bold">
                <span>总计:</span>
                <span className="text-primary">¥{getTotalPrice()}</span>
              </div>
            </div>
            <button 
              onClick={handleCheckout}
              className="btn-primary w-full"
            >
              立即结算
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Cart