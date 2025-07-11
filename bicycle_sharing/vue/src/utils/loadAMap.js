

const AMapLoader = {
    load(ak = '7a9ebfd8db9264a7f90b65369bd2970a', plugins = [], version = '2.0') {
        return new Promise((resolve, reject) => {
            if (window.AMap) {
                resolve(window.AMap);
                return;
            }
            // 防止多次加载
            if (document.getElementById('amap-script')) {
                // 如果已经有script标签但AMap还没挂载，监听onload
                document.getElementById('amap-script').addEventListener('load', () => {
                    resolve(window.AMap);
                });
                return;
            }

            // 确保plugins是数组并包含所需插件
            const pluginList = Array.isArray(plugins) ? plugins : [];
            if (!pluginList.includes('AMap.HeatMap')) {
                pluginList.push('AMap.HeatMap');
            }

            const script = document.createElement('script');
            script.id = 'amap-script';
            script.type = 'text/javascript';
            script.src = `https://webapi.amap.com/maps?v=${version}&key=${ak}${pluginList.length ? '&plugin=' + pluginList.join(',') : ''}`;
            script.onload = () => {
                if (window.AMap) {
                    resolve(window.AMap);
                } else {
                    reject(new Error('AMap SDK 加载失败'));
                }
            };
            script.onerror = reject;
            document.head.appendChild(script);
        });
    }
};

export default AMapLoader;