import axios from "axios";
import {ElMessage} from "element-plus";

const request = axios.create({
    // *********** 核心修改点 ***********
    // 将 baseURL 指向 Vite 代理的前缀 '/api'
    // 这样所有请求都会发往 http://localhost:5173/api/... (假设您的前端服务在5173端口)
    // 然后被 Vite 的代理捕获并转发到 http://localhost:8080/...
    baseURL: '/api',
    timeout: 30000  // 后台接口超时时间
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    
    // 自动添加token到请求头
    const token = sessionStorage.getItem('authToken');
    if (token) {
        config.headers['Authorization'] = 'Bearer ' + token;
    }
    
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string' && res) {
            try {
                // 尝试解析JSON，如果失败就返回原字符串
                res = JSON.parse(res);
            } catch (e) {
                // 如果不是有效的JSON，就保持原字符串
                // 针对后端返回纯文本的情况，包装成统一格式
                res = {
                    code: 200,
                    msg: res,
                    data: null
                };
            }
        }
        return res;
    },
    error => {
        // 确保在访问 error.response 之前，它本身是存在的
        if (error.response) {
            if (error.response.status === 401) {
                ElMessage.error('登录已过期，请重新登录');
                // 清除token并跳转到登录页
                sessionStorage.removeItem('authToken');
                window.location.href = '/login';
            } else if (error.response.status === 403) {
                ElMessage.error('权限不足，无法访问');
            } else if (error.response.status === 404) {
                ElMessage.error('未找到请求接口')
            } else if (error.response.status === 500) {
                ElMessage.error('系统异常，请查看后端控制台报错')
            } else if (error.response.data && error.response.data.msg) {
                // 其他有具体业务错误信息的失败
                ElMessage.error(error.response.data.msg);
            } else {
                // 对于其他未知的HTTP错误
                ElMessage.error(`请求失败，HTTP状态码: ${error.response.status}`);
            }
        } else if (error.request) {
            // 请求已发出，但没有收到响应 (例如，网络错误，后端服务未运行)
            ElMessage.error('网络错误，请检查后端服务是否正在运行');
            console.error('Network Error:', error.message);
        } else {
            // 设置请求时发生了一些事情，触发了一个错误
            ElMessage.error('请求发送失败');
            console.error('Request Setup Error:', error.message);
        }

        return Promise.reject(error)
    }
)

//导出request
export default request