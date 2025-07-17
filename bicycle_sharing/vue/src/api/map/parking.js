import request from '@/utils/request'

// 停车点数据（使用边界框坐标）- 保留作为备用数据
const mockParkingAreas = [

];

// 获取所有停车点（保留用于兼容性）
export function getAllParkingAreas() {
    return Promise.resolve({
        code: 200,
        data: mockParkingAreas
    });
}

// 根据地图边界获取停车点数据
export function getParkingAreasInBounds(params) {
    return request({
        url: '/elitesites/parkingAreasByLatLng',
        method: 'get',
        params: {
            minLat: params.minLat,
            maxLat: params.maxLat,
            minLon: params.minLon,
            maxLon: params.maxLon
        }
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

// 获取所有停车区域编号（用于下拉选择）
export function getAllParkingAreaCodes() {
    return request({
        url: '/elitesites/allParkingAreas',
        method: 'get'
    });
}


// 数据格式转换函数：将后端返回的数据转换为前端需要的格式
export function convertParkingAreaData(backendData) {
    // 添加安全检查
    if (!backendData || !Array.isArray(backendData)) {
        console.error('convertParkingAreaData: 输入数据无效', backendData);
        return [];
    }

    return backendData.map(area => {
        // 验证必需字段
        if (!area || !area.geohash) {
            console.warn('convertParkingAreaData: 跳过无效的停车区域数据', area);
            return null;
        }

        return {
            id: area.geohash,
            // 使用后端返回的四个角点坐标构建边界框（用于兼容旧代码）
            bounds: {
                southwest: [area.bottomLeftLat, area.bottomLeftLon],  // 左下角
                northeast: [area.topRightLat, area.topRightLon]      // 右上角
            },
            // 中心点坐标
            center: [area.centerLat, area.centerLon],
            // 完整的四个角点坐标（用于绘制多边形）
            corners: {
                topLeft: [area.topLeftLat, area.topLeftLon],
                topRight: [area.topRightLat, area.topRightLon],
                bottomRight: [area.bottomRightLat, area.bottomRightLon],
                bottomLeft: [area.bottomLeftLat, area.bottomLeftLon]
            },
            // 构建多边形路径（按照顺时针或逆时针顺序）
            polygonPath: [
                [area.topLeftLon, area.topLeftLat],        // 左上角
                [area.topRightLon, area.topRightLat],      // 右上角
                [area.bottomRightLon, area.bottomRightLat], // 右下角
                [area.bottomLeftLon, area.bottomLeftLat],   // 左下角
                [area.topLeftLon, area.topLeftLat]         // 闭合多边形
            ],
            // 保留原始数据中的所有字段
            geohash: area.geohash,
            centerLat: area.centerLat,
            centerLon: area.centerLon,
            topLeftLat: area.topLeftLat,
            topLeftLon: area.topLeftLon,
            topRightLat: area.topRightLat,
            topRightLon: area.topRightLon,
            bottomRightLat: area.bottomRightLat,
            bottomRightLon: area.bottomRightLon,
            bottomLeftLat: area.bottomLeftLat,
            bottomLeftLon: area.bottomLeftLon,
            regionGroupId: area.regionGroupId,
            parkingCapacity: area.parkingCapacity,
            // 保留原始数据
            rawData: area
        };
    }).filter(area => area !== null); // 过滤掉无效数据
} 