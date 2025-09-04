import { Link, useNavigate } from 'react-router-dom'
import { useState } from 'react'

const Header = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const navigate = useNavigate()

  const handleLogout = () => {
    localStorage.removeItem('token')
    setIsLoggedIn(false)
    navigate('/')
  }

  return (
    <header className="bg-white shadow-md">
      <nav className="container mx-auto px-4 py-4 flex justify-between items-center">
        <Link to="/" className="text-2xl font-bold text-primary">
          FullStack Mall
        </Link>
        
        <div className="flex items-center space-x-6">
          <Link to="/products" className="text-gray-700 hover:text-primary">
            商品
          </Link>
          <Link to="/cart" className="text-gray-700 hover:text-primary">
            购物车
          </Link>
          
          {isLoggedIn ? (
            <div className="flex items-center space-x-4">
              <Link to="/profile" className="text-gray-700 hover:text-primary">
                个人中心
              </Link>
              <button 
                onClick={handleLogout}
                className="btn-secondary"
              >
                退出登录
              </button>
            </div>
          ) : (
            <div className="flex items-center space-x-2">
              <Link to="/login" className="btn-primary">
                登录
              </Link>
              <Link to="/register" className="btn-secondary">
                注册
              </Link>
            </div>
          )}
        </div>
      </nav>
    </header>
  )
}

export default Header