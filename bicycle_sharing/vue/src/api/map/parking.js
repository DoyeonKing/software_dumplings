import request from '@/utils/request'

// 模拟的停车点数据（使用边界框坐标）
const mockParkingAreas = [
    {
        id: 'P001',
        bounds: {
            southwest: [22.557678, 114.098511],
            northeast: [22.560425, 114.104004]  // min_lat + 0.002747, min_lon + 0.005493
        },
        status: 'normal',
        name: '停车区域1'
    },
    {
        id: 'P002',
        bounds: {
            southwest: [22.552185, 114.043579],
            northeast: [22.554932, 114.049072]  // min_lat + 0.002747, min_lon + 0.005493
        },
        status: 'normal',
        name: '停车区域2'
    },
    {
        id: 'P003',
        bounds: {
            southwest: [22.480774, 114.037745],
            northeast: [22.483521, 114.043238]  // min_lat + 0.002747, min_lon + 0.005493
        },
        status: 'normal',
        name: '停车区域3'
    },
    {
        id: 'P004',
        bounds: {
            southwest: [22.557678, 114.065552],
            northeast: [22.560425, 114.071045]  // min_lat + 0.002747, min_lon + 0.005493
        },
        status: 'normal',
        name: '停车区域4'
    },
    {
        id: 'P005',
        bounds: {
            southwest: [22.552185, 114.043579],
            northeast: [22.554932, 114.049072]  // min_lat + 0.002747, min_lon + 0.005493
        },
        status: 'normal',
        name: '停车区域5'
    }
];

// 获取所有停车点
export function getAllParkingAreas() {
    return Promise.resolve({
        code: 200,
        data: mockParkingAreas
    });
}

// 获取指定区域内的停车点
export function getParkingAreasInBounds(bounds) {
    // 实际项目中，这里应该根据传入的bounds筛选符合条件的停车区域
    return Promise.resolve({
        code: 200,
        data: mockParkingAreas
    });
}

// 获取单个停车点详情
export function getParkingAreaDetails(parkingId) {
    const parkingArea = mockParkingAreas.find(area => area.id === parkingId);
    if (parkingArea) {
        return Promise.resolve({
            code: 200,
            data: parkingArea
        });
    }
    return Promise.resolve({
        code: 404,
        data: null
    });
} 