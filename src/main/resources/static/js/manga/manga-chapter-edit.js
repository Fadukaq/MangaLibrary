document.addEventListener('DOMContentLoaded', function() {
    const imageUrlsHidden = document.getElementById('imageUrlsHidden');
    imageUrlsHidden.value = chapterImageUrls.join(',');

    const previewContainer = document.getElementById('imagePreviewSection');
    previewContainer.style.display = 'flex';

    function updateFileOrder() {
        const fileOrderInput = document.getElementById('fileOrder');
        const imagePreviews = previewContainer.querySelectorAll('.image-wrapper');
        const orderArray = Array.from(imagePreviews).map(wrapper => wrapper.dataset.fileName);
        fileOrderInput.value = orderArray.join(',');
    }

    chapterImageUrls.forEach((url) => {
        const imgWrapper = document.createElement('div');
        imgWrapper.className = 'image-wrapper';
        imgWrapper.dataset.fileName = url.split('/').pop();

        const img = document.createElement('img');
        img.src = url;
        img.className = 'preview-image';
        imgWrapper.appendChild(img);

        const moveUpBtn = document.createElement('button');
        moveUpBtn.className = 'move-up-btn';
        moveUpBtn.textContent = '⬆';
        moveUpBtn.onclick = function() {
            moveImage(imgWrapper, 'up');
        };
        imgWrapper.appendChild(moveUpBtn);

        const moveDownBtn = document.createElement('button');
        moveDownBtn.className = 'move-down-btn';
        moveDownBtn.textContent = '⬇';
        moveDownBtn.onclick = function() {
            moveImage(imgWrapper, 'down');
        };
        imgWrapper.appendChild(moveDownBtn);

        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'delete-image-btn';
        deleteBtn.textContent = 'Удалить';
        deleteBtn.onclick = function() {
            imgWrapper.remove();
            updateFileOrder();
        };
        imgWrapper.appendChild(deleteBtn);

        previewContainer.appendChild(imgWrapper);
    });

    function moveImage(imgWrapper, direction) {
        const siblings = Array.from(previewContainer.children);
        const index = siblings.indexOf(imgWrapper);
        if (direction === 'up' && index > 0) {
            previewContainer.insertBefore(imgWrapper, siblings[index - 1]);
        } else if (direction === 'down' && index < siblings.length - 1) {
            previewContainer.insertBefore(imgWrapper, siblings[index + 2]);
        }

        updateFileOrder();
    }
});