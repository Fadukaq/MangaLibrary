//Comments
document.addEventListener('DOMContentLoaded', () => {
    const reportCommentForm = document.getElementById('reportCommentForm');
    const reportCommentModalElement = document.getElementById('reportCommentModal');
    const reportCommentModal = new bootstrap.Modal(reportCommentModalElement);
    const submitReportButton = document.getElementById('submitReport');

    document.querySelectorAll('.report-comment').forEach(button => {
        button.addEventListener('click', () => {
            const form = button.closest('form');
            const commentId = form.querySelector('#commentIdForm').value;
            const userId = form.querySelector('#userIdForm').value;
            document.getElementById('reportCommentForm').querySelector('input[name="commentId"]').value = commentId;
            document.getElementById('reportCommentForm').querySelector('input[name="userId"]').value = userId;
            reportCommentModal.show();
        });
    });

    submitReportButton.addEventListener('click', () => {
        const reason = document.getElementById('reportReasonComment').value;
        const maxLength = 255;

        if (!reason || reason.trim() === '') {
            reportCommentModal.hide();
            $('#errorMessage').text('Будь ласка, введіть причину скарги');
            $('#errorModal').modal('show');
            return;
        } else if (reason.length > maxLength) {
            reportCommentModal.hide();
            $('#errorMessage').text(`Причина занадто довга. Максимальна довжина: ${maxLength} символів.`);
            $('#errorModal').modal('show');
            document.getElementById('reportReasonComment').value = '';
            return;
        }
        document.getElementById('reportReasonCommentForm').value = reason;


        fetch(reportCommentForm.action, {
            method: 'POST',
            body: new FormData(reportCommentForm)
        })
            .then(response => response.text().then(text => {
            if (response.ok) {
                reportCommentModal.hide();
                $('#successMessage').text('Звіт успішно надіслано');
                $('#successModal').modal('show');
            } else if (response.status === 403) {
                reportCommentModal.hide();
                $('#errorMessage').text(text);
                $('#errorModal').modal('show');
            } else {
                throw new Error('Неочікуваний код стану: ' + response.status);
            }
        }))
            .catch(error => {
            reportCommentModal.hide();
            $('#errorMessage').text('Виникла помилка. Спробуйте ще раз.');
            $('#errorModal').modal('show');
        })
            .finally(() => {
            document.getElementById('reportReasonComment').value = '';
            reportCommentModal.hide();
        });
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const deleteButtons = document.querySelectorAll('.delete-comment');
    const modalCommentDelete = new bootstrap.Modal(document.getElementById('confirmDeleteModalComment'));
    let commentIdToDelete = null;

    deleteButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            commentIdToDelete = this.closest('form').querySelector('input[name="comment-id"]').value;
            modalCommentDelete.show();
        });
    });

    const confirmDeleteButton = document.getElementById('confirmDeleteCommentBtn');
    confirmDeleteButton.addEventListener('click', function() {
        if (commentIdToDelete) {
            fetch(`/comment/${commentIdToDelete}/delete`, {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                }
            })
                .then(response => {
                if (response.ok) {
                    const commentElement = document.querySelector(`.comment-item input[name="comment-id"][value="${commentIdToDelete}"]`).closest('.comment-item');
                    if (commentElement) {
                        console.log("Удаление элемента:", commentElement);
                        commentElement.remove();
                        modalCommentDelete.hide();
                    } else {
                        modalCommentDelete.hide();
                        $('#errorMessage').text(`Элемент с ID ${commentIdToDelete} не найден.`);
                        $('#errorModal').modal('show');
                    }
                } else {
                    alert('Помилка під час видалення коментаря.');
                }
            })
                .catch(error => {
                console.error('Помилка:', error);
            });
        }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const editCommentForm = document.getElementById('editCommentForm');
    const editCommentModalElement = document.getElementById('editCommentModal');
    const editCommentModal = new bootstrap.Modal(editCommentModalElement);
    const confirmEditCommentButton = document.getElementById('confirmEditCommentButton');

    document.querySelectorAll('.edit-comment').forEach(button => {
        button.addEventListener('click', () => {
            const formCommentEdit = button.closest('form');
            const commentIdEdit = formCommentEdit.querySelector('#commentIdEdit').value;
            const commentText = formCommentEdit.querySelector('#commentTextEdit').value;
            document.getElementById('edit-comment-id').value = commentIdEdit;
            document.getElementById('edit-comment-text').value = commentText;
            document.getElementById('editCommentForm').querySelector('input[name="commentIdEdit"]').value = commentIdEdit;
            document.getElementById('editCommentForm').querySelector('input[name="text"]').value = commentText;
            editCommentModal.show();
        });
    });

    confirmEditCommentButton.addEventListener('click', () => {
        const commentIdEdit = document.getElementById('edit-comment-id').value;
        const commentText = document.getElementById('edit-comment-text').value;

        if (!commentText.trim()) {
            editCommentModal.hide();
            $('#errorMessage').text('Будь ласка, введіть текст коментаря.');
            $('#errorModal').modal('show');
            return;
        }

        editCommentForm.querySelector('input[name="commentIdEdit"]').value = commentIdEdit;
        editCommentForm.querySelector('input[name="text"]').value = commentText;
        const formData = new FormData(editCommentForm);

        fetch(editCommentForm.action, {
            method: 'POST',
            body: new FormData(editCommentForm)
        })
            .then(response => response.text().then(text => {
            if (response.ok) {
                const commentTextElement = $(`#comments-list li.comment-item[data-comment-id="${commentIdEdit}"] .comment-text`);
                if (commentTextElement.length) {
                    const hiddenInput = document.getElementById('commentTextEdit');
                    hiddenInput.value = commentText;
                    commentTextElement.text(commentText);
                }
                editCommentModal.hide();
                $('#successMessage').text(text);
                $('#successModal').modal('show');
            } else {
                throw new Error('Ошибка: ' + response.status);
            }
        }))
            .catch(error => {
            editCommentModal.hide();
            $('#errorMessage').text('Виникла помилка. Спробуйте ще раз.');
            $('#errorModal').modal('show');
        });
    });
});
