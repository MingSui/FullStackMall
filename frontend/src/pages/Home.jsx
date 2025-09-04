import { Link } from 'react-router-dom'

const Home = () => {
  return (
    <div className="container mx-auto px-4 py-8">
      {/* Hero Section */}
      <section className="text-center py-20 bg-gradient-to-r from-primary to-blue-600 text-white rounded-lg mb-12">
        <h1 className="text-5xl font-bold mb-6">欢迎来到 FullStack Mall</h1>
        <p className="text-xl mb-8">现代化的全栈电商平台，为您提供最佳购物体验</p>
        <Link to="/products" className="bg-white text-primary px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors">
          开始购物
        </Link>
      </section>

      {/* Features Section */}
      <section className="mb-12">
        <h2 className="text-3xl font-bold text-center mb-8">平台特色</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="card text-center">
            <div className="text-4xl mb-4">🛍️</div>
            <h3 className="text-xl font-semibold mb-2">丰富商品</h3>
            <p className="text-gray-600">精选优质商品，满足您的各种需求</p>
          </div>
          
          <div className="card text-center">
            <div className="text-4xl mb-4">🚚</div>
            <h3 className="text-xl font-semibold mb-2">快速配送</h3>
            <p className="text-gray-600">高效物流，让您尽快收到心仪商品</p>
          </div>
          
          <div className="card text-center">
            <div className="text-4xl mb-4">🔒</div>
            <h3 className="text-xl font-semibold mb-2">安全支付</h3>
            <p className="text-gray-600">多重加密保护，确保交易安全</p>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="text-center py-12 bg-gray-100 rounded-lg">
        <h2 className="text-3xl font-bold mb-4">立即开始您的购物之旅</h2>
        <p className="text-gray-600 mb-6">注册成为会员，享受更多优惠</p>
        <div className="space-x-4">
          <Link to="/register" className="btn-primary">
            立即注册
          </Link>
          <Link to="/login" className="btn-secondary">
            会员登录
          </Link>
        </div>
      </section>
    </div>
  )
}

export default Home