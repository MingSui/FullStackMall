import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { authAPI } from '../services/apiService'

const Login = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const location = useLocation()
  const from = location.state?.from?.pathname || '/'

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const response = await authAPI.login(formData)
      
      if (response.data.success) {
        const { token, user } = response.data.data
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))
        
        // 跳转到之前访问的页面或首页
        navigate(from, { replace: true })
      } else {
        setError(response.data.message || '登录失败')
      }
    } catch (err) {
      console.error('登录错误:', err)
      if (err.response?.data?.error) {
        const { code, message } = err.response.data.error
        if (code === 'INVALID_CREDENTIALS') {
          setError('邮箱或密码错误')
        } else if (code === 'USER_NOT_FOUND') {
          setError('用户不存在，请检查邮箱地址')
        } else {
          setError(message || '登录失败，请稍后再试')
        }
      } else {
        setError('网络错误，请检查网络连接后重试')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-md mx-auto">
        <div className="card">
          <h2 className="text-2xl font-bold text-center mb-6">用户登录</h2>
          
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="form-label">邮箱</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="form-input"
                placeholder="请输入邮箱地址"
                required
              />
            </div>

            <div className="mb-6">
              <label className="form-label">密码</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="form-input"
                placeholder="请输入密码"
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full mb-4"
            >
              {loading ? '登录中...' : '登录'}
            </button>
          </form>

          <div className="text-center">
            <p className="text-gray-600">
              还没有账户？ 
              <Link to="/register" className="text-primary hover:underline ml-1">
                立即注册
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login