import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { cartAPI, orderAPI } from '../services/apiService'

const Cart = () => {
  const navigate = useNavigate()
  const [cartItems, setCartItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updating, setUpdating] = useState({})
  const [checkingOut, setCheckingOut] = useState(false)
  const [shippingAddress, setShippingAddress] = useState('')

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      navigate('/login', { state: { from: { pathname: '/cart' } } })
      return
    }
    fetchCartItems()
  }, [])

  const fetchCartItems = async () => {
    try {
      setLoading(true)
      const response = await cartAPI.getCart()
      
      if (response.data.success) {
        setCartItems(response.data.data)
      } else {
        setError(response.data.message || '获取购物车失败')
      }
    } catch (err) {
      console.error('获取购物车错误:', err)
      if (err.response?.status === 401) {
        localStorage.removeItem('token')
        navigate('/login', { state: { from: { pathname: '/cart' } } })
      } else {
        setError('网络错误，请稍后重试')
      }
    } finally {
      setLoading(false)
    }
  }

  const updateQuantity = async (itemId, newQuantity) => {
    if (newQuantity <= 0) {
      removeItem(itemId)
      return
    }

    try {
      setUpdating(prev => ({ ...prev, [itemId]: true }))
      const response = await cartAPI.updateCartItem(itemId, newQuantity)
      
      if (response.data.success) {
        setCartItems(cartItems.map(item => 
          item.id === itemId ? { ...item, quantity: newQuantity } : item
        ))
      } else {
        alert(response.data.message || '更新失败')
      }
    } catch (err) {
      console.error('更新购物车错误:', err)
      if (err.response?.data?.error) {
        const { code, message } = err.response.data.error
        if (code === 'INSUFFICIENT_STOCK') {
          alert('库存不足')
        } else {
          alert(message || '更新失败，请稍后重试')
        }
      } else {
        alert('网络错误，请稍后重试')
      }
    } finally {
      setUpdating(prev => ({ ...prev, [itemId]: false }))
    }
  }

  const removeItem = async (itemId) => {
    try {
      const response = await cartAPI.removeFromCart(itemId)
      
      if (response.data.success) {
        setCartItems(cartItems.filter(item => item.id !== itemId))
        alert('商品已从购物车移除')
      } else {
        alert(response.data.message || '删除失败')
      }
    } catch (err) {
      console.error('删除购物车商品错误:', err)
      alert('网络错误，请稍后重试')
    }
  }

  const clearCart = async () => {
    if (!confirm('确定要清空购物车吗？')) {
      return
    }

    try {
      const response = await cartAPI.clearCart()
      
      if (response.data.success) {
        setCartItems([])
        alert('购物车已清空')
      } else {
        alert(response.data.message || '清空失败')
      }
    } catch (err) {
      console.error('清空购物车错误:', err)
      alert('网络错误，请稍后重试')
    }
  }

  const getTotalPrice = () => {
    return cartItems.reduce((total, item) => total + (item.product.price * item.quantity), 0)
  }

  const getTotalItems = () => {
    return cartItems.reduce((total, item) => total + item.quantity, 0)
  }

  const handleCheckout = async () => {
    if (!shippingAddress.trim()) {
      alert('请填写收货地址')
      return
    }

    if (cartItems.length === 0) {
      alert('购物车为空')
      return
    }

    try {
      setCheckingOut(true)
      
      // 准备订单数据
      const orderData = {
        shippingAddress: shippingAddress.trim(),
        items: cartItems.map(item => ({
          productId: item.product.id,
          quantity: item.quantity
        }))
      }

      const response = await orderAPI.createOrder(orderData)
      
      if (response.data.success) {
        const order = response.data.data
        alert(`订单创建成功！订单号: ${order.id}`)
        setCartItems([])
        setShippingAddress('')
        navigate('/profile', { state: { activeTab: 'orders' } })
      } else {
        alert(response.data.message || '创建订单失败')
      }
    } catch (err) {
      console.error('创建订单错误:', err)
      if (err.response?.data?.error) {
        const { code, message } = err.response.data.error
        if (code === 'PRODUCT_NOT_FOUND') {
          alert('商品不存在，请刷新购物车')
          fetchCartItems()
        } else if (code === 'INSUFFICIENT_STOCK') {
          alert('商品库存不足，请调整数量')
          fetchCartItems()
        } else {
          alert(message || '创建订单失败，请稍后重试')
        }
      } else {
        alert('网络错误，请稍后重试')
      }
    } finally {
      setCheckingOut(false)
    }
  }

  if (cartItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-20">
          <div className="text-6xl mb-4">🛒</div>
          <h2 className="text-2xl font-bold mb-4">购物车为空</h2>
          <p className="text-gray-600 mb-6">快去挑选您喜欢的商品吧</p>
          <Link to="/products" className="btn-primary">
            去购物
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">购物车 ({getTotalItems()}件商品)</h1>
        <button
          onClick={clearCart}
          className="text-red-500 hover:text-red-700 text-sm"
        >
          清空购物车
        </button>
      </div>
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* 购物车商品列表 */}
        <div className="lg:col-span-2">
          {cartItems.map(item => (
            <div key={item.id} className="card mb-4">
              <div className="flex items-center">
                <img 
                  src={item.product.imageUrl || 'https://via.placeholder.com/100x100'} 
                  alt={item.product.name}
                  className="w-20 h-20 object-cover rounded mr-4"
                  onError={(e) => {
                    e.target.src = 'https://via.placeholder.com/100x100'
                  }}
                />
                <div className="flex-1">
                  <h3 className="text-lg font-semibold mb-1">
                    <Link 
                      to={`/products/${item.product.id}`}
                      className="hover:text-primary transition-colors"
                    >
                      {item.product.name}
                    </Link>
                  </h3>
                  <p className="text-gray-600 text-sm mb-1">{item.product.category}</p>
                  <p className="text-primary font-bold">¥{item.product.price}</p>
                  <p className="text-xs text-gray-500">库存: {item.product.stock}件</p>
                </div>
                <div className="flex items-center space-x-2">
                  <button 
                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                    disabled={updating[item.id]}
                    className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    -
                  </button>
                  <span className="w-12 text-center font-medium">
                    {updating[item.id] ? '...' : item.quantity}
                  </span>
                  <button 
                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                    disabled={updating[item.id] || item.quantity >= item.product.stock}
                    className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    +
                  </button>
                  <button 
                    onClick={() => removeItem(item.id)}
                    className="ml-4 px-3 py-1 text-red-500 hover:text-red-700 hover:bg-red-50 rounded text-sm transition-colors"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
          ))}
          
          <div className="mt-6">
            <Link to="/products" className="btn-secondary">
              继续购物
            </Link>
          </div>
        </div>
        
        {/* 订单结算区域 */}
        <div className="lg:col-span-1">
          <div className="card sticky top-4">
            <h3 className="text-xl font-semibold mb-4">订单结算</h3>
            
            {/* 收货地址 */}
            <div className="mb-4">
              <label className="block text-sm font-medium mb-2">收货地址 *</label>
              <textarea
                value={shippingAddress}
                onChange={(e) => setShippingAddress(e.target.value)}
                placeholder="请输入详细的收货地址"
                className="w-full border rounded-lg px-3 py-2 text-sm resize-none"
                rows={3}
              />
            </div>
            
            {/* 价格明细 */}
            <div className="space-y-2 mb-4">
              <div className="flex justify-between text-sm">
                <span>商品数量:</span>
                <span>{getTotalItems()}件</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>商品小计:</span>
                <span>¥{getTotalPrice().toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>运费:</span>
                <span className="text-green-600">免费</span>
              </div>
              <hr className="my-3" />
              <div className="flex justify-between text-lg font-bold">
                <span>总计:</span>
                <span className="text-primary">¥{getTotalPrice().toFixed(2)}</span>
              </div>
            </div>
            
            <button 
              onClick={handleCheckout}
              disabled={checkingOut || !shippingAddress.trim()}
              className="btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {checkingOut ? '处理中...' : '立即结算'}
            </button>
            
            <div className="mt-3 text-xs text-gray-500">
              <p>• 支持7天无理由退货</p>
              <p>• 全国包邮，48小时内发货</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Cart