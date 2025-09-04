import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authAPI } from '../services/apiService'

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

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

    // 验证密码确认
    if (formData.password !== formData.confirmPassword) {
      setError('两次输入的密码不一致')
      setLoading(false)
      return
    }

    // 基本验证
    if (formData.password.length < 6) {
      setError('密码长度至少6个字符')
      setLoading(false)
      return
    }

    try {
      const registerData = {
        username: formData.username,
        email: formData.email,
        password: formData.password
      }
      
      const response = await authAPI.register(registerData)
      
      // 注册成功后自动登录
      if (response.data.success) {
        const { token, user } = response.data.data
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))
        navigate('/', { state: { message: '注册成功，欢迎使用!' } })
      } else {
        setError(response.data.message || '注册失败')
      }
    } catch (err) {
      console.error('注册错误:', err)
      if (err.response?.data?.error) {
        const { code, message } = err.response.data.error
        if (code === 'USERNAME_EXISTS') {
          setError('用户名已存在，请选择其他用户名')
        } else if (code === 'EMAIL_EXISTS') {
          setError('邮箱已被注册，请使用其他邮箱')
        } else {
          setError(message || '注册失败，请稍后再试')
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
          <h2 className="text-2xl font-bold text-center mb-6">用户注册</h2>
          
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="form-label">用户名</label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                className="form-input"
                placeholder="请输入用户名（3-50个字符）"
                minLength={3}
                maxLength={50}
                required
              />
            </div>

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

            <div className="mb-4">
              <label className="form-label">密码</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="form-input"
                placeholder="请输入密码（至少6个字符）"
                minLength={6}
                required
              />
            </div>

            <div className="mb-6">
              <label className="form-label">确认密码</label>
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className="form-input"
                placeholder="请再次输入密码"
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full mb-4"
            >
              {loading ? '注册中...' : '注册'}
            </button>
          </form>

          <div className="text-center">
            <p className="text-gray-600">
              已有账户？ 
              <Link to="/login" className="text-primary hover:underline ml-1">
                立即登录
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Register