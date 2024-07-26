var loader = document.getElementById('loader');

document.addEventListener('DOMContentLoaded', function() {
    var backgroundImageUrl = '/images/settingsPicture/backGroundSettings1.jpg';

    var backGroundImgElement = document.getElementById('backGroundImg');
    if (backGroundImgElement && backGroundImgElement.value.trim() !== '') {
        backgroundImageUrl = backGroundImgElement.value;
    }

    var body = document.body;

    if (backgroundImageUrl && backgroundImageUrl.trim() !== '') {
        body.style.backgroundImage = 'url("' + backgroundImageUrl + '")';
    } else {
        body.style.backgroundImage = 'url("' + '/images/settingsPicture/backGroundSettings1.jpg' + '")';
    }

    body.style.backgroundSize = 'cover';
    body.style.backgroundRepeat = 'no-repeat';
    body.style.backgroundPosition = 'center center';
    body.style.position = 'relative';

    if (!backgroundImageUrl.includes('backGroundSettings1.jpg')) {
        var overlay = document.createElement('div');
        overlay.style.position = 'fixed';
        overlay.style.top = '0';
        overlay.style.left = '0';
        overlay.style.width = '100%';
        overlay.style.height = '100%';
        overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
        overlay.style.backdropFilter = 'blur(10px)';
        overlay.style.zIndex = '-1';
        body.appendChild(overlay);
    }

    loader.classList.add('hidden');
});
