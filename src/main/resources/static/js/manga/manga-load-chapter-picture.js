let fileList = [];

function previewMultipleImages(event) {
    const files = event.target.files;
    const previewContainer = document.getElementById('imagePreviewSection');
    previewContainer.style.display = 'flex';
    previewContainer.innerHTML = '';

    // Обновление списка файлов
    fileList = Array.from(files);

    fileList.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const imgWrapper = document.createElement('div');
            imgWrapper.className = 'image-wrapper';
            imgWrapper.dataset.index = index;

            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'preview-image';
            imgWrapper.appendChild(img);

            const moveUpBtn = document.createElement('button');
            moveUpBtn.className = 'move-up-btn';
            moveUpBtn.textContent = '⬆';
            moveUpBtn.onclick = function() {
                moveImage(imgWrapper, 'up');
                reorderFiles();
            };
            imgWrapper.appendChild(moveUpBtn);

            const moveDownBtn = document.createElement('button');
            moveDownBtn.className = 'move-down-btn';
            moveDownBtn.textContent = '⬇';
            moveDownBtn.onclick = function() {
                moveImage(imgWrapper, 'down');
                reorderFiles();
            };
            imgWrapper.appendChild(moveDownBtn);

            const deleteBtn = document.createElement('button');
            deleteBtn.className = 'delete-image-btn';
            deleteBtn.textContent = 'Удалить';
            deleteBtn.onclick = function() {
                imgWrapper.remove();
                reorderFiles();
            };
            imgWrapper.appendChild(deleteBtn);

            previewContainer.appendChild(imgWrapper);
        };
        reader.readAsDataURL(file);
    });
}

function moveImage(element, direction) {
    const container = element.parentElement;
    if (direction === 'up' && element.previousElementSibling) {
        container.insertBefore(element, element.previousElementSibling);
    } else if (direction === 'down' && element.nextElementSibling) {
        container.insertBefore(element.nextElementSibling, element);
    }
}

function reorderFiles() {
    const container = document.getElementById('imagePreviewSection');
    const newOrder = Array.from(container.children).map(child => parseInt(child.dataset.index, 10));

    const dt = new DataTransfer();

    newOrder.forEach(index => {
        dt.items.add(fileList[index]);
    });

    const input = document.getElementById('pagesImage');
    input.files = dt.files;
}
