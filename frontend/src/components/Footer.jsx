const Footer = () => {
  return (
    <footer className="bg-gray-800 text-white">
      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <h3 className="text-lg font-semibold mb-4">FullStack Mall</h3>
            <p className="text-gray-400">
              现代化的全栈电商平台，为您提供最佳的购物体验。
            </p>
          </div>
          
          <div>
            <h4 className="text-md font-semibold mb-4">快速链接</h4>
            <ul className="space-y-2 text-gray-400">
              <li><a href="/products" className="hover:text-white">商品</a></li>
              <li><a href="/cart" className="hover:text-white">购物车</a></li>
              <li><a href="/profile" className="hover:text-white">个人中心</a></li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-md font-semibold mb-4">客户服务</h4>
            <ul className="space-y-2 text-gray-400">
              <li><a href="#" className="hover:text-white">帮助中心</a></li>
              <li><a href="#" className="hover:text-white">联系我们</a></li>
              <li><a href="#" className="hover:text-white">退换货政策</a></li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-md font-semibold mb-4">关于我们</h4>
            <ul className="space-y-2 text-gray-400">
              <li><a href="#" className="hover:text-white">公司简介</a></li>
              <li><a href="#" className="hover:text-white">招聘信息</a></li>
              <li><a href="#" className="hover:text-white">隐私政策</a></li>
            </ul>
          </div>
        </div>
        
        <div className="border-t border-gray-700 mt-8 pt-8 text-center text-gray-400">
          <p>&copy; 2024 FullStack Mall. All rights reserved.</p>
        </div>
      </div>
    </footer>
  )
}

export default Footer