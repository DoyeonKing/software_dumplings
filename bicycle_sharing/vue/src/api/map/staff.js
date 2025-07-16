import request from '@/utils/request.js'

/**
 * 获取所有工作人员信息
 * @returns {Promise<Object>} 返回标准Result格式的工作人员列表
 */
export const getAllStaff = async () => {
  try {
    const response = await request({
      url: '/staff/workers',
      method: 'GET'
    })
    
    // 处理返回的数据，转换为标准Result格式
    if (Array.isArray(response)) {
      // 如果返回的是数组，转换为标准Result格式
      return {
        code: '200',
        msg: '获取工作人员信息成功',
        data: response
      }
    } else if (response && response.code) {
      // 如果已经是标准Result格式，直接返回
      return response
    } else {
      // 其他情况，包装为标准Result格式
      return {
        code: '200',
        msg: '获取工作人员信息成功',
        data: response || []
      }
    }
  } catch (error) {
    console.error('获取所有工作人员信息失败:', error)
    return {
      code: '500',
      msg: '获取工作人员信息失败',
      data: null
    }
  }
}
