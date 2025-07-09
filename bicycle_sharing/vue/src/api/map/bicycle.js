import request from '@/utils/request'

// 模拟的共享单车数据（固定的随机分布）
const mockBicycles = [
    { 
        id: 'B001',
        location: [114.085947, 22.547],
        status: 'available',
        battery: 85,
        lastUsed: '2024-03-20 14:30',
        totalMileage: 1250.5
    },
    { 
        id: 'B002',
        location: [114.109947, 22.542],
        status: 'available',
        battery: 92,
        lastUsed: '2024-03-20 12:15',
        totalMileage: 980.2
    },
    { 
        id: 'B003',
        location: [114.095947, 22.532],
        status: 'in_use',
        battery: 45,
        lastUsed: '2024-03-20 15:00',
        totalMileage: 1520.8
    },
    { 
        id: 'B004',
        location: [114.088947, 22.552],
        status: 'available',
        battery: 78,
        lastUsed: '2024-03-20 11:45',
        totalMileage: 2100.3
    },
    { 
        id: 'B005',
        location: [114.092947, 22.537],
        status: 'available',
        battery: 95,
        lastUsed: '2024-03-20 09:20',
        totalMileage: 750.6
    },
    { 
        id: 'B006',
        location: [114.102947, 22.547],
        status: 'maintenance',
        battery: 30,
        lastUsed: '2024-03-19 18:45',
        totalMileage: 3200.1
    },
    { 
        id: 'B007',
        location: [114.089947, 22.542],
        status: 'available',
        battery: 88,
        lastUsed: '2024-03-20 13:10',
        totalMileage: 1680.4
    },
    { 
        id: 'B008',
        location: [114.099947, 22.539],
        status: 'available',
        battery: 72,
        lastUsed: '2024-03-20 10:30',
        totalMileage: 2450.7
    },
    { 
        id: 'B009',
        location: [114.105947, 22.544],
        status: 'in_use',
        battery: 55,
        lastUsed: '2024-03-20 15:15',
        totalMileage: 1890.2
    },
    { 
        id: 'B010',
        location: [114.093947, 22.549],
        status: 'available',
        battery: 90,
        lastUsed: '2024-03-20 12:50',
        totalMileage: 1100.9
    },
    { 
        id: 'B011',
        location: [114.097947, 22.541],
        status: 'available',
        battery: 83,
        lastUsed: '2024-03-20 13:25',
        totalMileage: 1750.3
    },
    { 
        id: 'B012',
        location: [114.091947, 22.545],
        status: 'in_use',
        battery: 67,
        lastUsed: '2024-03-20 14:45',
        totalMileage: 2200.6
    },
    { 
        id: 'B013',
        location: [114.104947, 22.538],
        status: 'available',
        battery: 94,
        lastUsed: '2024-03-20 11:20',
        totalMileage: 890.4
    },
    { 
        id: 'B014',
        location: [114.087947, 22.543],
        status: 'maintenance',
        battery: 25,
        lastUsed: '2024-03-19 17:30',
        totalMileage: 2800.8
    },
    { 
        id: 'B015',
        location: [114.095947, 22.546],
        status: 'available',
        battery: 87,
        lastUsed: '2024-03-20 12:40',
        totalMileage: 1450.2
    }
];

// 获取所有单车
export function getAllBicycles() {
    return Promise.resolve({
        code: 200,
        data: mockBicycles
    });
}

// 获取指定区域内的单车
export function getBicyclesInArea(bounds) {
    return Promise.resolve({
        code: 200,
        data: mockBicycles
    });
}

// 获取单个单车详情
export function getBicycleDetails(bicycleId) {
    const bicycle = mockBicycles.find(bike => bike.id === bicycleId);
    return Promise.resolve({
        code: bicycle ? 200 : 404,
        data: bicycle || null
    });
}

// 更新单车状态（比如开始使用、结束使用等）
export function updateBicycleStatus(bicycleId, status) {
    return request({
        url: `/bicycles/${bicycleId}/status`,
        method: 'put',
        data: { status }
    })
} 