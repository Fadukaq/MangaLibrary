document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('form[data-ajax="true"]').forEach(form => {
        form.addEventListener('submit', function(event) {
            event.preventDefault();

            const formData = new FormData(this);

            fetch(this.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams(formData).toString()
            })
                .then(response => response.json())
                .then(data => {
                if (data.success) {
                    showSuccessModal(data.message);
                    this.closest('.list-group-item').remove();
                } else {
                    showErrorModal(data.message);
                }
            })
                .catch(error => {
                console.error('Помилка:', error);
                showErrorModal(error.message);
            });
        });
    });
});
function handleFormSubmit(event) {
    event.preventDefault();

    const form = event.target;
    const formData = new FormData(form);

    fetch(form.action, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams(formData).toString()
    })
        .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
        .then(data => {
        if (data.success) {
            showSuccessModal("Друг видалено");
            const listItem = form.closest('.list-group-item');
            listItem.remove();
        } else {
            showErrorModal(data.message);
        }
    })
        .catch(error => {
        console.error('Помилка:', error);
        showErrorModal(error.message);
    });

    return false;
}
function handleResponse(button, requestId, response) {
    const form = button.closest('form');
    const formData = new FormData(form);
    formData.append('response', response);

    fetch(form.action, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams(formData).toString()
    })
        .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
        .then(data => {
        if (data.success) {
            showSuccessModal('Запит на дружбу ' + (response === 'accept' ? 'прийнятий' : 'відхилений'));
            const listItem = form.closest('.list-group-item');
            listItem.remove();
        } else {
            showErrorModal(data.message);
        }
    })
        .catch(error => {
        console.error('Помилка:', error);
        showErrorModal(error.message);
    });
}
function showSuccessModal(message) {
    const successModal = document.querySelector('#successModal');
    const modalBody = successModal.querySelector('.modal-body');
    modalBody.textContent = message;
    const modal = new bootstrap.Modal(successModal);
    modal.show();
}

function showErrorModal(message) {
    const errorModal = document.querySelector('#errorModal');
    const modalBody = errorModal.querySelector('.modal-body');
    modalBody.textContent = message;
    const modal = new bootstrap.Modal(errorModal);
    modal.show();
}
function sendFriendRequest(event) {
    event.preventDefault();

    const form = event.target.closest('form');
    const formData = new FormData(form);

    fetch(form.action, {
        method: 'POST',
        body: formData,
        headers: {
            'Accept': 'application/json',
        }
    })
        .then(response => response.json())
        .then(data => {
        if (data.success) {
            showSuccessModal(data.message);
        } else {
            showErrorModal('Помилка при відправці запиту на дружбу: ' + data.message);
        }
    })
        .catch(error => {
        console.error('Помилка:', error);
        showErrorModal('Виникла помилка при відправці запиту: ' + error.message);
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const friendRequestForms = document.querySelectorAll('form[action="/friends/sendFriendRequest"]');
    friendRequestForms.forEach(form => {
        form.addEventListener('submit', sendFriendRequest);
    });
});