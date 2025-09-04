import { useState, useEffect } from 'react'

const Toast = ({ message, type = 'success', duration = 3000, onClose }) => {
    const [isVisible, setIsVisible] = useState(true)

    useEffect(() => {
        const timer = setTimeout(() => {
            setIsVisible(false)
            setTimeout(() => onClose && onClose(), 300)
        }, duration)

        return () => clearTimeout(timer)
    }, [duration, onClose])

    const bgColor = type === 'success' ? 'bg-green-500' : type === 'error' ? 'bg-red-500' : 'bg-blue-500'

    return (
        <div className={`fixed top-4 right-4 z-50 transition-all duration-300 ${isVisible ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-2'
            }`}>
            <div className={`${bgColor} text-white px-6 py-3 rounded-lg shadow-lg flex items-center gap-3`}>
                <span>{message}</span>
                <button
                    onClick={() => {
                        setIsVisible(false)
                        setTimeout(() => onClose && onClose(), 300)
                    }}
                    className="text-white hover:text-gray-200"
                >
                    ✕
                </button>
            </div>
        </div>
    )
}

export default Toast