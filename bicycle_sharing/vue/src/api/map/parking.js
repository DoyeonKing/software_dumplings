import request from '@/utils/request'

// 停车点数据（使用边界框坐标）
const mockParkingAreas = [
    {
        id: 'ws1053',
        bounds: {
            southwest: [22.505493, 114.049072],  // south_lat, west_lon
            northeast: [22.510986, 114.060059]   // north_lat, east_lon
        },
        status: 'normal',
        name: '停车区域1'
    },
    {
        id: 'ws1056',
        bounds: {
            southwest: [22.510986, 114.049072],  // south_lat, west_lon
            northeast: [22.516479, 114.060059]   // north_lat, east_lon
        },
        status: 'normal',
        name: '停车区域2'
    },
    {
        id: 'ws106b',
        bounds: {
            southwest: [22.543945, 114.027100],  // south_lat, west_lon
            northeast: [22.549438, 114.038086]   // north_lat, east_lon
        },
        status: 'normal',
        name: '停车区域3'
    },
    {
        id: 'ws1052',
        bounds: {
            southwest: [22.500000, 114.049072],  // south_lat, west_lon
            northeast: [22.505493, 114.060059]   // north_lat, east_lon
        },
        status: 'normal',
        name: '停车区域4'
    },
    {
        id: 'ws109s',
        bounds: {
            southwest: [22.593384, 114.104004],  // south_lat, west_lon
            northeast: [22.598877, 114.114990]   // north_lat, east_lon
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