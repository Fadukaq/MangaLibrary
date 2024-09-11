$(document).ready(function() {
    $('#privacySelect').select2({
        theme: 'default',
        minimumResultsForSearch: Infinity,
    });

    $('#readStyle').select2({
        theme: 'default',
        minimumResultsForSearch: Infinity,
    });
    $('.select2').on('select2:open', function() {
        var $container = $(this).parent();
        var containerWidth = $container.outerWidth();
        $('.select2-dropdown').css('width', containerWidth + 'px');
    });
});

function previewImage(event, previewId, uploadIconId) {
    const file = event.target.files[0];
    const reader = new FileReader();

    reader.onload = function() {
        const preview = document.getElementById(previewId);
        preview.src = reader.result;
        preview.style.display = 'block';
        const uploadIcon = document.getElementById(uploadIconId);
        uploadIcon.style.display = 'none';
    };

    if (file) {
        reader.readAsDataURL(file);
    }
}

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