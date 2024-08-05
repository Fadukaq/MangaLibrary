$(document).ready(function() {
    loadComments();

    function formatDate(dateString) {
        const options = { day: 'numeric', month: 'short', year: 'numeric' };
        const date = new Date(dateString);
        return date.toLocaleDateString('uk-UA', options);
    }

    $('#submit-comment').click(function(event) {
        event.preventDefault();
        const $button = $(this);
        const formData = $('#add-comment-form').serialize();
        const mangaId = $('#comments').data('manga-id');
        const currentUserId = $('#comments').data('current-user-id');

        $button.prop('disabled', true);

        $.ajax({
            type: 'POST',
            url: `/manga/${mangaId}/add-comment`,
            data: formData,
            success: function(comment) {
                const userName = comment.userName || 'Unknown User';
                const userIcon = comment.ProfilePicture || '<i class="fa-solid fa-user-circle"></i>';
                const formattedDate = formatDate(comment.createdAt);

                const commentElement = $('<div>').addClass('comment').attr('id', `comment-${comment.id}`).html(`
                    <div class="comment-header">
                        <a href="/profile/${comment.userName}" class="user-link">
                            <img src="${userIcon}" class="user-icon" alt="${userName}'s icon">
                            <span class="user-name">${userName}</span>
                        </a>
                        <div class="dropdown comment-options">
                            <button class="btn btn-link dropdown-toggle" type="button" id="dropdownMenuButton-${comment.id}" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="fas fa-ellipsis-v"></i>
                            </button>
                            <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton-${comment.id}">
                                ${comment.userId !== currentUserId ? `
                                    <li><a class="dropdown-item report-comment" href="#" data-comment-id="${comment.id}"><i class="fas fa-flag"></i>Поскаржитися</a></li>
                                ` : ''}
                                ${comment.userId === currentUserId ? `
                                    <form action="/manga/comment/${comment.id}/edit" method="post">
                                        <input type="hidden" name="comment-id" value="${comment.id}">
                                        <button class="dropdown-item edit-comment" type="submit"><i class="fas fa-edit"></i>Редагувати</button>
                                    </form>
                                    <form action="/manga/comment/${comment.id}/delete" method="post">
                                        <input type="hidden" name="comment-id" value="${comment.id}">
                                        <button class="dropdown-item delete-comment" type="submit"><i class="fas fa-trash-alt"></i>Видалити</button>
                                    </form>
                                ` : ''}
                            </ul>
                        </div>
                    </div>
                    <p class="user-comment-text">${comment.text}</p>
                    <textarea class="edit-comment-text form-control mb-3" style="display:none; width:100%;">${comment.text}</textarea>
                    <button class="btn btn-primary edit-comment-save" style="display:none;">Сохранить</button>
                    <button class="btn btn-primary edit-comment-cancel" style="display:none;">Отменить</button>
                    <div class="comment-footer">
                        <small class="comment-time">${formattedDate}</small>
                        <i style="font-size:8px; color:gray;" class="fa-solid fa-circle"></i>
                        <button class="btn btn-link reply-button" data-comment-id="${comment.id}">Відповісти</button>
                        <div class="rating-controls">
                            <button class="btn btn-link upvote-button" data-comment-id="${comment.id}">
                                <i class="fa-solid fa-arrow-up"></i>
                            </button>
                            <span class="rating-score" id="rating-score-${comment.id}">${comment.rating || 0}</span>
                            <button class="btn btn-link downvote-button" data-comment-id="${comment.id}">
                                <i class="fa-solid fa-arrow-down"></i>
                            </button>
                        </div>
                    </div>
                    <hr>
                `);

                $('#comments-list').prepend(commentElement);
                $('#comment-text').val('');
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

    function loadComments() {
        const mangaId = $('#comments').data('manga-id');
        const currentUserId = $('#comments').data('current-user-id');

        $.ajax({
            url: `/manga/${mangaId}/comments`,
            method: 'GET',
            success: function(response) {
                const comments = response.comments;
                const userRatings = response.userRatings;
                const commentRatings = response.commentRatings;

                $('#comments-list').empty();
                comments.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
                comments.forEach(comment => {
                    const userName = comment.userName || 'Unknown User';
                    const userIcon = comment.ProfilePicture || '<i class="fa-solid fa-user-circle"></i>';
                    const formattedDate = formatDate(comment.createdAt);

                    const userRating = userRatings[comment.id] || 0;
                    const upvoteSelected = userRating === 1 ? 'selected' : '';
                    const downvoteSelected = userRating === -1 ? 'selected' : '';

                    const ratingInfo = commentRatings[comment.id] || { upvotes: 0, downvotes: 0 };
                    const ratingTitle = `Плюсів: ${ratingInfo.upvotes} | Мінусів: ${ratingInfo.downvotes}`;


                    const commentElement = $('<div>').addClass('comment').attr('id', `comment-${comment.id}`);

                    let optionsMenu = `
                    <div class="dropdown comment-options">
                        <button class="btn btn-link dropdown-toggle" type="button" id="dropdownMenuButton-${comment.id}" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-ellipsis-v"></i>
                        </button>
                        <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton-${comment.id}">
                    `;
                    if (comment.userId !== currentUserId) {
                        optionsMenu += `
                            <li><a class="dropdown-item report-comment" href="#" data-comment-id="${comment.id}"><i class="fas fa-flag"></i>Поскаржитися</a></li>
                        `;
                    }
                    if (comment.userId === currentUserId) {
                        optionsMenu += `
                        <form action="/manga/comment/${comment.id}/edit" method="post">
                            <input type="hidden" name="comment-id" value="${comment.id}">
                            <button class="dropdown-item edit-comment" type="submit"><i class="fas fa-edit"></i>Редагувати</button>
                        </form>
                        <form action="/manga/comment/${comment.id}/delete" method="post">
                            <input type="hidden" name="comment-id" value="${comment.id}">
                            <button class="dropdown-item delete-comment" type="submit"><i class="fas fa-trash-alt"></i>Видалити</button>
                        </form>
                        `;
                    }
                    optionsMenu += `</ul></div>`;

                    commentElement.html(`
                    <div class="comment-header">
                        <a href="/profile/${comment.userName}" class="user-link">
                            <img src="${userIcon}" class="user-icon" alt="${userName}'s icon">
                            <span class="user-name">${userName}</span>
                        </a>
                        ${optionsMenu}
                    </div>
                    <p class="user-comment-text">${comment.text}</p>
                    <textarea class="edit-comment-text form-control mb-3" style="display:none; width:100%;">${comment.text}</textarea>
                    <button class="btn btn-primary edit-comment-save" style="display:none;">Сохранить</button>
                    <button class="btn btn-primary edit-comment-cancel" style="display:none;">Отменить</button>
                    <div class="comment-footer">
                        <small class="comment-time">${formattedDate}</small>
                        <i style="font-size:8px; color:gray;" class="fa-solid fa-circle"></i>
                        <button class="btn btn-link reply-button" data-comment-id="${comment.id}">Відповісти</button>
                        <div class="rating-controls" data-user-rating="${userRating}">
                        <button class="btn btn-link upvote-button ${upvoteSelected}" data-comment-id="${comment.id}">
                            <i class="fa-solid fa-arrow-up"></i>
                        </button>
                            <span class="rating-score" id="rating-score-${comment.id}" title="${ratingTitle}">${comment.rating || 0}</span>
                        <button class="btn btn-link downvote-button ${downvoteSelected}" data-comment-id="${comment.id}">
                            <i class="fa-solid fa-arrow-down"></i>
                        </button>
                    </div>
                    </div>
                    <hr>
                    `);
                    commentElement.hide().appendTo('#comments-list').fadeIn('slow');
                });

                $('.report-comment').click(function(e) {
                    e.preventDefault();
                    const commentId = $(this).data('comment-id');
                    const userId = $('#comments').data('current-user-id');

                    const reason = prompt('Введите причину жалобы:');
                    if (reason) {
                        $.ajax({
                            url: `/manga/comment/${commentId}/report`,
                            method: 'GET',
                            data: { userId: userId, reason: reason },
                            success: function(response) {
                                alert('Ваше повідомлення про порушення надіслано.');
                            },
                            error: function(xhr, status, error) {
                                alert('Ви вже надіслали повідомлення про цей коментар.');
                            }
                        });
                    }
                });

                $('#comments-list').on('click', '.edit-comment', function(e) {
                    e.preventDefault();
                    const commentElement = $(this).closest('.comment');
                    const commentId = commentElement.attr('id').split('-')[1];
                    const commentText = commentElement.find('.user-comment-text').text().trim();

                    commentElement.find('.user-comment-text').hide();
                    commentElement.find('.edit-comment-text').val(commentText).show();
                    commentElement.find('.edit-comment-save').show();
                    commentElement.find('.edit-comment-cancel').show();
                });

            $('#comments-list').on('click', '.edit-comment-save', function() {
                const commentElement = $(this).closest('.comment');
                const commentId = commentElement.attr('id').split('-')[1];
                const newCommentText = commentElement.find('.edit-comment-text').val().trim();
                $.ajax({
                    url: `/manga/comment/${commentId}/edit`,
                    method: 'GET',
                    data: { commentText: newCommentText },
                    success: function(response) {
                        commentElement.find('.user-comment-text').text(newCommentText).show();
                        commentElement.find('.edit-comment-text').hide();
                        commentElement.find('.edit-comment-save').hide();
                        commentElement.find('.edit-comment-cancel').hide();
                    },
                    error: function(xhr, status, error) {
                        console.error('Помилка редагування коментаря:', error);
                        alert('Коментар повинен містити від 1 до 1000 символів.');
                    }
                });
            });

                $('#comments-list').on('click', '.edit-comment-cancel', function() {
                    const commentElement = $(this).closest('.comment');
                    commentElement.find('.user-comment-text').show();
                    commentElement.find('.edit-comment-text').hide();
                    commentElement.find('.edit-comment-save').hide();
                    commentElement.find('.edit-comment-cancel').hide();
                });

                $('#comments-list').off('click', '.delete-comment').on('click', '.delete-comment', function(e) {
                    e.preventDefault();
                    e.stopImmediatePropagation();
                    const form = $(this).closest('form');
                    const commentId = form.find('input[name="comment-id"]').val();

                    if (confirm('Ви впевнені, що хочете видалити цей коментар?')) {
                        $.ajax({
                            url: form.attr('action'),
                            method: 'GET',
                            data: form.serialize(),
                            success: function(response) {
                                form.closest('.comment').remove();
                            },
                            error: function(xhr, status, error) {
                                console.error('Помилка видалення коментаря:', error);
                                alert('Помилка видалення коментаря. Спробуйте пізніше.');
                            }
                        });
                    }
                });

                $('#comments-list').on('click', '.reply-button', function() {
                    const commentId = $(this).data('comment-id');
                    console.log('Відповісти на коментар:', commentId);
                });
            }
        });
    }

    $('#comments-list').on('click', '.upvote-button, .downvote-button', function() {
        const commentId = $(this).data('comment-id');
        const userId = $('#comments').data('current-user-id');
        const delta = $(this).hasClass('upvote-button') ? 1 : -1;
        const currentButton = $(this);

        $.ajax({
            url: `/manga/comment/${commentId}/rate`,
            method: 'GET',
            data: {
                userId: userId,
                delta: delta
            },
            success: function(response) {
                updateRatingDisplay(commentId, response.newRatingScore, response.ratingTitle);

                if (currentButton.hasClass('selected')) {
                    currentButton.removeClass('selected');
                } else {
                    currentButton.addClass('selected');
                    currentButton.siblings('.upvote-button, .downvote-button').removeClass('selected');
                }
            },
            error: function() {
            }
        });
    });
    function updateRatingDisplay(commentId, newRating, ratingTitle) {
        const ratingElement = $(`#rating-score-${commentId}`);
        ratingElement.text(newRating);
        ratingElement.attr('title', ratingTitle);
    }
});
