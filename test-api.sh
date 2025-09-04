#!/bin/bash

# API集成测试脚本
# 用于测试FullStackMall后端API接口

API_BASE_URL="http://localhost:8080/api"
CONTENT_TYPE="Content-Type: application/json"

echo "=== FullStackMall API 集成测试 ==="
echo "API Base URL: $API_BASE_URL"
echo

# 颜色输出函数
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# 测试函数
test_api() {
    local name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_status="$5"
    
    print_info "测试: $name"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$API_BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" -H "$CONTENT_TYPE" -d "$data" "$API_BASE_URL$endpoint")
    fi
    
    # 分离响应体和状态码
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -eq "$expected_status" ]; then
        print_success "$name - 状态码: $http_code"
        echo "响应: $response_body" | head -c 200
        echo
    else
        print_error "$name - 期望状态码: $expected_status, 实际: $http_code"
        echo "响应: $response_body"
    fi
    echo
}

# 1. 健康检查测试
echo "1. 健康检查测试"
test_api "系统健康检查" "GET" "/health" "" "200"
test_api "API版本信息" "GET" "/health/version" "" "200"

# 2. 用户认证测试
echo "2. 用户认证测试"

# 用户注册测试数据
register_data='{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
}'

test_api "用户注册" "POST" "/auth/register" "$register_data" "201"

# 用户登录测试数据
login_data='{
    "email": "test@example.com",
    "password": "password123"
}'

print_info "测试: 用户登录"
login_response=$(curl -s -w "\n%{http_code}" -X "POST" -H "$CONTENT_TYPE" -d "$login_data" "$API_BASE_URL/auth/login")
login_http_code=$(echo "$login_response" | tail -n1)
login_response_body=$(echo "$login_response" | head -n -1)

if [ "$login_http_code" -eq "200" ]; then
    print_success "用户登录 - 状态码: $login_http_code"
    # 提取token用于后续测试
    token=$(echo "$login_response_body" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$token" ]; then
        print_success "Token提取成功: ${token:0:20}..."
        AUTH_HEADER="Authorization: Bearer $token"
    else
        print_error "Token提取失败"
        AUTH_HEADER=""
    fi
else
    print_error "用户登录 - 期望状态码: 200, 实际: $login_http_code"
    AUTH_HEADER=""
fi
echo

# 3. 商品管理测试
echo "3. 商品管理测试"

test_api "获取商品列表" "GET" "/products" "" "200"
test_api "获取商品分类" "GET" "/products/categories" "" "200"
test_api "搜索商品" "GET" "/products/search?keyword=test" "" "200"

# 4. 购物车测试（需要认证）
echo "4. 购物车测试"

if [ -n "$AUTH_HEADER" ]; then
    print_info "测试: 获取购物车（需要认证）"
    cart_response=$(curl -s -w "\n%{http_code}" -H "$AUTH_HEADER" "$API_BASE_URL/cart")
    cart_http_code=$(echo "$cart_response" | tail -n1)
    cart_response_body=$(echo "$cart_response" | head -n -1)
    
    if [ "$cart_http_code" -eq "200" ]; then
        print_success "获取购物车 - 状态码: $cart_http_code"
    else
        print_error "获取购物车 - 期望状态码: 200, 实际: $cart_http_code"
    fi
    echo "响应: $cart_response_body" | head -c 200
    echo
else
    print_error "无法测试购物车 - 缺少认证Token"
fi

# 5. 订单测试（需要认证）
echo "5. 订单测试"

if [ -n "$AUTH_HEADER" ]; then
    print_info "测试: 获取我的订单（需要认证）"
    orders_response=$(curl -s -w "\n%{http_code}" -H "$AUTH_HEADER" "$API_BASE_URL/orders/my")
    orders_http_code=$(echo "$orders_response" | tail -n1)
    orders_response_body=$(echo "$orders_response" | head -n -1)
    
    if [ "$orders_http_code" -eq "200" ]; then
        print_success "获取我的订单 - 状态码: $orders_http_code"
    else
        print_error "获取我的订单 - 期望状态码: 200, 实际: $orders_http_code"
    fi
    echo "响应: $orders_response_body" | head -c 200
    echo
else
    print_error "无法测试订单 - 缺少认证Token"
fi

echo "=== API 集成测试完成 ==="