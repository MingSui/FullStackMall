import api from './api'

// 用户认证相关API
export const authAPI = {
  // 用户注册
  register: (userData) => {
    return api.post('/auth/register', userData)
  },

  // 用户登录
  login: (credentials) => {
    return api.post('/auth/login', credentials)
  },

  // 获取当前用户信息
  getCurrentUser: () => {
    return api.get('/auth/me')
  }
}

// 商品相关API
export const productAPI = {
  // 获取所有商品
  getProducts: () => {
    return api.get('/products')
  },

  // 根据ID获取商品详情
  getProductById: (id) => {
    return api.get(`/products/${id}`)
  },

  // 搜索商品
  searchProducts: (query) => {
    return api.get(`/products/search?q=${query}`)
  }
}

// 购物车相关API
export const cartAPI = {
  // 获取购物车内容
  getCart: () => {
    return api.get('/cart')
  },

  // 添加商品到购物车
  addToCart: (productId, quantity = 1) => {
    return api.post('/cart/add', { productId, quantity })
  },

  // 更新购物车商品数量
  updateCartItem: (itemId, quantity) => {
    return api.put(`/cart/items/${itemId}`, { quantity })
  },

  // 从购物车删除商品
  removeFromCart: (itemId) => {
    return api.delete(`/cart/items/${itemId}`)
  },

  // 清空购物车
  clearCart: () => {
    return api.delete('/cart')
  }
}

// 订单相关API
export const orderAPI = {
  // 获取用户订单列表
  getOrders: () => {
    return api.get('/orders')
  },

  // 创建订单
  createOrder: (orderData) => {
    return api.post('/orders', orderData)
  },

  // 根据ID获取订单详情
  getOrderById: (id) => {
    return api.get(`/orders/${id}`)
  }
}