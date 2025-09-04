import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Login from '../pages/Login'
import { authAPI } from '../services/apiService'

// Mock the API service
vi.mock('../services/apiService', () => ({
  authAPI: {
    login: vi.fn()
  }
}))

// Mock react-router-dom
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => ({ state: {} })
  }
})

const LoginWrapper = () => (
  <BrowserRouter>
    <Login />
  </BrowserRouter>
)

describe('Login Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders login form correctly', () => {
    render(<LoginWrapper />)
    
    expect(screen.getByText('用户登录')).toBeInTheDocument()
    expect(screen.getByLabelText('邮箱')).toBeInTheDocument()
    expect(screen.getByLabelText('密码')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '登录' })).toBeInTheDocument()
    expect(screen.getByText('还没有账户？')).toBeInTheDocument()
  })

  it('shows validation errors for empty form submission', async () => {
    render(<LoginWrapper />)
    
    const submitButton = screen.getByRole('button', { name: '登录' })
    fireEvent.click(submitButton)
    
    // Check if HTML5 validation is working
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    
    expect(emailInput).toBeRequired()
    expect(passwordInput).toBeRequired()
  })

  it('updates input values when typing', () => {
    render(<LoginWrapper />)
    
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    
    expect(emailInput.value).toBe('test@example.com')
    expect(passwordInput.value).toBe('password123')
  })

  it('handles successful login', async () => {
    const mockResponse = {
      data: {
        success: true,
        data: {
          token: 'mock-token',
          user: { id: 1, email: 'test@example.com', username: 'testuser' }
        }
      }
    }
    
    authAPI.login.mockResolvedValue(mockResponse)
    
    render(<LoginWrapper />)
    
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const submitButton = screen.getByRole('button', { name: '登录' })
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(authAPI.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123'
      })
    })
    
    expect(localStorage.setItem).toHaveBeenCalledWith('token', 'mock-token')
    expect(localStorage.setItem).toHaveBeenCalledWith('user', JSON.stringify(mockResponse.data.data.user))
    expect(mockNavigate).toHaveBeenCalledWith('/', { replace: true })
  })

  it('handles login failure with invalid credentials', async () => {
    const mockError = {
      response: {
        data: {
          error: {
            code: 'INVALID_CREDENTIALS',
            message: '邮箱或密码错误'
          }
        }
      }
    }
    
    authAPI.login.mockRejectedValue(mockError)
    
    render(<LoginWrapper />)
    
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const submitButton = screen.getByRole('button', { name: '登录' })
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('邮箱或密码错误')).toBeInTheDocument()
    })
    
    expect(localStorage.setItem).not.toHaveBeenCalled()
    expect(mockNavigate).not.toHaveBeenCalled()
  })

  it('handles network error', async () => {
    authAPI.login.mockRejectedValue(new Error('Network Error'))
    
    render(<LoginWrapper />)
    
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const submitButton = screen.getByRole('button', { name: '登录' })
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('网络错误，请检查网络连接后重试')).toBeInTheDocument()
    })
  })

  it('shows loading state during login', async () => {
    let resolveLogin
    const loginPromise = new Promise(resolve => {
      resolveLogin = resolve
    })
    authAPI.login.mockReturnValue(loginPromise)
    
    render(<LoginWrapper />)
    
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const submitButton = screen.getByRole('button', { name: '登录' })
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)
    
    expect(screen.getByText('登录中...')).toBeInTheDocument()
    expect(submitButton).toBeDisabled()
    
    // Resolve the promise
    resolveLogin({
      data: {
        success: true,
        data: { token: 'mock-token', user: {} }
      }
    })
    
    await waitFor(() => {
      expect(screen.getByText('登录')).toBeInTheDocument()
    })
  })

  it('navigates to register page when clicking register link', () => {
    render(<LoginWrapper />)
    
    const registerLink = screen.getByText('立即注册')
    expect(registerLink).toHaveAttribute('href', '/register')
  })
})