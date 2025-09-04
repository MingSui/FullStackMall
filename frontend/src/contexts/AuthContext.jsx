import { createContext, useContext, useState, useEffect } from 'react'
import Toast from '../components/Toast'

const AuthContext = createContext()

export const useAuth = () => {
    const context = useContext(AuthContext)
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider')
    }
    return context
}

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)
    const [toast, setToast] = useState(null)

    // 检查本地存储中的用户信息
    useEffect(() => {
        try {
            const token = localStorage.getItem('token')
            const userData = localStorage.getItem('user')

            if (token && userData) {
                setUser(JSON.parse(userData))
            }
        } catch (error) {
            console.error('解析用户数据失败:', error)
            // 清理无效数据
            localStorage.removeItem('token')
            localStorage.removeItem('user')
        } finally {
            setLoading(false)
        }
    }, [])

    const showToast = (message, type = 'success') => {
        setToast({ message, type })
    }

    const hideToast = () => {
        setToast(null)
    }

    const login = (token, userData) => {
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(userData))
        setUser(userData)
        showToast(`欢迎回来，${userData.username || userData.email}!`)
    }

    const logout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        setUser(null)
        showToast('已安全退出登录', 'info')
    }

    const isAuthenticated = () => {
        return !!user && !!localStorage.getItem('token')
    }

    const value = {
        user,
        login,
        logout,
        isAuthenticated,
        loading,
        showToast
    }

    return (
        <AuthContext.Provider value={value}>
            {children}
            {toast && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={hideToast}
                />
            )}
        </AuthContext.Provider>
    )
}