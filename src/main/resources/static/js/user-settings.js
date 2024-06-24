document.addEventListener('DOMContentLoaded', function () {
    const backgroundImagesContainer = document.getElementById('backgroundImagesContainer');
    let selectedImageInput = document.getElementById('selectedImageInput');
    let backGroundImgUser = document.getElementById('GetBackGroundImgUser').value;

    function selectImage(imageElement) {
        let previousSelectedImage = backgroundImagesContainer.querySelector('.selected');
        if (previousSelectedImage) {
            previousSelectedImage.classList.remove('selected');
        }

        imageElement.classList.add('selected');
        selectedImageInput.value = imageElement.src;
    }

    if (backGroundImgUser) {
        let images = backgroundImagesContainer.getElementsByTagName('img');
        for (let img of images) {
            if (img.src.includes(backGroundImgUser)) {
                selectImage(img);
                break;
            }
        }
    }

    window.selectImage = selectImage;
});