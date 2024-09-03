document.addEventListener('DOMContentLoaded', function() {
    const leftBlock = document.getElementById('leftBlock');
    const rightBlock = document.getElementById('rightBlock');
    const threshold = 950;
    const topOffset = 10;

    function updateLayout() {
        const scrollPosition = window.scrollY;

        if (scrollPosition > threshold) {
            leftBlock.style.width = '0';
            leftBlock.style.opacity = '0';
            rightBlock.style.marginLeft = '0';
            setTimeout(() => {
                rightBlock.classList.add('sticky');
            }, 100);
        } else {
            leftBlock.style.width = '210px';
            leftBlock.style.opacity = '1';
            rightBlock.style.marginLeft = '';
            setTimeout(() => {
                rightBlock.classList.remove('sticky');
            }, 100);
        }
    }
    function updateStarRatings() {
        const gradeDetails = document.querySelectorAll('.custom-grade-details');

        gradeDetails.forEach(detail => {
            const rating = parseFloat(detail.dataset.rating);
            const starsContainer = detail.querySelector('.stars');
            const ratingNumber = detail.querySelector('.rating-number');

            starsContainer.innerHTML = '';
            for (let i = 1; i <= 5; i++) {
                const star = document.createElement('i');
                star.className = i <= Math.floor(rating) ? 'fas fa-star'
                : (i - 0.8 < rating ? 'fas fa-star-half-alt' : 'far fa-star');
                starsContainer.appendChild(star);
            }
            ratingNumber.textContent = rating.toFixed(1);
        });
    }
    updateStarRatings();
    window.addEventListener('scroll', updateLayout);
    updateLayout();
});

document.addEventListener('DOMContentLoaded', () => {
    const reportCommentForm = document.getElementById('reportCommentForm');
    const reportCommentId = document.getElementById('reportCommentId');
    const reportUserId = document.getElementById('reportUserId');
    const submitReportButton = document.getElementById('submitReport');
    const successModalElement = document.getElementById('successModal');
    const errorModalElement = document.getElementById('errorModal');
    const successModal = new bootstrap.Modal(successModalElement);
    const errorModal = new bootstrap.Modal(errorModalElement);
    const reportCommentModalElement = document.getElementById('reportCommentModal');
    const reportCommentModal = new bootstrap.Modal(reportCommentModalElement);

    document.querySelectorAll('.report-comment').forEach(button => {
        button.addEventListener('click', (event) => {
            event.preventDefault();
            const commentId = button.getAttribute('data-comment-id');
            const userId = button.getAttribute('data-user-id');

            reportCommentId.value = commentId;
            reportUserId.value = userId;

            reportCommentModal.show();
        });
    });

    submitReportButton.addEventListener('click', () => {
        const commentId = reportCommentId.value;
        const userId = reportUserId.value;
        const reason = document.getElementById('reportReason').value;

        if (reason) {
            fetch(`/comment/${commentId}/report?userId=${userId}&reason=${encodeURIComponent(reason)}`, {
                method: 'GET'
            })
                .then(response => {
                if (response.status === 200) {
                    return response.text().then(text => {
                        document.getElementById('successMessage').textContent = text;
                        reportCommentModal.hide();
                        successModal.show();
                    });
                } else if (response.status === 403) {
                    return response.text().then(text => {
                        document.getElementById('errorMessage').textContent = text;
                        reportCommentModal.hide();
                        errorModal.show();
                    });
                } else {
                    throw new Error('Неочікуваний код стану: ' + response.status);
                }
            })
                .catch(error => {
                console.error('Error reporting comment:', error);
                document.getElementById('errorMessage').textContent = 'Виникла помилка. Спробуйте ще раз.';
                reportCommentModal.hide();
                errorModal.show();
            })
                .finally(() => {
                document.getElementById('reportReason').value = '';
            });;
        } else {
            alert('Будь ласка, введіть причину скарги.');
        }
    });
});

$(document).on('click', '.report-reply', function(e) {
    e.preventDefault();
    const replyId = $(this).data('reply-id');
    const userId = $(this).data('user-id');
    replyToReport = replyId;
    userToReport = userId;
    $('#reportReplyModal').modal('show');
});

$('#confirmReportReplyButton').click(function() {
    if (replyToReport) {
        const reason = $('#reportReplyReason').val();
        if (reason) {
            $.ajax({
                url: `/reply/${replyToReport}/report`,
                method: 'GET',
                data: { userId: userToReport, reason: reason },
                success: function(response) {
                    $('#reportReplyModal').modal('hide');
                    $('#successMessage').text('Ваше повідомлення про порушення надіслано.');
                    $('#successModal').modal('show');
                    $('#reportReplyReason').val('');

                },
                error: function(xhr, status, error) {
                    $('#reportReplyModal').modal('hide');
                    $('#errorMessage').text('Ви вже надіслали повідомлення про порушення цієї відповіді.');
                    $('#errorModal').modal('show');
                    $('#reportReplyReason').val('');
                }
            });
        } else {
            $('#reportReplyModal').modal('hide');
            $('#errorMessage').text('Будь ласка, введіть причину скарги.');
            $('#errorModal').modal('show');
        }
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const deleteButtons = document.querySelectorAll('.delete-comment');
    let commentIdToDelete = null;

    deleteButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            commentIdToDelete = this.closest('form').querySelector('input[name="comment-id"]').value;
            const modal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));
            modal.show();
        });
    });

    const confirmDeleteButton = document.getElementById('confirmDeleteButton');
    confirmDeleteButton.addEventListener('click', function() {
        if (commentIdToDelete) {
            fetch(`/manga/comment/${commentIdToDelete}/delete`, {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                }
            })
                .then(response => {
                if (response.ok) {
                    const commentElement = document.querySelector(`.comment-item input[name="comment-id"][value="${commentIdToDelete}"]`).closest('.comment-item');
                    if (commentElement) {
                        commentElement.remove();
                        const modal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmationModal'));
                        modal.hide();
                    } else {
                        console.error(`Элемент с ID ${commentIdToDelete} не найден.`);
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
document.addEventListener('DOMContentLoaded', function() {
    const deleteButtons = document.querySelectorAll('.delete-reply');
    let replyIdToDelete = null;
    let modal = null;

    deleteButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            const form = event.target.closest('form');
            if (form) {
                replyIdToDelete = form.querySelector('input[name="reply-id"]').value;
                modal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));
                modal.show();
            }
        });
    });

    const confirmDeleteButton = document.getElementById('confirmDeleteButton');
    if (confirmDeleteButton) {
        confirmDeleteButton.addEventListener('click', function() {
            if (replyIdToDelete) {
                fetch(`/manga/reply/${replyIdToDelete}/delete`, {
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
                            modal.hide();
                        } else {
                            console.error(`Елемент з ID ${replyIdToDelete} не знайдений.`);
                        }
                    } else {
                        alert('Помилка під час видалення відповіді.');
                    }
                })
                    .catch(error => {
                    console.error('Помилка:', error);
                });
            }
        });
    }
});
let currentSort = 'sortByDate';
let isAscending = true;
function formatDate(dateString) {
    const options = { day: 'numeric', month: 'short', year: 'numeric' };
    const date = new Date(dateString);
    return date.toLocaleDateString('uk-UA', options);
}

function sortComments(sortBy) {
    const commentsList = document.getElementById('comments-list');
    if (!commentsList) {
        console.error('Comments list not found');
        return;
    }
    const comments = Array.from(commentsList.children);

    if (sortBy === currentSort) {
        isAscending = !isAscending;
    } else {
        isAscending = false;
        currentSort = sortBy;
    }

    comments.sort((a, b) => {
        let comparison = 0;
        if (sortBy === 'sortByDate') {
            const dateA = new Date(a.dataset.date);
            const dateB = new Date(b.dataset.date);
            if (isNaN(dateA.getTime()) || isNaN(dateB.getTime())) {
                console.error('Invalid date data:', a.dataset.date, b.dataset.date);
                return 0;
            }
            comparison = dateB - dateA;
        } else if (sortBy === 'sortByRating') {
            const ratingA = parseInt(a.dataset.rating);
            const ratingB = parseInt(b.dataset.rating);
            if (isNaN(ratingA) || isNaN(ratingB)) {
                console.error('Invalid rating data:', a.dataset.rating, b.dataset.rating);
                return 0;
            }
            comparison = ratingB - ratingA;
        }
        return isAscending ? -comparison : comparison;
    });
    updateSortIcons();
}

function updateSortIcons() {
    const sortTabs = document.querySelectorAll('.sortBy .nav-link');
    sortTabs.forEach(tab => {
        const sortBy = tab.getAttribute('href').substring(1);
        const icon = tab.querySelector('i');
        if (icon) {
            if (sortBy === currentSort) {
                icon.style.display = 'inline-block';
                icon.classList.toggle('rotate-up', isAscending);
                icon.classList.toggle('rotate-down', !isAscending);
            } else {
                icon.style.display = 'none';
            }
        }
    });
}

$(document).ready(function() {
    $('.edit-comment').on('click', function(e) {
        e.preventDefault();
        const commentId = $(this).data('comment-id');
        const commentTextElement = $(`#comments-list li.comment-item[data-comment-id="${commentId}"] .comment-text`);

        if (commentTextElement.length) {
            const commentText = commentTextElement.text();
            console.log(commentText);
            $('#edit-comment-modal').modal('show');
            $('#edit-comment-text').val(commentText);
            $('#edit-comment-modal').data('comment-id', commentId);
        } else {
            console.error('Comment text element not found for comment ID:', commentId);
        }
    });

    $('#edit-comment-form').on('submit', function(e) {
        e.preventDefault();
        const commentId = $('#edit-comment-modal').data('comment-id');
        const commentText = $('#edit-comment-text').val();

        $.ajax({
            type: 'GET',
            url: `/manga/comments/${commentId}/edit`,
            data: { content: commentText },
            success: function(data) {
                const commentTextElement = $(`#comments-list li.comment-item[data-comment-id="${commentId}"] .comment-text`);
                if (commentTextElement.length) {
                    commentTextElement.text(commentText);
                } else {
                    console.error('Comment text element not found for comment ID:', commentId);
                }
                $('#edit-comment-modal').modal('hide');
            },
            error: function(xhr, status, error) {
                console.error(error);
            }
        });
    });
});
$(document).ready(function() {
    $('.edit-reply').on('click', function(e) {
        e.preventDefault();
        const replyId = $(this).data('reply-id');
        const replyTextElement = $(`#replies-list li.reply-item[data-reply-id="${replyId}"] .reply-text`);

        if (replyTextElement.length) {
            const replyText = replyTextElement.text();
            console.log(replyText);
            $('#edit-reply-modal').modal('show');
            $('#edit-reply-text').val(replyText);
            $('#edit-reply-modal').data('reply-id', replyId);
        } else {
            console.error('Reply text element not found for reply ID:', replyId);
        }
    });

    $('#edit-reply-form').on('submit', function(e) {
        e.preventDefault();
        const replyId = $('#edit-reply-modal').data('reply-id');
        const replyText = $('#edit-reply-text').val();

        $.ajax({
            type: 'GET',
            url: `/reply/${replyId}/edit`,
            data: { content: replyText },
            success: function(data) {
                const replyTextElement = $(`#replies-list li.reply-item[data-reply-id="${replyId}"] .reply-text`);
                if (replyTextElement.length) {
                    replyTextElement.text(replyText);
                } else {
                    console.error('Reply text element not found for reply ID:', replyId);
                }
                $('#edit-reply-modal').modal('hide');
            },
            error: function(xhr, status, error) {
                console.error(error);
            }
        });
    });
});
document.addEventListener('DOMContentLoaded', () => {
    const sortTabs = document.querySelectorAll('.sortBy .nav-link');
    sortTabs.forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            const sortBy = e.target.closest('.nav-link').getAttribute('href').substring(1);
            sortComments(sortBy);

            sortTabs.forEach(t => t.classList.remove('active'));
            e.target.closest('.nav-link').classList.add('active');
        });
    });
    const commentDates = document.querySelectorAll('.comment-date');
    commentDates.forEach(dateElement => {
        const dateString = dateElement.closest('.comment-item').dataset.date;
        if (dateString) {
            dateElement.textContent = formatDate(dateString);
        }
    });
    sortComments('sortByDate');
});

document.addEventListener('DOMContentLoaded', function() {
    const replyDates = document.querySelectorAll('.reply-date');
    replyDates.forEach(dateElement => {
        const dateString = dateElement.closest('.reply-item').dataset.date;
        if (dateString) {
            dateElement.textContent = formatDate(dateString);
        }
    });
});