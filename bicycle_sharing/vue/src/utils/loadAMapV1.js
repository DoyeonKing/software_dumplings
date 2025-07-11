let amapScript = null;
let amapLoaded = false;

export function loadAMapV1(key) {
    return new Promise((resolve, reject) => {
        if (amapLoaded && window.AMapV1) {
            resolve(window.AMapV1);
            return;
        }
        if (amapScript) {
            amapScript.onload = () => resolve(window.AMapV1);
            amapScript.onerror = reject;
            return;
        }
        window.initAMapV1 = function() {
            window.AMapV1 = window.AMap;
            // 不要 delete window.AMap
            amapLoaded = true;
            resolve(window.AMapV1);
        };
        amapScript = document.createElement('script');
        amapScript.type = 'text/javascript';
        amapScript.src = `https://webapi.amap.com/maps?v=1.4.15&key=${key}&callback=initAMapV1`;
        amapScript.onerror = reject;
        document.head.appendChild(amapScript);
    });
}

export function unloadAMapV1() {
    if (amapScript && amapScript.parentNode) {
        amapScript.parentNode.removeChild(amapScript);
        amapScript = null;
        amapLoaded = false;
    }
    if (window.AMapV1) {
        delete window.AMapV1;
    }
    if (window.initAMapV1) {
        delete window.initAMapV1;
    }
    // 不要 delete window.AMap
}