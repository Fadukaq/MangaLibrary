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
    body.style.backgroundPosition = 'center center fixed';

    loader.classList.add('hidden');
});
