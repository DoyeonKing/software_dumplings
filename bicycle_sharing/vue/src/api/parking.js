import request from '@/utils/request'
import geohash from 'ngeohash';

// GeoHash 解码函数（将 GeoHash 转换为经纬度范围）
function decodeGeoHash(hash) {
    console.log(`\n开始解码 GeoHash: ${hash}`);
    
    // 使用 ngeohash 库解码
    const [minlat, minlon, maxlat, maxlon] = geohash.decode_bbox(hash);
    
    const result = {
        southwest: [minlat, minlon],
        northeast: [maxlat, maxlon]
    };
    
    console.log('解码结果：');
    console.log(`经度范围: ${minlon} 到 ${maxlon}`);
    console.log(`纬度范围: ${minlat} 到 ${maxlat}`);
    
    console.log('返回的边界坐标：');
    console.log(`西南角 - 经度: ${minlon}, 纬度: ${minlat}`);
    console.log(`东北角 - 经度: ${maxlon}, 纬度: ${maxlat}\n`);
    
    return result;
}

// 模拟的停车点数据（使用 GeoHash）
const mockParkingAreas = [
    { id: 'P001', geohash: 'uxbpbz' },
    { id: 'P002', geohash: 'uxbpcr' },
    { id: 'P003', geohash: 'uxbpcp' },
    { id: 'P004', geohash: 'uxbpbz' },
    { id: 'P005', geohash: 'uxbpfp' },
    { id: 'P006', geohash: 'uxbpgr' },
    { id: 'P007', geohash: 'uxbpcr' },
    { id: 'P008', geohash: 'uxbpbx' }
];

// 获取所有停车点
export function getAllParkingAreas() {
    // 为每个停车区域添加边界坐标
    const areasWithBounds = mockParkingAreas.map(area => ({
        ...area,
        bounds: decodeGeoHash(area.geohash)
    }));

    return Promise.resolve({
        code: 200,
        data: areasWithBounds
    });
}

// 获取指定区域内的停车点
export function getParkingAreasInBounds(bounds) {
    // 实际项目中，这里应该根据传入的bounds筛选符合条件的停车区域
    const areasWithBounds = mockParkingAreas.map(area => ({
        ...area,
        bounds: decodeGeoHash(area.geohash)
    }));

    return Promise.resolve({
        code: 200,
        data: areasWithBounds
    });
}

// 获取单个停车点详情
export function getParkingAreaDetails(parkingId) {
    const parkingArea = mockParkingAreas.find(area => area.id === parkingId);
    if (parkingArea) {
        const areaWithBounds = {
            ...parkingArea,
            bounds: decodeGeoHash(parkingArea.geohash)
        };
        return Promise.resolve({
            code: 200,
            data: areaWithBounds
        });
    }
    return Promise.resolve({
        code: 404,
        data: null
    });
} 