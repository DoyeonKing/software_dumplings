

const AMapLoader = {
    load(ak = 'dea7cc14dad7340b0c4e541dfa3d27b7', plugins = '', version = '2.0') {
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
            const script = document.createElement('script');
            script.id = 'amap-script';
            script.type = 'text/javascript';
            script.src = `https://webapi.amap.com/maps?v=${version}&key=${ak}${plugins ? '&plugin=' + plugins : ''}`;
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