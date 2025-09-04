import { useState, useEffect } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { orderAPI } from '../services/apiService'

const Profile = () => {
  const { user, logout, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [ordersLoading, setOrdersLoading] = useState(false)
  const [error, setError] = useState('')
  const [activeTab, setActiveTab] = useState('profile')
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate('/login', { state: { from: location } })
      return
    }

    // 检查是否需要切换到订单页签
    if (location.state?.activeTab === 'orders') {
      setActiveTab('orders')
    }

    setLoading(false)
  }, [isAuthenticated, navigate, location])

  useEffect(() => {
    if (activeTab === 'orders' && isAuthenticated()) {
      fetchOrders(0)
    }
  }, [activeTab, isAuthenticated])

  const fetchOrders = async (page = 0) => {
    try {
      setOrdersLoading(true)
      const response = await orderAPI.getMyOrders({ page, size: 5 })

      if (response.data.success) {
        const { content, totalPages } = response.data.data
        setOrders(content)
        setTotalPages(totalPages)
        setCurrentPage(page)
      } else {
        setError(response.data.message || '获取订单失败')
      }
    } catch (err) {
      console.error('获取订单错误:', err)
      if (err.response?.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { state: { from: location } })
      } else {
        setError('网络错误，请稍后重试')
      }
    } finally {
      setOrdersLoading(false)
    }
  }

  const cancelOrder = async (orderId) => {
    if (!confirm('确定要取消这个订单吗？')) {
      return
    }

    try {
      const response = await orderAPI.cancelOrder(orderId)

      if (response.data.success) {
        alert('订单已取消')
        fetchOrders(currentPage)
      } else {
        alert(response.data.message || '取消订单失败')
      }
    } catch (err) {
      console.error('取消订单错误:', err)
      if (err.response?.data?.error) {
        alert(err.response.data.error.message || '取消订单失败')
      } else {
        alert('网络错误，请稍后重试')
      }
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800'
      case 'CONFIRMED':
        return 'bg-blue-100 text-blue-800'
      case 'SHIPPED':
        return 'bg-purple-100 text-purple-800'
      case 'DELIVERED':
        return 'bg-green-100 text-green-800'
      case 'CANCELLED':
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusText = (status) => {
    switch (status) {
      case 'PENDING':
        return '待处理'
      case 'CONFIRMED':
        return '已确认'
      case 'SHIPPED':
        return '已发货'
      case 'DELIVERED':
        return '已送达'
      case 'CANCELLED':
        return '已取消'
      default:
        return status
    }
  }

  // 格式化日期显示
  const formatDate = (dateString) => {
    if (!dateString) return '未知'
    try {
      const date = new Date(dateString)
      if (isNaN(date.getTime())) {
        return '未知'
      }
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    } catch (error) {
      console.error('日期格式化错误:', error)
      return '未知'
    }
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">个人中心</h1>

      {/* 标签导航 */}
      <div className="mb-8">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setActiveTab('profile')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${activeTab === 'profile'
                ? 'border-primary text-primary'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
            >
              个人信息
            </button>
            <button
              onClick={() => setActiveTab('orders')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${activeTab === 'orders'
                ? 'border-primary text-primary'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
            >
              订单历史
            </button>
          </nav>
        </div>
      </div>

      {/* 个人信息页签 */}
      {activeTab === 'profile' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-1">
            <div className="card text-center">
              <div className="w-24 h-24 bg-primary text-white rounded-full mx-auto mb-4 flex items-center justify-center text-2xl font-bold">
                {user.username.charAt(0).toUpperCase()}
              </div>
              <h3 className="text-xl font-semibold mb-2">{user.username}</h3>
              <p className="text-gray-600 mb-4">{user.email}</p>
              <div className="space-y-2">
                <p className="text-sm text-gray-500">角色: {user.role === 'ADMIN' ? '管理员' : '用户'}</p>
                <p className="text-sm text-gray-500">注册时间: {formatDate(user.createdAt)}</p>
              </div>
              <div className="mt-6 space-y-2">
                <button
                  onClick={() => setActiveTab('orders')}
                  className="btn-primary w-full"
                >
                  查看订单
                </button>
                <button
                  onClick={handleLogout}
                  className="btn-secondary w-full"
                >
                  退出登录
                </button>
              </div>
            </div>
          </div>

          <div className="lg:col-span-2">
            <div className="card">
              <h3 className="text-xl font-semibold mb-4">账户概览</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="text-center p-4 bg-blue-50 rounded-lg">
                  <div className="text-2xl font-bold text-blue-600">{orders.length}</div>
                  <div className="text-sm text-gray-600">总订单数</div>
                </div>
                <div className="text-center p-4 bg-green-50 rounded-lg">
                  <div className="text-2xl font-bold text-green-600">
                    {orders.filter(o => o.status === 'DELIVERED').length}
                  </div>
                  <div className="text-sm text-gray-600">已完成</div>
                </div>
                <div className="text-center p-4 bg-yellow-50 rounded-lg">
                  <div className="text-2xl font-bold text-yellow-600">
                    {orders.filter(o => ['PENDING', 'CONFIRMED', 'SHIPPED'].includes(o.status)).length}
                  </div>
                  <div className="text-sm text-gray-600">进行中</div>
                </div>
              </div>

              <div className="mt-6">
                <h4 className="font-semibold mb-3">快捷操作</h4>
                <div className="grid grid-cols-2 gap-4">
                  <Link to="/products" className="btn-secondary text-center">
                    继续购物
                  </Link>
                  <Link to="/cart" className="btn-secondary text-center">
                    查看购物车
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 订单历史页签 */}
      {activeTab === 'orders' && (
        <div className="space-y-6">
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              {error}
              <button
                onClick={() => fetchOrders(currentPage)}
                className="ml-4 text-sm underline"
              >
                重试
              </button>
            </div>
          )}

          {ordersLoading ? (
            <div className="text-center py-8">
              <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
              <p className="mt-2 text-sm text-gray-500">加载订单中...</p>
            </div>
          ) : orders.length === 0 ? (
            <div className="card text-center py-12">
              <div className="text-4xl mb-4">📋</div>
              <h3 className="text-lg font-semibold mb-2">暂无订单记录</h3>
              <p className="text-gray-600 mb-4">您还没有任何订单，快去购物吧！</p>
              <Link to="/products" className="btn-primary">
                去购物
              </Link>
            </div>
          ) : (
            <>
              <div className="space-y-4">
                {orders.map(order => (
                  <div key={order.id} className="card">
                    <div className="flex justify-between items-start mb-4">
                      <div>
                        <h4 className="font-semibold text-lg">订单号: {order.id}</h4>
                        <p className="text-gray-600 text-sm">
                          下单时间: {new Date(order.createdAt).toLocaleString()}
                        </p>
                      </div>
                      <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(order.status)}`}>
                        {getStatusText(order.status)}
                      </span>
                    </div>

                    <div className="mb-4">
                      <h5 className="font-medium mb-2">商品列表:</h5>
                      <div className="space-y-2">
                        {order.items.map((item, index) => (
                          <div key={index} className="flex items-center text-sm">
                            <img
                              src={item.product.imageUrl || 'https://via.placeholder.com/40x40'}
                              alt={item.product.name}
                              className="w-10 h-10 object-cover rounded mr-3"
                              onError={(e) => {
                                e.target.src = 'https://via.placeholder.com/40x40'
                              }}
                            />
                            <div className="flex-1">
                              <span className="font-medium">{item.product.name}</span>
                              <span className="text-gray-500 ml-2">x{item.quantity}</span>
                            </div>
                            <span className="text-primary font-medium">¥{item.price}</span>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div className="border-t pt-4">
                      <div className="flex justify-between items-center">
                        <div>
                          <span className="text-lg font-bold text-primary">
                            总计: ¥{order.totalAmount}
                          </span>
                          <div className="text-sm text-gray-500 mt-1">
                            收货地址: {order.shippingAddress}
                          </div>
                        </div>
                        <div className="space-x-2">
                          <Link
                            to={`/orders/${order.id}`}
                            className="btn-secondary text-sm"
                          >
                            查看详情
                          </Link>
                          {(order.status === 'PENDING' || order.status === 'CONFIRMED') && (
                            <button
                              onClick={() => cancelOrder(order.id)}
                              className="btn-outline text-sm text-red-600 border-red-600 hover:bg-red-50"
                            >
                              取消订单
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* 分页 */}
              {totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-8">
                  <button
                    onClick={() => fetchOrders(currentPage - 1)}
                    disabled={currentPage === 0 || ordersLoading}
                    className="px-4 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                  >
                    上一页
                  </button>

                  {[...Array(totalPages)].map((_, index) => (
                    <button
                      key={index}
                      onClick={() => fetchOrders(index)}
                      disabled={ordersLoading}
                      className={`px-4 py-2 border rounded disabled:cursor-not-allowed ${currentPage === index
                        ? 'bg-primary text-white'
                        : 'hover:bg-gray-50'
                        }`}
                    >
                      {index + 1}
                    </button>
                  ))}

                  <button
                    onClick={() => fetchOrders(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1 || ordersLoading}
                    className="px-4 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                  >
                    下一页
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}

export default Profile