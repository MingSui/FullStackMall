import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Register from '../pages/Register'
import { authAPI } from '../services/apiService'

// Mock the API service
vi.mock('../services/apiService', () => ({
  authAPI: {
    register: vi.fn()
  }
}))

// Mock react-router-dom
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate
  }
})

const RegisterWrapper = () => (
  <BrowserRouter>
    <Register />
  </BrowserRouter>
)

describe('Register Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders register form correctly', () => {
    render(<RegisterWrapper />)
    
    expect(screen.getByText('用户注册')).toBeInTheDocument()
    expect(screen.getByLabelText('用户名')).toBeInTheDocument()
    expect(screen.getByLabelText('邮箱')).toBeInTheDocument()
    expect(screen.getByLabelText('密码')).toBeInTheDocument()
    expect(screen.getByLabelText('确认密码')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '注册' })).toBeInTheDocument()
    expect(screen.getByText('已有账户？')).toBeInTheDocument()
  })

  it('updates input values when typing', () => {
    render(<RegisterWrapper />)
    
    const usernameInput = screen.getByLabelText('用户名')
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const confirmPasswordInput = screen.getByLabelText('确认密码')
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } })
    
    expect(usernameInput.value).toBe('testuser')
    expect(emailInput.value).toBe('test@example.com')
    expect(passwordInput.value).toBe('password123')
    expect(confirmPasswordInput.value).toBe('password123')
  })

  it('shows error when passwords do not match', async () => {
    render(<RegisterWrapper />)
    
    const usernameInput = screen.getByLabelText('用户名')
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const confirmPasswordInput = screen.getByLabelText('确认密码')
    const submitButton = screen.getByRole('button', { name: '注册' })
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.change(confirmPasswordInput, { target: { value: 'differentpassword' } })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('两次输入的密码不一致')).toBeInTheDocument()
    })
    
    expect(authAPI.register).not.toHaveBeenCalled()
  })

  it('shows error when password is too short', async () => {
    render(<RegisterWrapper />)
    
    const usernameInput = screen.getByLabelText('用户名')
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const confirmPasswordInput = screen.getByLabelText('确认密码')
    const submitButton = screen.getByRole('button', { name: '注册' })
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: '12345' } })
    fireEvent.change(confirmPasswordInput, { target: { value: '12345' } })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('密码长度至少6个字符')).toBeInTheDocument()
    })
    
    expect(authAPI.register).not.toHaveBeenCalled()
  })

  it('handles successful registration', async () => {
    const mockResponse = {
      data: {
        success: true,
        data: {
          token: 'mock-token',
          user: { id: 1, email: 'test@example.com', username: 'testuser' }
        }
      }
    }
    
    authAPI.register.mockResolvedValue(mockResponse)
    
    render(<RegisterWrapper />)
    
    const usernameInput = screen.getByLabelText('用户名')
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const confirmPasswordInput = screen.getByLabelText('确认密码')
    const submitButton = screen.getByRole('button', { name: '注册' })
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(authAPI.register).toHaveBeenCalledWith({
        username: 'testuser',
        email: 'test@example.com',
        password: 'password123'
      })
    })
    
    expect(localStorage.setItem).toHaveBeenCalledWith('token', 'mock-token')
    expect(localStorage.setItem).toHaveBeenCalledWith('user', JSON.stringify(mockResponse.data.data.user))
    expect(mockNavigate).toHaveBeenCalledWith('/', { state: { message: '注册成功，欢迎使用!' } })
  })

  it('handles registration failure with username exists', async () => {
    const mockError = {
      response: {
        data: {
          error: {
            code: 'USERNAME_EXISTS',
            message: '用户名已存在'
          }
        }
      }
    }
    
    authAPI.register.mockRejectedValue(mockError)
    
    render(<RegisterWrapper />)
    
    const usernameInput = screen.getByLabelText('用户名')
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const confirmPasswordInput = screen.getByLabelText('确认密码')
    const submitButton = screen.getByRole('button', { name: '注册' })
    
    fireEvent.change(usernameInput, { target: { value: 'existinguser' } })
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('用户名已存在，请选择其他用户名')).toBeInTheDocument()
    })
    
    expect(localStorage.setItem).not.toHaveBeenCalled()
    expect(mockNavigate).not.toHaveBeenCalled()
  })

  it('handles registration failure with email exists', async () => {
    const mockError = {
      response: {
        data: {
          error: {
            code: 'EMAIL_EXISTS',
            message: '邮箱已存在'
          }
        }
      }
    }
    
    authAPI.register.mockRejectedValue(mockError)
    
    render(<RegisterWrapper />)
    
    const usernameInput = screen.getByLabelText('用户名')
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const confirmPasswordInput = screen.getByLabelText('确认密码')
    const submitButton = screen.getByRole('button', { name: '注册' })
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    fireEvent.change(emailInput, { target: { value: 'existing@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText('邮箱已被注册，请使用其他邮箱')).toBeInTheDocument()
    })
  })

  it('shows loading state during registration', async () => {
    let resolveRegister
    const registerPromise = new Promise(resolve => {
      resolveRegister = resolve
    })
    authAPI.register.mockReturnValue(registerPromise)
    
    render(<RegisterWrapper />)
    
    const usernameInput = screen.getByLabelText('用户名')
    const emailInput = screen.getByLabelText('邮箱')
    const passwordInput = screen.getByLabelText('密码')
    const confirmPasswordInput = screen.getByLabelText('确认密码')
    const submitButton = screen.getByRole('button', { name: '注册' })
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)
    
    expect(screen.getByText('注册中...')).toBeInTheDocument()
    expect(submitButton).toBeDisabled()
    
    // Resolve the promise
    resolveRegister({
      data: {
        success: true,
        data: { token: 'mock-token', user: {} }
      }
    })
    
    await waitFor(() => {
      expect(screen.getByText('注册')).toBeInTheDocument()
    })
  })

  it('navigates to login page when clicking login link', () => {
    render(<RegisterWrapper />)
    
    const loginLink = screen.getByText('立即登录')
    expect(loginLink).toHaveAttribute('href', '/login')
  })
})