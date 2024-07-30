document.addEventListener('DOMContentLoaded', () => {
    const selectElement = document.getElementById('listTypeSelect');
    const form = document.getElementById('addMangaForm');

    let selectedOption = selectElement.querySelector('option:checked');
    if (selectedOption) {
        selectedOption.hidden = true;
    }

    selectElement.addEventListener('change', function(event) {
        event.preventDefault();

        if (selectedOption) {
            selectedOption.hidden = false;
        }
        selectedOption = selectElement.querySelector('option:checked');
        if (selectedOption) {
            selectedOption.hidden = true;
        }

        const formData = new FormData(form);

        fetch(form.action, {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
            if (data.success) {
                showNotification(data.message);
            } else {
                showNotification(data.message, 'error');
            }
        })
            .catch(error => {
            console.error('Error:', error);
            showNotification('Сталася помилка під час додавання манги до списку', 'error');
        });
    });
});
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('addMangaFavoritesForm');
    const button = document.getElementById('addToFavorites');

    if (form && button) {
        // При загрузке страницы проверяем состояние кнопки
        const mangaId = form.querySelector('input[name="mangaId"]').value;

        // Обработчик нажатия на кнопку
        form.addEventListener('submit', function(event) {
            event.preventDefault(); // Предотвратить стандартное поведение формы

            const formData = new FormData(form);

            fetch(form.action, {
                method: 'POST',
                body: new URLSearchParams(formData)
            })
                .then(response => response.json())
                .then(data => {
                if (data.success) {
                    if (data.message.includes("додана")) {
                        button.classList.add("favorited");
                    } else if (data.message.includes("видалена")) {
                        button.classList.remove("favorited");
                    }
                    showNotification(data.message);
                } else {
                    showNotification(data.message, 'error');
                }
            })
                .catch(error => {
                console.error('Error:', error);
                showNotification('Сталася помилка під час обробки запиту', 'error');
            });
        });
    }
});
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show hide-delay fixed-right`;
    notification.role = 'alert';
    notification.innerHTML = `
            <span>${message}</span>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 3000);
}