const Profile = () => {
  // 模拟用户数据
  const user = {
    username: 'demo_user',
    email: 'demo@example.com',
    avatar: 'https://via.placeholder.com/150x150'
  }

  // 模拟订单数据
  const orders = [
    {
      id: 'ORD001',
      date: '2024-01-15',
      status: '已完成',
      total: 10998,
      items: ['iPhone 15 Pro', 'AirPods Pro']
    },
    {
      id: 'ORD002',
      date: '2024-01-10',
      status: '配送中',
      total: 12999,
      items: ['MacBook Pro']
    }
  ]

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">个人中心</h1>
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* 用户信息 */}
        <div className="lg:col-span-1">
          <div className="card text-center">
            <img 
              src={user.avatar} 
              alt="用户头像"
              className="w-24 h-24 rounded-full mx-auto mb-4"
            />
            <h3 className="text-xl font-semibold mb-2">{user.username}</h3>
            <p className="text-gray-600 mb-4">{user.email}</p>
            <button className="btn-primary">
              编辑资料
            </button>
          </div>
        </div>
        
        {/* 订单历史 */}
        <div className="lg:col-span-2">
          <div className="card">
            <h3 className="text-xl font-semibold mb-4">订单历史</h3>
            
            {orders.length === 0 ? (
              <p className="text-gray-600 text-center py-8">暂无订单记录</p>
            ) : (
              <div className="space-y-4">
                {orders.map(order => (
                  <div key={order.id} className="border rounded-lg p-4">
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <h4 className="font-semibold">订单号: {order.id}</h4>
                        <p className="text-gray-600">下单时间: {order.date}</p>
                      </div>
                      <span className={`px-2 py-1 rounded text-sm ${
                        order.status === '已完成' 
                          ? 'bg-green-100 text-green-800' 
                          : 'bg-blue-100 text-blue-800'
                      }`}>
                        {order.status}
                      </span>
                    </div>
                    <div className="mb-2">
                      <p className="text-sm text-gray-600">
                        商品: {order.items.join(', ')}
                      </p>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="font-bold text-primary">
                        总计: ¥{order.total}
                      </span>
                      <button className="btn-secondary text-sm">
                        查看详情
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Profile