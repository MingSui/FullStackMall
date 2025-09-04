const fs = require('fs');
const path = require('path');

// 简单的SVG图片生成器
function createProductSVG(name, color, width = 400, height = 400) {
    return `<?xml version="1.0" encoding="UTF-8"?>
<svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
  <rect width="100%" height="100%" fill="${color}"/>
  <rect x="50" y="${height / 2 - 40}" width="${width - 100}" height="80" 
        fill="white" stroke="black" stroke-width="2" rx="10"/>
  <text x="${width / 2}" y="${height / 2 + 8}" 
        font-family="Arial, sans-serif" font-size="24" font-weight="bold"
        text-anchor="middle" fill="black">${name}</text>
</svg>`;
}

// 商品数据
const products = [
    // 电子产品 - 蓝色系
    { filename: "iphone15pro", name: "iPhone 15 Pro", color: "#4682FF" },
    { filename: "macbook", name: "MacBook Air M3", color: "#1E64C8" },
    { filename: "airpods", name: "AirPods Pro", color: "#3278DC" },
    { filename: "ipad", name: "iPad Air", color: "#5A8CFF" },

    // 服装 - 绿色系
    { filename: "shirt", name: "经典白衬衫", color: "#64C896" },
    { filename: "jeans", name: "牛仔裤", color: "#50B478" },
    { filename: "shoes", name: "运动鞋", color: "#78DCAA" },
    { filename: "sweater", name: "针织毛衣", color: "#3CA064" },

    // 图书 - 橙色系
    { filename: "java-book", name: "Java编程思想", color: "#FF9650" },
    { filename: "spring-book", name: "Spring Boot实战", color: "#FF823C" },
    { filename: "algorithm-book", name: "算法导论", color: "#FFAA64" },
    { filename: "design-pattern-book", name: "设计模式", color: "#FF8C46" },

    // 家居用品 - 紫色系
    { filename: "lamp", name: "北欧风台灯", color: "#B478FF" },
    { filename: "sofa", name: "懒人沙发", color: "#A064E6" },
    { filename: "storage", name: "收纳盒套装", color: "#C88CFF" },
    { filename: "candle", name: "香薰蜡烛", color: "#AA6EF0" }
];

// 输出目录
const outputDir = path.join(__dirname, 'backend', 'src', 'main', 'resources', 'static', 'images', 'products');

// 确保目录存在
if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
}

// 生成SVG图片
products.forEach(product => {
    const svgContent = createProductSVG(product.name, product.color);
    const outputPath = path.join(outputDir, `${product.filename}.svg`);

    fs.writeFileSync(outputPath, svgContent, 'utf8');
    console.log(`Generated: ${outputPath}`);
});

console.log(`\n所有图片已生成到: ${outputDir}`);
console.log(`共生成了 ${products.length} 张图片`);