import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { productAPI, cartAPI } from '../services/apiService'

const ProductDetail = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [addingToCart, setAddingToCart] = useState(false)

  useEffect(() => {
    fetchProduct()
  }, [id])

  const fetchProduct = async () => {
    try {
      setLoading(true)
      const response = await productAPI.getProductById(id)
      
      if (response.data.success) {
        setProduct(response.data.data)
      } else {
        setError(response.data.message || '获取商品详情失败')
      }
    } catch (err) {
      console.error('获取商品详情错误:', err)
      if (err.response?.status === 404) {
        setError('商品不存在')
      } else {
        setError('网络错误，请稍后重试')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleQuantityChange = (newQuantity) => {
    if (newQuantity >= 1 && newQuantity <= product.stock) {
      setQuantity(newQuantity)
    }
  }

  const addToCart = async () => {
    const token = localStorage.getItem('token')
    if (!token) {
      alert('请先登录')
      navigate('/login', { state: { from: location } })
      return
    }

    try {
      setAddingToCart(true)
      const response = await cartAPI.addToCart(product.id, quantity)
      
      if (response.data.success) {
        alert(`${product.name} (${quantity}件) 已添加到购物车`)
        setQuantity(1)
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
      setAddingToCart(false)
    }
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-8">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          <p className="mt-2">加载中...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-8">
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4 inline-block">
            {error}
          </div>
          <div>
            <Link to="/products" className="btn-primary">
              返回商品列表
            </Link>
          </div>
        </div>
      </div>
    )
  }

  if (!product) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-8">
          <p className="text-gray-500">商品不存在</p>
          <Link to="/products" className="btn-primary mt-4 inline-block">
            返回商品列表
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* 面包屑导航 */}
      <nav className="mb-8">
        <ol className="flex text-sm text-gray-500">
          <li><Link to="/" className="hover:text-primary">首页</Link></li>
          <li className="mx-2">/</li>
          <li><Link to="/products" className="hover:text-primary">商品列表</Link></li>
          <li className="mx-2">/</li>
          <li className="text-gray-900">{product.name}</li>
        </ol>
      </nav>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* 商品图片 */}
        <div>
          <img
            src={product.imageUrl || 'https://via.placeholder.com/600x600'}
            alt={product.name}
            className="w-full rounded-lg shadow-lg"
            onError={(e) => {
              e.target.src = 'https://via.placeholder.com/600x600'
            }}
          />
        </div>

        {/* 商品信息 */}
        <div>
          <h1 className="text-3xl font-bold mb-4">{product.name}</h1>
          
          <div className="mb-4">
            <span className="text-sm text-gray-500">分类: </span>
            <span className="text-primary font-medium">{product.category}</span>
          </div>

          <div className="mb-6">
            <span className="text-4xl font-bold text-primary">¥{product.price}</span>
          </div>

          <div className="mb-6">
            <h3 className="text-lg font-semibold mb-2">商品描述</h3>
            <p className="text-gray-700 leading-relaxed">
              {product.description || '暂无描述'}
            </p>
          </div>

          <div className="mb-6">
            <div className="flex items-center mb-2">
              <span className="text-sm text-gray-500 mr-2">库存:</span>
              <span className={`font-medium ${
                product.stock > 0 ? 'text-green-600' : 'text-red-600'
              }`}>
                {product.stock > 0 ? `${product.stock} 件` : '缺货'}
              </span>
            </div>
          </div>

          {product.stock > 0 && (
            <div className="mb-6">
              <label className="block text-sm font-medium mb-2">数量:</label>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleQuantityChange(quantity - 1)}
                  disabled={quantity <= 1}
                  className="w-8 h-8 border rounded flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  -
                </button>
                <input
                  type="number"
                  value={quantity}
                  onChange={(e) => handleQuantityChange(parseInt(e.target.value) || 1)}
                  min="1"
                  max={product.stock}
                  className="w-16 text-center border rounded py-1"
                />
                <button
                  onClick={() => handleQuantityChange(quantity + 1)}
                  disabled={quantity >= product.stock}
                  className="w-8 h-8 border rounded flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  +
                </button>
                <span className="text-sm text-gray-500 ml-2">
                  (最多 {product.stock} 件)
                </span>
              </div>
            </div>
          )}

          <div className="flex gap-4">
            {product.stock > 0 ? (
              <button
                onClick={addToCart}
                disabled={addingToCart}
                className="btn-primary flex-1 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {addingToCart ? '添加中...' : '加入购物车'}
              </button>
            ) : (
              <button
                disabled
                className="btn-primary flex-1 opacity-50 cursor-not-allowed"
              >
                暂时缺货
              </button>
            )}
            
            <Link
              to="/products"
              className="btn-secondary px-6"
            >
              继续购物
            </Link>
          </div>

          {/* 商品信息表格 */}
          <div className="mt-8">
            <h3 className="text-lg font-semibold mb-4">商品信息</h3>
            <div className="border rounded-lg overflow-hidden">
              <table className="w-full">
                <tbody>
                  <tr className="border-b">
                    <td className="px-4 py-3 bg-gray-50 font-medium">商品名称</td>
                    <td className="px-4 py-3">{product.name}</td>
                  </tr>
                  <tr className="border-b">
                    <td className="px-4 py-3 bg-gray-50 font-medium">商品分类</td>
                    <td className="px-4 py-3">{product.category}</td>
                  </tr>
                  <tr className="border-b">
                    <td className="px-4 py-3 bg-gray-50 font-medium">商品价格</td>
                    <td className="px-4 py-3 text-primary font-bold">¥{product.price}</td>
                  </tr>
                  <tr>
                    <td className="px-4 py-3 bg-gray-50 font-medium">库存状态</td>
                    <td className={`px-4 py-3 font-medium ${
                      product.stock > 0 ? 'text-green-600' : 'text-red-600'
                    }`}>
                      {product.stock > 0 ? '现货' : '缺货'}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ProductDetail