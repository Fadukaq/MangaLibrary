document.addEventListener('DOMContentLoaded', () => {
    const forms = [
        { formId: 'addMangaForm', selectId: 'listTypeSelect' },
        { formId: 'addMangaForm-mobile', selectId: 'listTypeSelect-mobile' }
    ];

    forms.forEach(({ formId, selectId }) => {
        const selectElement = document.getElementById(selectId);
        const form = document.getElementById(formId);

        let selectedOption = selectElement.querySelector('option:checked');
        if (selectedOption) {
            selectedOption.hidden = true;
        }

        selectElement.addEventListener('change', function(event) {
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
});

document.addEventListener('DOMContentLoaded', () => {
    const forms = document.querySelectorAll('#addMangaFavoritesForm, #addMangaFavoritesForm-mobile');
    const buttons = document.querySelectorAll('#addToFavorites, #addToFavorites-mobile');

    forms.forEach((form, index) => {
        const button = buttons[index];
        const mangaId = form.querySelector('input[name="mangaId"]').value;

        form.addEventListener('submit', function(event) {
            event.preventDefault();

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

                    buttons.forEach(btn => {
                        if (data.message.includes("додана")) {
                            btn.classList.add("favorited");
                        } else if (data.message.includes("видалена")) {
                            btn.classList.remove("favorited");
                        }
                    });
                } else {
                    showNotification(data.message, 'error');
                }
            })
                .catch(error => {
                console.error('Error:', error);
                showNotification('Сталася помилка під час обробки запиту', 'error');
            });
        });
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const stars = document.querySelectorAll('.rating-stars .fa-star');
    const ratingPanel = document.getElementById('ratingPanel');
    const overlay = document.getElementById('overlay');
    const openRatingBtn = document.querySelector('.open-rating-btn');
    const closePanelBtn = document.querySelector('.close-panel');
    const ratingForm = document.getElementById('ratingForm');
    const ratingInput = document.getElementById('ratingInput');
    const ratingValue = document.getElementById('ratingValue');
    const userRatingDiv = document.getElementById('userRating');
    const removeRatingButton = document.getElementById('removeRatingButton');
    const removeRatingForm = document.getElementById('removeRatingForm');
    const removeRatingDiv = document.getElementById('removeRatingContainer');
    let selectedRating = 0;

    function updateStars(rating) {
        stars.forEach(star => {
            const value = parseFloat(star.getAttribute('data-value'));
            if (value <= rating) {
                star.classList.add('checked');
            } else {
                star.classList.remove('checked');
            }
        });
    }

    function updateRatingValue() {
        ratingValue.textContent = "Ваша оцінка: "+ selectedRating;
    }

    function saveRating() {
        const formData = new FormData(ratingForm);

        fetch(ratingForm.getAttribute('action'), {
            method: 'POST',
            body: formData
        })
            .then(response => {
            if (!response.ok) {
                throw new Error('Реакція мережі була незадовільною');
            }
            return response.json();
        })
            .then(data => {
            if (data.success) {
                showNotification(data.message, 'success');
            } else {
                showNotification(data.message, 'danger');
            }
        })
            .catch(error => {
            console.error('помилка:', error);
            showNotification('Сталася помилка під час збереження оцінки', 'danger');
        });
    }

    function removeRating() {
        const formData = new FormData(removeRatingForm);

        fetch(removeRatingForm.getAttribute('action'), {
            method: 'POST',
            body: formData
        })
            .then(response => {
            if (!response.ok) {
                throw new Error('Реакція мережі була незадовільною');
            }
            return response.json();
        })
            .then(data => {
            if (data.success) {
                showNotification(data.message, 'success');
                ratingValue.textContent = '';
                const removeRatingContainer = document.getElementById('removeRatingContainer');
                if (removeRatingContainer) {
                    removeRatingContainer.style.display = 'none';
                }
                resetStars();

                ratingPanel.classList.remove('active');
                overlay.classList.remove('active');
            } else {
                showNotification(data.message, 'danger');
            }
        })
            .catch(error => {
            console.error('помилка:', error);
            showNotification('Сталася помилка під час видалення оцінки', 'danger');
        });
    }

    if (document.getElementById('removeRatingButton')) {
        document.getElementById('removeRatingButton').addEventListener('click', (e) => {
            e.preventDefault();
            removeRating();
        });
    }

    if (ratingValue != null) {
        selectedRating = parseFloat(ratingValue.textContent) || 0;
        updateStars(selectedRating);
        if(selectedRating!=0){
            updateRatingValue();
        }
    }

    stars.forEach(star => {
        star.addEventListener('mouseover', () => {
            const rating = parseFloat(star.getAttribute('data-value'));
            updateStars(rating);
        });

        star.addEventListener('mouseout', () => {
            updateStars(selectedRating);
        });

        star.addEventListener('click', () => {
            selectedRating = parseFloat(star.getAttribute('data-value'));
            updateStars(selectedRating);
            ratingInput.value = selectedRating;
            updateRatingValue();
            saveRating();

            const removeRatingContainer = document.getElementById('removeRatingContainer');
            if (removeRatingContainer) {
                removeRatingContainer.style.display = 'block';
            } else {
                const container = document.createElement('div');
                container.id = 'removeRatingContainer';

                const button = document.createElement('button');
                button.type = 'submit';
                button.id = 'removeRatingButton';
                button.className = 'remove-rating-button';
                button.textContent = 'Видалити оцінку';

                button.addEventListener('click', (e) => {
                    e.preventDefault();
                    removeRating();
                });

                container.appendChild(button);
                document.getElementById('ratingPanel').appendChild(container);
            }

        });
    });
    function resetStars() {
        stars.forEach(star => {
            star.classList.remove('checked');
        });
    }
    openRatingBtn.addEventListener('click', () => {
        ratingPanel.classList.add('active');
        overlay.classList.add('active');
    });

    closePanelBtn.addEventListener('click', () => {
        ratingPanel.classList.remove('active');
        overlay.classList.remove('active');
    });

    overlay.addEventListener('click', () => {
        ratingPanel.classList.remove('active');
        overlay.classList.remove('active');
    });
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