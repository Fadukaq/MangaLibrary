document.addEventListener('DOMContentLoaded', function() {
    const commentTimes = document.querySelectorAll('.comment-time');

    commentTimes.forEach(function(element) {
        const dateStr = element.getAttribute('data-date');
        const date = new Date(dateStr);

        const options = { day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' };
        const formattedDate = date.toLocaleDateString('uk-UA', options);

        element.textContent = formattedDate;
    });
});

$('#submit-comment').click(function(event) {
    event.preventDefault();
    const $button = $(this);
    const formData = $('#add-comment-form').serialize();
    const mangaId = $('#comments').data('manga-id');
    const currentUserId = $('#comments').data('current-user-id');

    $button.prop('disabled', true);

    $.ajax({
        type: 'POST',
        url: `/comment/${mangaId}/add-comment`,
        data: formData,
        success: function(comment) {
            location.reload();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.error('Error adding comment:', textStatus, errorThrown);
            alert('Произошла ошибка. Попробуйте снова.');
        },
        complete: function() {
            setTimeout(() => {
                $button.prop('disabled', false);
            }, 3000);
        }
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const commentElements = document.querySelectorAll('.rating-controls');

    commentElements.forEach(function(element) {
        const commentId = element.getAttribute('data-comment-id');
        const userRating = userRatings[commentId] || 0;
        const ratingInfo = commentRatings[commentId] || { upvotes: 0, downvotes: 0 };

        const upvoteButton = element.querySelector('.upvote-button');
        const downvoteButton = element.querySelector('.downvote-button');
        const ratingScore = element.querySelector('.rating-score');

        if (userRating === 1) {
            upvoteButton.classList.add('selected');
            downvoteButton.classList.remove('selected');
        } else if (userRating === -1) {
            downvoteButton.classList.add('selected');
            upvoteButton.classList.remove('selected');
        } else {
            upvoteButton.classList.remove('selected');
            downvoteButton.classList.remove('selected');
        }

        const ratingTitle = `Плюсів: ${ratingInfo.upvotes} | Мінусів: ${ratingInfo.downvotes}`;
        ratingScore.textContent = ratingInfo.upvotes - ratingInfo.downvotes;
        ratingScore.setAttribute('title', ratingTitle);
    });
});

function updateRatingDisplay(commentId, newRating, ratingTitle) {
    const ratingElement = $(`#rating-score-${commentId}`);
    ratingElement.text(newRating);
    ratingElement.attr('title', ratingTitle);
}

$(document).ready(function() {
    $('.rate-form').on('submit', function(event) {
        event.preventDefault();

        const form = $(this);
        const url = form.attr('action');
        const formData = form.serialize();

        $.ajax({
            url: url,
            method: 'POST',
            data: formData,
            success: function(response) {
                const commentId = form.find('input[name="commentId"]').val();
                const newRatingScore = response.newRatingScore;
                const ratingTitle = response.ratingTitle;
                updateRatingDisplay(commentId, newRatingScore, ratingTitle)

                if (form.hasClass('upvote-form')) {
                    form.find('.upvote-button').addClass('selected');
                    form.closest('.rating-controls').find('.downvote-button').removeClass('selected');
                } else {
                    form.find('.downvote-button').addClass('selected');
                    form.closest('.rating-controls').find('.upvote-button').removeClass('selected');
                }
            },
            error: function() {
                console.error("Ошибка при голосовании");
            }
        });
    });
});

$(document).ready(function() {
    $('.show-replies-button').each(function() {
        $(this).on('click', function() {
            const commentId = $(this).data('comment-id');
            const repliesContainer = $(this).siblings('.replies-container');
            repliesContainer.toggle();

            if (repliesContainer.is(':visible')) {
                $(this).text(`Приховати відповіді`);
            } else {
                const replyCount = repliesContainer.find('.reply').length;
                $(this).text(`Показати відповіді (${replyCount})`);
            }
        });
    });
});

$(document).ready(function() {
    $('.show-replies-button').each(function() {
        const commentId = $(this).data('comment-id');
        const repliesContainer = $(this).siblings('.replies-container');
        const replyCount = repliesContainer.find('.reply').length;
        if (replyCount > 0) {
            $(this).show().find('.replies-count').text(replyCount);
        }
    });
});

function linkifyUsernamesAndReplaceIds(text) {
    const userIdPattern = /@(\d+)/g;
    let match;

    while ((match = userIdPattern.exec(text)) !== null) {
        const userId = match[1];

        $.ajax({
            url: `/manga/getUsernameById`,
            type: 'GET',
            data: { userId: userId },
            async: false,
            success: function(response) {
                const username = response.username;
                text = text.replace(`@${userId}`, `<a href="/profile/${userId}" class="user-mention">@${username}</a>`);
            },
            error: function() {
                console.error(`не вдалося отримати нікнейм для ID ${userId}`);
            }
        });
    }
    return text;
}

$(document).on('click', '.reply-button', function() {
    const parentId = $(this).data('comment-id');
    const parentUserName = $(this).closest('.comment').find('.user-name-comment').text();
    const $formContainer = $(`#comment-${parentId}`).find('.reply-form-container');
    const $form = $formContainer.find('form');
    const $textarea = $form.find('textarea');

    $textarea.val('');
    $formContainer.show();

    $form.find('.username-block').show();
});

$('#comments-list').on('submit', '.reply-form', function(event) {
    event.preventDefault();

    const $form = $(this);
    const formData = $form.serializeArray();
    const parentId = $form.data('parent-id');
    const mangaId = $('#comments').data('manga-id');
    const userId = $form.data('user-id');

    const formDataObject = {};
    formData.forEach(item => {
        formDataObject[item.name] = item.value;
    });

    if (formDataObject.text) {
        formDataObject.text = `@${userId} ${formDataObject.text}`;
    }

    formDataObject.parentCommentId = parentId;
    formDataObject.mangaId = mangaId;

    $.ajax({
        type: 'POST',
        url: '/manga/comment/reply',
        data: formDataObject,
        success: function(response) {
            if (response.success) {
                const newReply = response.reply;
                const formattedDate = formatDate(newReply.createdAt);
                const replyText = linkifyUsernamesAndReplaceIds(newReply.text);
                console.log(replyText);
                const newReplyHtml = `
                    <div class="comment reply" id="comment-${newReply.id}">
                        <div class="reply-header">
                            <a href="/profile/${newReply.userId}" class="user-link">
                                <img src="${newReply.ProfilePicture}" class="user-icon" alt="${newReply.userName}'s icon">
                                <span class="user-name">${newReply.userName}</span>
                            </a>
                        </div>
                        <p class="user-reply-text">${replyText}</p>
                        <div class="reply-footer">
                            <small class="comment-time">${formattedDate}</small>
                        </div>
                    </div>
                `;
                $(`#comment-${parentId}`).append(newReplyHtml);

                $form.closest('.reply-form-container').hide();
                $form.closest('.reply-on-reply-form-container').hide();
                $form.find('textarea').val("");
            } else {
                alert('Помилка під час додавання відповіді');
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.error('Помилка під час додавання відповіді:', textStatus, errorThrown);
        }
    });

    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleString('uk-UA', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
});

$('#comments-list').on('click', '.cancel-reply', function() {
    $(this).closest('.reply-form-container').hide();
});

$(document).on('click', '.reply-on-reply-button', function() {
    const $reply = $(this).closest('.reply');
    const $formContainer = $reply.find('.reply-on-reply-form-container');
    const $form = $formContainer.find('form');

    const $textarea = $form.find('textarea');
    $textarea.val('');

    $formContainer.toggle();
});

$('#comments-list').on('click', '.cancel-reply-on-reply', function() {
    $(this).closest('.reply-on-reply-form-container').hide();
});

document.addEventListener("DOMContentLoaded", function() {
    const replyElement = document.querySelector('.user-reply-text');
    if(replyElement){
        const originalText = replyElement.getAttribute('data-text');
        if (originalText) {
            const updatedText = originalText;
            replyElement.innerHTML = updatedText;
        }
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const sortDropdown = document.querySelector('.dropdown-menu-sortBy');
    const commentsList = document.getElementById('comments-list');

    function sortComments(criteria) {
        event.preventDefault();
        const comments = Array.from(commentsList.querySelectorAll('.list-group-item-comments'));

        comments.sort((a, b) => {
            const dateA = new Date(a.querySelector('.comment-time').getAttribute('data-date'));
            const dateB = new Date(b.querySelector('.comment-time').getAttribute('data-date'));

            if (criteria === 'byNew') {
                return dateB - dateA;
            } else if (criteria === 'byRating') {
                const ratingA = parseInt(a.querySelector('.rating-score').textContent, 10);
                const ratingB = parseInt(b.querySelector('.rating-score').textContent, 10);
                return ratingB - ratingA;
            }
        });
        commentsList.innerHTML = '';
        comments.forEach(comment => commentsList.appendChild(comment));
    }

    function setActiveMenuItem(criteria) {
        const items = sortDropdown.querySelectorAll('.dropdown-item');
        items.forEach(item => {
            if (item.getAttribute('data-sort') === criteria) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });
    }

    sortDropdown.addEventListener('click', function(event) {
        if (event.target.classList.contains('dropdown-item')) {
            const sortBy = event.target.getAttribute('data-sort');
            const dropdownText = document.getElementById('dropdownMenuText');
            dropdownText.textContent = event.target.textContent;
            sortComments(sortBy);
            setActiveMenuItem(sortBy);
        }
    });
});