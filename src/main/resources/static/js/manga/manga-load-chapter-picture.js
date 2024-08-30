let fileList = [];
let nextIndex = 1;

function previewMultipleImages(event) {
    const files = event.target.files;
    const previewContainer = document.getElementById('imagePreviewSection');
    previewContainer.style.display = 'flex';

    if (fileList.length > 0) {
        previewContainer.innerHTML = '';
        fileList = [];
    }

    const newFiles = Array.from(files).map(file => ({
        originalIndex: nextIndex++,
        file: file
    }));

    fileList = [...fileList, ...newFiles];

    fileList.sort((a, b) => a.originalIndex - b.originalIndex);

    previewContainer.innerHTML = '';

    const imagePromises = fileList.map(({originalIndex, file}) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.onload = function(e) {
                const imgWrapper = createImageWrapper(originalIndex, e.target.result, file);
                resolve(imgWrapper);
            };
            reader.readAsDataURL(file);
        });
    });

    Promise.all(imagePromises).then(imgWrappers => {
        imgWrappers.forEach(wrapper => {
            previewContainer.appendChild(wrapper);
        });
        updateDisplayOrder();
    });
}

function createImageWrapper(originalIndex, imgSrc, file) {
    const imgWrapper = document.createElement('div');
    imgWrapper.className = 'image-wrapper';
    imgWrapper.dataset.originalIndex = originalIndex;

    const img = document.createElement('img');
    img.src = imgSrc;
    img.className = 'preview-image';
    imgWrapper.appendChild(img);

    const buttonGroup = document.createElement('div');
    buttonGroup.className = 'button-group';

    const moveUpBtn = document.createElement('button');
    moveUpBtn.className = 'move-up-btn';
    moveUpBtn.textContent = '⭠';
    moveUpBtn.onclick = function() {
        event.preventDefault();
        moveImage(imgWrapper, 'up');
        updateDisplayOrder();
    };
    buttonGroup.appendChild(moveUpBtn);

    const moveDownBtn = document.createElement('button');
    moveDownBtn.className = 'move-down-btn';
    moveDownBtn.textContent = '⭢';
    moveDownBtn.onclick = function() {
        event.preventDefault();
        moveImage(imgWrapper, 'down');
        updateDisplayOrder();
    };
    buttonGroup.appendChild(moveDownBtn);

    imgWrapper.appendChild(buttonGroup);

    const indexSpan = document.createElement('span');
    indexSpan.className = 'image-index';
    imgWrapper.appendChild(indexSpan);

    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'delete-image-btn';
    deleteBtn.textContent = 'X';
    deleteBtn.onclick = function() {
        imgWrapper.remove();
        fileList = fileList.filter(item => item.originalIndex !== originalIndex);
        updateDisplayOrder();
    };
    imgWrapper.appendChild(deleteBtn);

    return imgWrapper;
}

function moveImage(element, direction) {
    const container = element.parentElement;
    if (direction === 'up' && element.previousElementSibling) {
        container.insertBefore(element, element.previousElementSibling);
    } else if (direction === 'down' && element.nextElementSibling) {
        container.insertBefore(element.nextElementSibling, element);
    }
}

function updateDisplayOrder() {
    const container = document.getElementById('imagePreviewSection');

    Array.from(container.children).forEach((child, idx) => {
        const displayIndex = idx + 1;
        child.querySelector('.image-index').textContent = displayIndex;
    });
    updateFileList();
}

function updateFileList() {
    const container = document.getElementById('imagePreviewSection');
    fileList = Array.from(container.children).map(child => {
        const originalIndex = parseInt(child.dataset.originalIndex);
        return fileList.find(item => item.originalIndex === originalIndex);
    });

    const dt = new DataTransfer();
    fileList.forEach(item => dt.items.add(item.file));
    document.getElementById('pagesImage').files = dt.files;
}