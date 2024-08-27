function previewMultipleImages(event, previewContainerId, uploadIconId) {
    const files = event.target.files;
    const previewContainer = document.getElementById('imagePreviewSection');
    const uploadIcon = document.getElementById(uploadIconId);
    previewContainer.style.display = 'flex';
    previewContainer.innerHTML = '';

    Array.from(files).forEach(file => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'preview-image';
            previewContainer.insertBefore(img, previewContainer.lastChild);
        }
        reader.readAsDataURL(file);
    });
}