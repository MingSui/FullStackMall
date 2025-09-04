import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { productAPI, cartAPI } from '../services/apiService'

const Products = () => {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [addingToCart, setAddingToCart] = useState({})

  // 获取商品数据
  const fetchProducts = async (page = 0, keyword = '', category = '') => {
    try {
      setLoading(true)
      let response
      
      if (keyword || category) {
        response = await productAPI.searchProducts({
          keyword,
          category,
          page,
          size: 8
        })
      } else {
        response = await productAPI.getProducts({
          page,
          size: 8,
          sort: 'id',
          direction: 'DESC'
        })
      }
      
      if (response.data.success) {
        const { content, totalPages } = response.data.data
        setProducts(content)
        setTotalPages(totalPages)
        setCurrentPage(page)
      } else {
        setError(response.data.message || '获取商品失败')
      }
    } catch (err) {
      console.error('获取商品错误:', err)
      setError('网络错误，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  // 获取商品分类
  const fetchCategories = async () => {
    try {
      const response = await productAPI.getCategories()
      if (response.data.success) {
        setCategories(response.data.data)
      }
    } catch (err) {
      console.error('获取分类错误:', err)
    }
  }

  useEffect(() => {
    fetchProducts()
    fetchCategories()
  }, [])

  // 搜索处理
  const handleSearch = (e) => {
    e.preventDefault()
    setCurrentPage(0)
    fetchProducts(0, searchKeyword, selectedCategory)
  }

  // 分类选择
  const handleCategoryChange = (category) => {
    setSelectedCategory(category)
    setCurrentPage(0)
    fetchProducts(0, searchKeyword, category)
  }

  // 分页处理
  const handlePageChange = (page) => {
    fetchProducts(page, searchKeyword, selectedCategory)
  }

  // 添加到购物车
  const addToCart = async (product) => {
    const token = localStorage.getItem('token')
    if (!token) {
      alert('请先登录')
      return
    }

    try {
      setAddingToCart(prev => ({ ...prev, [product.id]: true }))
      const response = await cartAPI.addToCart(product.id, 1)
      
      if (response.data.success) {
        alert(`${product.name} 已添加到购物车`)
      } else {
        alert(response.data.message || '添加失败')
      }
    } catch (err) {
      console.error('添加到购物车错误:', err)
      if (err.response?.data?.error) {
        const { code, message } = err.response.data.error
        if (code === 'INSUFFICIENT_STOCK') {
          alert('库存不足')
        } else {
          alert(message || '添加失败，请稍后重试')
        }
      } else {
        alert('网络错误，请稍后重试')
      }
    } finally {
      setAddingToCart(prev => ({ ...prev, [product.id]: false }))
    }
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">商品列表</h1>
      
      {/* 搜索和筛选区域 */}
      <div className="mb-8">
        <form onSubmit={handleSearch} className="flex flex-wrap gap-4 mb-4">
          <div className="flex-1 min-w-64">
            <input
              type="text"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              placeholder="搜索商品名称..."
              className="form-input w-full"
            />
          </div>
          <button type="submit" className="btn-primary">
            搜索
          </button>
        </form>
        
        {/* 分类筛选 */}
        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => handleCategoryChange('')}
            className={`px-4 py-2 rounded ${
              selectedCategory === '' ? 'bg-primary text-white' : 'bg-gray-200 hover:bg-gray-300'
            }`}
          >
            全部分类
          </button>
          {categories.map(category => (
            <button
              key={category}
              onClick={() => handleCategoryChange(category)}
              className={`px-4 py-2 rounded ${
                selectedCategory === category ? 'bg-primary text-white' : 'bg-gray-200 hover:bg-gray-300'
              }`}
            >
              {category}
            </button>
          ))}
        </div>
      </div>

      {/* 错误信息 */}
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* 加载状态 */}
      {loading ? (
        <div className="text-center py-8">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          <p className="mt-2">加载中...</p>
        </div>
      ) : (
        <>
          {/* 商品网格 */}
          {products.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              {products.map(product => (
                <div key={product.id} className="card">
                  <img 
                    src={product.imageUrl || 'https://via.placeholder.com/300x300'} 
                    alt={product.name}
                    className="w-full h-48 object-cover rounded-lg mb-4"
                    onError={(e) => {
                      e.target.src = 'https://via.placeholder.com/300x300'
                    }}
                  />
                  <h3 className="text-lg font-semibold mb-2">
                    <Link 
                      to={`/products/${product.id}`}
                      className="hover:text-primary transition-colors"
                    >
                      {product.name}
                    </Link>
                  </h3>
                  <p className="text-gray-600 mb-3 line-clamp-2">{product.description}</p>
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-xl font-bold text-primary">
                      ¥{product.price}
                    </span>
                    <span className="text-sm text-gray-500">
                      库存: {product.stock}
                    </span>
                  </div>
                  <div className="flex gap-2">
                    <Link 
                      to={`/products/${product.id}`}
                      className="btn-secondary flex-1 text-center"
                    >
                      查看详情
                    </Link>
                    <button 
                      onClick={() => addToCart(product)}
                      disabled={product.stock === 0 || addingToCart[product.id]}
                      className="btn-primary flex-1 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {addingToCart[product.id] 
                        ? '添加中...' 
                        : product.stock === 0 
                        ? '缺货' 
                        : '加入购物车'
                      }
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <p className="text-gray-500">暂无商品</p>
            </div>
          )}

          {/* 分页 */}
          {totalPages > 1 && (
            <div className="flex justify-center gap-2">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="px-4 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                上一页
              </button>
              
              {[...Array(totalPages)].map((_, index) => (
                <button
                  key={index}
                  onClick={() => handlePageChange(index)}
                  className={`px-4 py-2 border rounded ${
                    currentPage === index 
                      ? 'bg-primary text-white' 
                      : 'hover:bg-gray-50'
                  }`}
                >
                  {index + 1}
                </button>
              ))}
              
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                className="px-4 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                下一页
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

export default Products