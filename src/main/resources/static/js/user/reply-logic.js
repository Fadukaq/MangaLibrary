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
                        const replyElementMangaDetails = document.querySelector(`#reply-${replyIdToDelete}`);
                        if (replyElementMangaDetails) {
                            replyElementMangaDetails.style.display = 'none';
                            replyElementMangaDetails.remove();
                            modalDeleteReply.hide();
                        } else {
                            const replyElement = document.querySelector(`.reply-item input[name="reply-id"][value="${replyIdToDelete}"]`).closest('.reply-item');
                            if (replyElement) {
                                replyElement.remove();
                                modalDeleteReply.hide();
                            }
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
    const editReplyModalElement = document.getElementById('editReplyModal');
    const editReplyModal = new bootstrap.Modal(editReplyModalElement);
    const editReplyForm = document.getElementById('edit-reply-form');

    document.querySelectorAll('.edit-reply').forEach(button => {
        button.addEventListener('click', () => {
            const replyFormEdit = button.closest('form');
            const replyId = replyFormEdit.querySelector('#replyIdEdit').value;
            const replyText = replyFormEdit.querySelector('#replyTextEdit').value;
            const replyElement = document.querySelector(`.user-reply-text[data-reply-id="${replyId}"]`);
            if (replyElement) {
                const mention = replyElement.querySelector('.user-mention') ?
                replyElement.querySelector('.user-mention').textContent.trim() : "";
                const textAfterMention = replyElement.querySelector('.user-text') ?
                replyElement.querySelector('.user-text').textContent.trim() : "";
                const combinedText = `${mention} ${textAfterMention}`.trim();
                if(combinedText==''){
                    document.getElementById('edit-reply-text').value = replyText;
                }else{
                    document.getElementById('edit-reply-id').value = replyId;
                    document.getElementById('edit-reply-text').value = combinedText;
                }
                editReplyModal.show();
            }
            else{
                document.getElementById('edit-reply-id').value = replyId;
                document.getElementById('edit-reply-text').value = replyText;
            }
            editReplyModal.show();
        });
    });

    editReplyForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const replyId = document.getElementById('edit-reply-id').value;
        const replyText = document.getElementById('edit-reply-text').value.trim();
        const formReplyEdit = document.getElementById(`editReplyForm-${replyId}`);

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
            const replyElementMangaDetails = document.querySelector(`#reply-${replyId}`);
            if (replyElementMangaDetails) {
                const replyTextElement = replyElementMangaDetails.querySelector('.user-reply-text');
                if (replyTextElement) {
                    replyTextElement.textContent = replyText;
                }
            }else {
                const replyElement = document.querySelector(`.reply-item[data-reply-id="${replyId}"]`);
                if (replyElement) {
                    const replyTextElement = replyElement.querySelector('.reply-text');
                    if (replyTextElement) {
                        replyTextElement.textContent = replyText;
                    }
                }
            }
            editReplyModal.hide();
        })
            .catch(error => {
            console.error('Error:', error);
            editReplyModal.hide();
            $('#errorMessage').text('Виникла помилка. Спробуйте ще раз.');
            $('#errorModal').modal('show');
        });
    });
});
