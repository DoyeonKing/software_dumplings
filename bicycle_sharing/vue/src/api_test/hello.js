import request from '@/utils/request'

// 测试接口连接
export function testHello() {
  return request({
    url: '/hello',  // 移除重复的 /api 前缀
    method: 'get'
  })
}


