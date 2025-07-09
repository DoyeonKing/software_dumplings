// 地图混入 - 包含地图初始化、标记点、热力图等功能
export const mapMixin = {
    data() {
        return {
            map: null,
            infoWindow: null,
            markers: [],
            heatmap: null,
            heatmapReady: false,
            showHeatmap: false
        }
    },
    methods: {
// 初始化地图
        initMap(center = [114.057868, 22.53445], zoom = 17) {
            this.map = new window.AMap.Map("mapContainer", {
                center: center,
                zoom: zoom,
                dragEnable: true
            });

            this.infoWindow = new window.AMap.InfoWindow({
                offset: new window.AMap.Pixel(0, -20)
            });

// 创建自行车图标
            const bikeSvg = `
<svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
<circle cx="16" cy="16" r="15" fill="#FFD600" stroke="#FFD600" stroke-width="2"/>
<g stroke="#222" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none">
  <circle cx="10" cy="22" r="4"/>
  <circle cx="22" cy="22" r="4"/>
  <path d="M10 22 L14 14 L18 14 L22 22"/>
  <path d="M14 14 L16 10 L18 14"/>
  <circle cx="16" cy="10" r="1"/>
</g>
</svg>
`;

            const yellowBikeIcon = new window.AMap.Icon({
                image: 'data:image/svg+xml;base64,' + btoa(bikeSvg),
                size: new window.AMap.Size(32, 32),
                imageSize: new window.AMap.Size(32, 32)
            });

// 加载热力图插件
            window.AMap.plugin(['AMap.Heatmap'], () => {
                this.heatmap = new window.AMap.Heatmap(this.map, {
                    radius: 35,
                    opacity: [0.1, 0.9],
                    gradient: {
                        0.2: 'blue',
                        0.4: 'green',
                        0.6: 'yellow',
                        0.8: 'orange',
                        1.0: 'red'
                    }
                });
                this.heatmapReady = true;
            });

            return { map: this.map, yellowBikeIcon };
        },

// 添加自行车标记点
        addBikeMarkers(bikeList, yellowBikeIcon) {
// 清除现有标记
            this.markers.forEach(marker => marker.setMap(null));
            this.markers = [];

            bikeList.forEach(bike => {
                const marker = new window.AMap.Marker({
                    position: [bike.lng, bike.lat],
                    map: this.map,
                    icon: yellowBikeIcon,
                    title: `单车编号: ${bike.id}`
                });

                marker.on('mouseover', () => {
                    this.infoWindow.setContent(`
<div style="min-width:160px;">
<b>单车编号：</b>${bike.id}<br/>
<b>状态：</b>${bike.status}<br/>
<b>位置：</b>${bike.address}
</div>`);
                    this.infoWindow.open(this.map, marker.getPosition());
                });
                marker.on('mouseout', () => this.infoWindow.close());

                this.markers.push(marker);
            });
        },

// 切换热力图
        toggleHeatmap(bikeList) {
            this.showHeatmap = !this.showHeatmap;

            if (this.showHeatmap) {
                this.markers.forEach(m => m.hide());
                const heatData = bikeList.map(bike => ({
                    lng: bike.lng,
                    lat: bike.lat,
                    count: 80
                }));
                if (this.heatmapReady && this.heatmap) {
                    this.heatmap.setDataSet({
                        data: heatData,
                        max: 100
                    });
                    this.heatmap.show();
                }
            } else {
                this.markers.forEach(m => m.show());
                if (this.heatmap) this.heatmap.hide();
            }
        },

// 清除地图
        clearMap() {
            if (this.markers.length > 0) {
                this.markers.forEach(marker => marker.setMap(null));
                this.markers = [];
            }
            if (this.heatmap) {
                this.heatmap.hide();
            }
        }
    }
}