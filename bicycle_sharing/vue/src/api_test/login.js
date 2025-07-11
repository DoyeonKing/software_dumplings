import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/login',
    method: 'post',
    data: {
      username: data.username,
      password: data.password,
      role: data.role
    }
  })
}
