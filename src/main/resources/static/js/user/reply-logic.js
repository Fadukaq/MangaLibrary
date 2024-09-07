//Reply
document.addEventListener('DOMContentLoaded', function() {
    const deleteButtons = document.querySelectorAll('.delete-reply');
    let replyIdToDelete = null;
    let modalDeleteReply = null;

    deleteButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            const form = event.target.closest('form');
            if (form) {
                replyIdToDelete = form.querySelector('input[name="reply-id"]').value;
                modalDeleteReply = new bootstrap.Modal(document.getElementById('confirmDeleteModalReply'));
                modalDeleteReply.show();
            }
        });
    });

    const confirmDeleteButton = document.getElementById('confirmDeleteReplyButton');
    if (confirmDeleteButton) {
        confirmDeleteButton.addEventListener('click', function() {
            if (replyIdToDelete) {
                fetch(`/reply/${replyIdToDelete}/delete`, {
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value,
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: `reply-id=${replyIdToDelete}`
                })
                    .then(response => {
                    if (response.ok) {
                        const replyElement = document.querySelector(`.reply-item input[name="reply-id"][value="${replyIdToDelete}"]`).closest('.reply-item');
                        if (replyElement) {
                            replyElement.remove();
                            modalDeleteReply.hide();
                        } else {
                            $('#errorMessage').text(`Елемент з ID ${replyIdToDelete} не знайдений.`);
                            $('#errorMessage').modal('show');
                            console.error(`Елемент з ID ${replyIdToDelete} не знайдений.`);
                        }
                    } else {
                        modalDeleteReply.hide();
                        $('#errorMessage').text('Помилка під час видалення відповіді');
                        $('#errorMessage').modal('show');
                    }
                })
                    .catch(error => {
                    modalDeleteReply.hide();
                    $('#errorMessage').text('Помилка: '+ error);
                    $('#errorMessage').modal('show');
                    console.error('Помилка:', error);
                });
            }
        });
    }
});



document.addEventListener('DOMContentLoaded', () => {
    const reportReplyForm = document.getElementById('reportReplyForm');
    const reportReplyModalElement = document.getElementById('reportReplyModal');
    const reportReplyModal = new bootstrap.Modal(reportReplyModalElement);
    const confirmReportReplyButton = document.getElementById('confirmReportReplyButton');

    document.querySelectorAll('.report-reply').forEach(button => {
        button.addEventListener('click', () => {
            const form = button.closest('form');
            const replyId = form.querySelector('#replyIdFormReply').value;
            const userId = form.querySelector('#userIdFormReply').value;

            document.getElementById('reportReplyUserId').value = userId;
            document.getElementById('reportReplyId').value = replyId;

            reportReplyModal.show();
        });
    });

    confirmReportReplyButton.addEventListener('click', () => {
        const reason = document.getElementById('reportReplyReason').value;
        const maxLength = 255;

        if (!reason || reason.trim() === '') {
            reportReplyModal.hide();
            $('#errorMessage').text('Будь ласка, введіть причину скарги');
            $('#errorModal').modal('show');
            return;
        } else if (reason.length > maxLength) {
            reportReplyModal.hide();
            $('#errorMessage').text(`Причина занадто довга. Максимальна довжина: ${maxLength} символів.`);
            $('#errorModal').modal('show');
            document.getElementById('reportReplyReason').value = '';
            return;
        }

        document.getElementById('reportReasonReplyForm').value = reason;
        fetch(reportReplyForm.action, {
            method: 'POST',
            body: new FormData(reportReplyForm)
        })
            .then(response => response.text().then(text => {
            if (response.ok) {
                reportReplyModal.hide();
                $('#successMessage').text(text);
                $('#successModal').modal('show');
            } else if (response.status === 403) {
                reportReplyModal.hide();
                $('#errorMessage').text(text);
                $('#errorModal').modal('show');
            } else {
                throw new Error('Неочікуваний код стану: ' + response.status);
            }
        }))
            .catch(error => {
            reportReplyModal.hide();
            $('#errorMessage').text('Виникла помилка. Спробуйте ще раз.');
            $('#errorModal').modal('show');
        })
            .finally(() => {
            document.getElementById('reportReplyReason').value = '';
            reportReplyModal.hide();
        });
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const formReplyEdit = document.getElementById('editReplyForm');
    const editReplyModalElement = document.getElementById('editReplyModal');
    const editReplyModal = new bootstrap.Modal(editReplyModalElement);
    const editReplyForm = document.getElementById('edit-reply-form');

    document.querySelectorAll('.edit-reply').forEach(button => {
        button.addEventListener('click', () => {
            const replyFormEdit = button.closest('form');

            const replyId = replyFormEdit.querySelector('#replyIdEdit').value;
            const replyText = replyFormEdit.querySelector('#replyTextEdit').value;

            document.getElementById('edit-reply-id').value = replyId;
            document.getElementById('edit-reply-text').value = replyText;
            document.getElementById('editReplyForm').querySelector('input[name="replyIdEdit"]').value = replyId;
            document.getElementById('editReplyForm').querySelector('input[name="text"]').value = replyText;
            editReplyModal.show();
        });
    });
    editReplyForm.addEventListener('submit', (event) => {
        event.preventDefault();

        const replyId = document.getElementById('edit-reply-id').value;
        const replyText = document.getElementById('edit-reply-text').value.trim();

        if (!replyText) {
            editReplyModal.hide();
            $('#errorMessage').text('Будь ласка, введіть текст відповіді.');
            $('#errorModal').modal('show');
            return;
        }

        formReplyEdit.querySelector('input[name="replyIdEdit"]').value = replyId;
        formReplyEdit.querySelector('input[name="text"]').value = replyText;

        const formData = new FormData(formReplyEdit);

        fetch(formReplyEdit.action, {
            method: 'POST',
            body: new FormData(formReplyEdit)
        })
            .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Помилка при редагуванні відповіді');
            }
        })
            .then(data => {
            const replyTextElement = document.querySelector(`.reply-item[data-reply-id="${replyId}"] .reply-text`);
            if (replyTextElement) {
                const hiddenInput = document.getElementById('replyTextEdit');
                hiddenInput.value = replyText;
                replyTextElement.textContent = replyText;
            }
            editReplyModal.hide();
            $('#successMessage').text('Відповідь успішно відредагована');
            $('#successModal').modal('show');
        })
            .catch(error => {
            console.error('Error:', error);
            editReplyModal.hide();
            $('#errorMessage').text('Виникла помилка. Спробуйте ще раз.');
            $('#errorModal').modal('show');
        });
    });
});
