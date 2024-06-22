var backgroundImageUrl = document.getElementById('backGroundImg').value || '/images/settingsPicture/backGroundSettings1.jpg';
var loader = document.getElementById('loader');
document.addEventListener('DOMContentLoaded', function() {
    var body = document.body;
    body.style.backgroundImage = 'linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url("' + backgroundImageUrl + '")';
    body.style.backgroundSize = 'cover';
    body.style.backgroundRepeat = 'no-repeat';
    body.style.backgroundPosition = 'center center fixed';

    var backgroundImage = new Image();
    backgroundImage.src = backgroundImageUrl;

    backgroundImage.onload = function() {
        loader.classList.add('hidden');
    };
});