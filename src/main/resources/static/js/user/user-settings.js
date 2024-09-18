$(document).ready(function() {
    $('#privacySelect').select2({
        theme: 'default',
        minimumResultsForSearch: Infinity,
    });

    $('#readStyle').select2({
        theme: 'default',
        minimumResultsForSearch: Infinity,
    });
    $('#pageStyle').select2({
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
document.addEventListener('DOMContentLoaded', function () {
    const tabs = document.querySelectorAll('#settingsTabs .nav-link');
    const tabPanes = document.querySelectorAll('.tab-pane');

    function activateTab(tabId) {
        tabs.forEach(tab => {
            if (tab.getAttribute('href') === '#' + tabId) {
                tab.classList.add('active');
            } else {
                tab.classList.remove('active');
            }
        });

        tabPanes.forEach(pane => {
            if (pane.id === tabId) {
                pane.classList.add('show', 'active');
            } else {
                pane.classList.remove('show', 'active');
            }
        });
    }

    function handleHashChange(e) {
        e.preventDefault();
        const hash = window.location.hash.substring(1);
        activateTab(hash);
    }

    const initialTab = window.location.hash.substring(1) || 'info';
    activateTab(initialTab);

    window.addEventListener('hashchange', handleHashChange);
});

function submitForm(action) {
    const form = document.getElementById('mainForm');
    const formData = new FormData(form);
    formData.append('action', action);
    fetch(form.action, {
        method: 'POST',
        body: formData
    })
        .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text);
            });
        }
        return response.text();
    })
        .then(data => {
        try {
            const jsonData = JSON.parse(data);
            handleJsonResponse(jsonData);
        } catch (e) {
            handleHtmlResponse(data);
        }
    })
        .catch(error => {
        let errorMessage = error.message;
        try {
            const errorJson = JSON.parse(error.message);
            if (errorJson && errorJson.message) {
                errorMessage = errorJson.message;
            }
        } catch (e) {
        }

        console.error('Error:', error);
        showMessage(errorMessage, 'error');
    });
}
function handleJsonResponse(data) {
    if (data.success) {
        showMessage(data.message, 'success');
    } else {
        showMessage(data.message, 'error');
    }
}
function handleHtmlResponse(html) {
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = html;
    const successMessage = tempDiv.querySelector('[th\\:if="${successMessageEmail}"]');
    const errorMessage = tempDiv.querySelector('[th\\:if="${errorMessagePasswordOrCode}"]');
    if (successMessage) {
        showMessage(successMessage.textContent, 'success');
    } else if (errorMessage) {
        showMessage(errorMessage.textContent, 'error');
    } else {
        document.getElementById('mainForm').innerHTML = tempDiv.querySelector('#mainForm').innerHTML;
    }
}
function showMessage(message, type) {
    const messageArea = document.getElementById('messageArea');
    messageArea.innerHTML = `<div class="${type === 'success' ? 'text-success' : 'text-error'}" role="alert">${message}</div>`;
}