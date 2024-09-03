$(document).ready(function() {
    let currentPage = 1;
    const pageSize = 4;
    let isLoading = false;
    let hasMoreComments = true;
    let sortOption = 'byNew';

    $('.dropdown-sort .dropdown-menu .dropdown-item').on('click', function(event) {
        event.preventDefault();
        sortOption = $(this).data('sort');
        $('#dropdownMenuText').text($(this).text());
        $('#comments-list').empty();
        currentPage = 1;
        hasMoreComments = true;
        loadComments(sortOption, currentPage, pageSize);
        $('.dropdown-sort .dropdown-menu .dropdown-item').removeClass('active');
        $(this).addClass('active');
    });
    $('#comments-list').on('click', '.cancel-reply', function() {
        $(this).closest('.reply-form-container').hide();
    });

    function formatDate(dateString) {
        const options = { day: 'numeric', month: 'short', year: 'numeric' };
        const date = new Date(dateString);
        return date.toLocaleDateString('uk-UA', options);
    }

    $(document).on('click', '.reply-button', function() {
        const parentId = $(this).data('comment-id');
        const parentUserName = $(this).closest('.comment').find('.user-name-comment').text();
        const $formContainer = $(`#comment-${parentId}`).find('.reply-form-container');
        const $form = $formContainer.find('form');
        const $textarea = $form.find('textarea');

        const replyText = `@${parentUserName}, `;
        $textarea.val(replyText);
        $formContainer.show();

        $form.find('.username-block').show();
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
            url: `/manga/${mangaId}/add-comment`,
            data: formData,
            success: function(comment) {
                $('#comment-text').val('');
                $('#comments-list').empty();
                currentPage = 1;
                hasMoreComments = true;
                loadComments(sortOption, currentPage, pageSize);
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

    function loadComments(sortBy, page, size) {
        const mangaId = $('#comments').data('manga-id');
        const currentUserId = $('#comments').data('current-user-id');
        if (isLoading || !hasMoreComments) return;
        isLoading = true;
        $('#loading-comments-for-manga').show();

        //setTimeout(function() {
            $.ajax({
                url: `/manga/${mangaId}/comments`,
                method: 'GET',
                data: {
                    page: page,
                    size: size,
                    sortBy: sortBy
                },
                success: function(response) {
                    const comments = response.comments;
                    const userRatings = response.userRatings;
                    const commentRatings = response.commentRatings;
                    const commentReplies = response.commentReplies;
                    hasMoreComments = response.hasMore;

                    isLoading = false;
                    $('#loading-comments-for-manga').hide();

                    const $commentsList = $('#comments-list');
                    if (page === 1) {
                        $commentsList.empty();
                    }

                    if (comments.length === 0 && page === 1) {
                        $commentsList.append(`
                    <li class="list-group-nocomments-item">
                        <i class="fa-regular fa-face-smile-wink"></i>
                        Будьте першим хто залишить коментар для цієї манги.
                    </li>
                `);
                        $('.dropdown-sort').addClass('disabled');
                    } else {
                        $('.dropdown-sort').removeClass('disabled');
                    }

                    if (sortBy === 'byNew') {
                        comments.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
                    } else if (sortBy === 'byRating') {
                        comments.sort((a, b) => {
                            const ratingInfoA = commentRatings[a.id] || { upvotes: 0, downvotes: 0 };
                            const ratingInfoB = commentRatings[b.id] || { upvotes: 0, downvotes: 0 };

                            const scoreA = ratingInfoA.upvotes - ratingInfoA.downvotes;
                            const scoreB = ratingInfoB.upvotes - ratingInfoB.downvotes;

                            return scoreB - scoreA;
                        });
                    }
                    comments.forEach(comment => {
                        const userName = comment.userName || 'Unknown User';
                        const userIcon = comment.ProfilePicture || 'https://www.riseandfall.xyz/unrevealed.png';
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
                        `;
                        }

                        if ( comment.userId === currentUserId) {
                            optionsMenu += `
                            <form action="/manga/comment/${comment.id}/delete" method="post">
                                <input type="hidden" name="comment-id" value="${comment.id}">
                                <button class="dropdown-item delete-comment" type="submit"><i class="fas fa-trash-alt"></i>Видалити</button>
                            </form>
                        `;
                        }
                        optionsMenu += `</ul></div>`;

                        commentElement.html(`
                    <div class="comment-header">
                        <a href="/profile/${comment.userId}" class="user-link">
                            <img src="${userIcon}" class="user-icon" alt="${userName}'s icon">
                            <span class="user-name-comment">${userName}</span>
                        </a>
                        ${optionsMenu}
                    </div>
                    <p class="user-comment-text">${comment.text}</p>
                    <textarea class="edit-comment-text form-control mb-3" style="display:none; width:100%;">${comment.text}</textarea>
                    <button class="btn btn-primary edit-comment-save" style="display:none;">Зберегти</button>
                    <button class="btn btn-primary edit-comment-cancel" style="display:none;">Скасувати</button>
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
                    <div class="reply-form-container" style="display: none;">
                        <form class="reply-form" data-username="${comment.userName}" data-parent-id="${comment.id}">
                            <div class="textarea-container">
                                <span class="username-block">@${comment.userName}</span>
                                <textarea name="text" class="form-control" rows="2" placeholder="Напишіть відповідь..." required></textarea>
                            </div>
                            <button type="submit" class="btn btn-primary mt-2">Відповісти</button>
                            <button type="button" class="btn btn-primary mt-2 cancel-reply">Скасувати</button>
                        </form>
                    </div>
                    <button class="btn btn-link show-replies-button" data-comment-id="${comment.id}" style="display: none;">Показати відповіді (<span class="replies-count">0</span>)</button>
                    <div class="replies-container" style="display: none;"></div>
                    <hr>
                    `);

                        commentElement.hide().appendTo('#comments-list').fadeIn('slow');

                        const replies = commentReplies[comment.id] || [];
                        if (replies.length > 0) {
                            commentElement.find('.show-replies-button').show().find('.replies-count').text(replies.length);
                        }

                        commentElement.find('.show-replies-button').on('click', function() {
                            const commentId = $(this).data('comment-id');
                            const repliesContainer = $(this).siblings('.replies-container');

                            repliesContainer.toggle();

                            const repliesCount = repliesContainer.find('.reply').length;

                            $(this).text(function(i, text) {
                                const newCount = repliesContainer.find('.reply').length;
                                return repliesContainer.is(':visible')
                                ? `Приховати відповіді`
                                : `Показати відповіді (${newCount})`;
                            });

                            if (repliesContainer.is(':empty')) {
                                const replies = commentReplies[commentId] || [];
                                replies.forEach(reply => {
                                    const replyElement = createReplyElement(reply, comment.id);
                                    repliesContainer.append(replyElement);
                                });
                            }
                        });

                    });

                    $(document).on('click', '.report-comment', function(e) {
                        e.preventDefault();
                        const commentId = $(this).data('comment-id');
                        const userId = $('#comments').data('current-user-id');
                        $('#reportCommentId').val(commentId);
                        $('#reportUserId').val(userId);
                        $('#reportCommentModal').modal('show');
                    });

                    $('#submitReport').click(function() {
                        const commentId = $('#reportCommentId').val();
                        const userId = $('#reportUserId').val();
                        const reason = $('#reportReason').val();
                        const maxLength = 255;
                        if (!reason || reason.trim() === '') {
                            $('#reportCommentModal').modal('hide');
                            $('#errorMessage').text('Будь ласка, введіть причину скарги.');
                            $('#errorModal').modal('show');
                            return;
                        }else if (reason.length > maxLength) {
                            $('#reportCommentModal').modal('hide');
                            $('#errorMessage').text(`Причина занадто довга. Максимальна довжина: ${maxLength} символів.`);
                            $('#errorModal').modal('show');
                            $('#reportReason').val('');
                            return;
                        }
                        if (reason) {
                            $.ajax({
                                url: `/manga/comment/${commentId}/report`,
                                method: 'GET',
                                data: {
                                    userId: userId,
                                    reason: reason
                                },
                                success: function(response) {
                                    $('#reportCommentModal').modal('hide');
                                    $('#successMessage').text('Ваша скарга надіслана.');
                                    $('#successModal').modal('show');
                                    $('#reportReason').val('');
                                },
                                error: function(xhr, status, error) {
                                    $('#reportCommentModal').modal('hide');
                                    $('#errorMessage').text('Ви вже надіслали повідомлення про цей коментар.');
                                    $('#errorModal').modal('show');
                                    $('#reportReason').val('');
                                }
                            });
                        } else {
                            $('#reportCommentModal').modal('hide');
                            $('#errorMessage').text('Будь ласка, введіть причину скарги.');
                            $('#errorModal').modal('show');
                        }
                    });
                    if (hasMoreComments) {
                        $('#load-more-comments').show();
                    } else {
                        $('#load-more-comments').hide();
                    }
                },
                error: function() {
                    $('#loading-comments-for-manga').hide();
                    isLoading = false;
                    alert('Ошибка загрузки комментариев');
                }
            });
        //}, 500);
    }

    $(window).on('scroll', function() {
        const scrollPosition = $(window).scrollTop() + $(window).height();
        const documentHeight = $(document).height();
        if (scrollPosition >= documentHeight - 100 && !isLoading && hasMoreComments) {
            currentPage++;
            loadComments(sortOption, currentPage, pageSize);
        }
    });
    loadComments(sortOption, currentPage, pageSize);

    function updateRatingDisplay(commentId, newRating, ratingTitle) {
        const ratingElement = $(`#rating-score-${commentId}`);
        ratingElement.text(newRating);
        ratingElement.attr('title', ratingTitle);
    }

    //Comment
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

    $(document).on('click', '.delete-comment', function(e) {
        e.preventDefault();
        e.stopImmediatePropagation();

        const form = $(this).closest('form');
        commentToDelete = form.closest('.comment');
        $('#deleteConfirmationModal').modal('show');
    });

    $('#confirmDeleteButton').click(function() {
        if (commentToDelete) {
            const commentId = $(commentToDelete).find('input[name="comment-id"]').val();

            $.ajax({
                url: `/manga/comment/${commentId}/delete`,
                method: 'GET',
                data: { commentId: commentId },
                success: function(response) {
                    commentToDelete.remove();
                    $('#deleteConfirmationModal').modal('hide');
                    currentPage = 1;
                    loadComments(sortOption, currentPage, pageSize);
                },
                error: function(xhr, status, error) {
                    $('#deleteConfirmationModal').modal('hide');
                    $('#errorMessage').text('Помилка видалення коментаря. Спробуйте пізніше.');
                    $('#errorModal').modal('show');
                }
            });
        }
    });

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

    //Reply
    $('#comments-list').on('submit', '.reply-form', function(event) {
        event.preventDefault();

        const $form = $(this);
        const formData = $form.serializeArray();
        const parentId = $form.data('parent-id');
        const mangaId = $('#comments').data('manga-id');

        const formDataObject = {};
        formData.forEach(item => {
            formDataObject[item.name] = item.value;
        });

        formDataObject.parentCommentId = parentId;
        formDataObject.mangaId = mangaId;

        const queryString = $.param(formDataObject);

        $.ajax({
            type: 'GET',
            url: '/manga/comment/reply?' + queryString,
            success: function(response) {
                if (response.success) {
                    const newReply = response.reply;
                    const formattedDate = formatDate(newReply.createdAt);
                    const replyText = linkifyUsernamesAndReplaceIds(newReply.text);

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
                                    <hr>
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
    });

    $(document).on('click', '.delete-reply', function(e) {
        e.preventDefault();
        const form = $(this).closest('form');
        const replyId = form.find('input[name="reply-id"]').val();
        const commentId = $(this).closest('.comment').attr('id').split('-')[1];

        replyToDelete = { replyId: replyId, commentId: commentId };
        $('#deleteReplyModal').modal('show');
    });

    $('#confirmDeleteReplyButton').click(function() {
        if (replyToDelete) {
            $.ajax({
                url: `/manga/reply/${replyToDelete.replyId}/delete`,
                method: 'GET',
                success: function(response) {
                    const $reply = $(`.reply[data-reply-id="${replyToDelete.replyId}"]`);
                    if ($reply.length) {
                        $reply.remove();
                    } else {
                        console.warn('Reply not found for ID:', replyToDelete.replyId);
                    }

                    const $comment = $(`.comment[id="comment-${replyToDelete.commentId}"]`);
                    if ($comment.length) {
                        const replies = $comment.find('.reply');
                        const repliesCount = replies.length;

                        const $showRepliesButton = $comment.find('.show-replies-button');
                        if (repliesCount > 0) {
                            $showRepliesButton.find('.replies-count').text(repliesCount);
                        } else {
                            $showRepliesButton.hide();
                        }
                    } else {
                        console.warn('Comment not found for ID:', replyToDelete.commentId);
                    }

                    $('#deleteReplyModal').modal('hide');
                },
                error: function(xhr, status, error) {
                    $('#deleteReplyModal').modal('hide');
                    $('#errorMessage').text('Помилка видалення відповіді. Спробуйте пізніше.');
                    $('#errorModal').modal('show');
                }
            });
        }
    });

    $(document).on('click', '.report-reply', function(e) {
        e.preventDefault();
        const replyId = $(this).closest('.reply').data('reply-id');
        replyToReport = replyId;
        $('#reportReplyModal').modal('show');
    });

    $('#confirmReportReplyButton').click(function() {
        if (replyToReport) {
            const reason = $('#reportReplyReason').val();
            const maxLength = 255;
            if (!reason || reason.trim() === '') {
                $('#reportReplyModal').modal('hide');
                $('#errorMessage').text('Будь ласка, введіть причину скарги.');
                $('#errorModal').modal('show');
                return;
            }else if (reason.length > maxLength) {
                $('#reportReplyModal').modal('hide');
                $('#errorMessage').text(`Причина занадто довга. Максимальна довжина: ${maxLength} символів.`);
                $('#errorModal').modal('show');
                $('#reportReplyReason').val('');
                return;
            }
            if (reason) {
                const userId = $('#comments').data('current-user-id');
                $.ajax({
                    url: `/manga/reply/${replyToReport}/report`,
                    method: 'GET',
                    data: { userId: userId, reason: reason },
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

    $(document).on('click', '.edit-reply', function(event) {
        event.preventDefault();
        const replyId = $(this).closest('.reply').data('reply-id');
        const replyElement = $(`.reply[data-reply-id="${replyId}"]`);
        replyElement.find('.user-reply-text').hide();
        replyElement.find('.edit-reply-text').show();
        replyElement.find('.edit-reply-save').show();
        replyElement.find('.edit-reply-cancel').show();
    });

    $(document).on('click', '.edit-reply-cancel', function() {
        const replyElement = $(this).closest('.reply');
        replyElement.find('.user-reply-text').show();
        replyElement.find('.edit-reply-text').hide();
        replyElement.find('.edit-reply-save').hide();
        replyElement.find('.edit-reply-cancel').hide();
    });

    $(document).on('click', '.edit-reply-save', function() {
        const replyElement = $(this).closest('.reply');
        const replyId = replyElement.data('reply-id');
        const newText = replyElement.find('.edit-reply-text').val();
        const formattedText = linkifyUsernamesAndReplaceIds(newText);

        $.ajax({
            type: 'GET',
            url: `/manga/reply/${replyId}/edit`,
            data: { text: newText },
            success: function() {
                replyElement.find('.user-reply-text').html(formattedText).show();
                replyElement.find('.edit-reply-text').hide();
                replyElement.find('.edit-reply-save').hide();
                replyElement.find('.edit-reply-cancel').hide();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error('Error editing reply:', textStatus, errorThrown);
                alert('Відповідь повинна містити від 1 до 1000 символів.');
            }
        });
    });

    //Reply on Reply
    $(document).on('click', '.reply-on-reply-button', function() {
    const commentElement = $(this).closest('.comment');
    const commentId = commentElement.attr('id').split('-')[1];
    const parentId = commentId;

    const $reply = $(this).closest('.reply');
    const $formContainer = $reply.find('.reply-on-reply-form-container');
    const $form = $formContainer.find('form');

    const replyId = $(this).data('reply-id');
    const userName = $form.data('username');

    const $textarea = $form.find('textarea');
        $textarea.val(`@${userName}, `);

    $formContainer.toggle();
    });

    $(document).on('input', '.reply-form textarea', function() {
        const $textarea = $(this);
        const $form = $textarea.closest('form');
        const userName = $form.data('username');
        const textValue = $textarea.val();
        const prefix = `@${userName}`;

        if (textValue.startsWith(prefix)) {
            $form.find('.username-block').show();
        } else {
            $form.find('.username-block').hide();
        }
    });

    $(document).on('click', '.cancel-reply-on-reply', function() {
        const $formContainer = $(this).closest('.reply-on-reply-form-container');
        $formContainer.find('textarea').val("");
        $(this).closest('.reply-on-reply-form-container').hide();
    });

    function createReplyElement(reply,commentId) {
        const currentUserId = $('#comments').data('current-user-id');
        const userName = reply.userName || 'Unknown User';
        const userIcon = reply.ProfilePicture || 'https://www.riseandfall.xyz/unrevealed.png';
        const formattedDate = formatDate(reply.createdAt);
        const replyText = linkifyUsernamesAndReplaceIds(reply.text);

        let optionsMenu = `
        <div class="dropdown reply-options">
            <button class="btn btn-link dropdown-toggle" type="button" id="dropdownMenuButton-${reply.id}" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="fas fa-ellipsis-v"></i>
            </button>
            <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton-${reply.id}">
    `;

        if (reply.userId !== currentUserId) {
            optionsMenu += `
            <li><a class="dropdown-item report-reply" href="#" data-reply-id="${reply.id}"><i class="fas fa-flag"></i>Поскаржитися</a></li>
        `;
        }

        if (reply.userId === currentUserId) {
            optionsMenu += `
            <form action="/manga/reply/${reply.id}/edit" method="post">
                <input type="hidden" name="reply-id" value="${reply.id}">
                <button class="dropdown-item edit-reply" type="submit"><i class="fas fa-edit"></i>Редагувати</button>
            </form>
            <form action="/manga/reply/${reply.id}/delete" method="post">
                <input type="hidden" name="reply-id" value="${reply.id}">
                <button class="dropdown-item delete-reply" type="submit"><i class="fas fa-trash-alt"></i>Видалити</button>
            </form>
        `;
        }

        optionsMenu += `</ul></div>`;

        return $(`
        <div class="reply" data-reply-id="${reply.id}">
            <div class="reply-header">
                <a href="/profile/${reply.userId}" class="user-link">
                    <img src="${userIcon}" class="user-icon" alt="${userName}'s icon">
                    <span class="user-name">${userName}</span>
                </a>
            </div>
            <p class="user-reply-text">${replyText}</p>
            <div class="reply-footer">
                <small class="comment-time">${formattedDate}</small>
                <button class="btn btn-link reply-on-reply-button" data-reply-id="${reply.id}">Відповісти</button>
                ${optionsMenu}
            </div>
            <div class="reply-on-reply-form-container" style="display: none;">
    <form class="reply-form" data-parent-id="${commentId}" data-username="${reply.userName}">
        <div class="textarea-container">
            <span class="username-block">@${reply.userName}</span>
            <textarea name="text" class="form-control" rows="2" placeholder="Напишіть відповідь..." required></textarea>
        </div>
        <button type="submit" class="btn btn-primary mt-2">Відповісти</button>
        <button type="button" class="btn btn-primary mt-2 cancel-reply-on-reply">Скасувати</button>
    </form>
</div>
            <textarea class="edit-reply-text form-control mb-3" style="display:none; width:100%;">${reply.text}</textarea>
            <button class="btn btn-primary edit-reply-save" style="display:none;">Зберегти</button>
            <button class="btn btn-primary edit-reply-cancel" style="display:none;">Скасувати</button>
        </div>
    `);
    }

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
});