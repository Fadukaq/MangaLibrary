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

        $button.prop('disabled', true);

        $.ajax({
            type: 'POST',
            url: `/manga/${mangaId}/add-comment`,
            data: formData,
            success: function(data) {
                loadComments();
                $('#comment-text').val('');
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error('Error adding comment:', textStatus, errorThrown);
            },
            complete: function() {
                setTimeout(() => $button.prop('disabled', false), 3000);
            }
        });
    });

    function loadComments() {
        const mangaId = $('#comments').data('manga-id');
        const currentUserId = $('#comments').data('current-user-id');

        $.ajax({
            url: `/manga/${mangaId}/comments`,
            method: 'GET',
            success: function(comments) {
                $('#comments-list').empty();
                comments.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
                comments.forEach(comment => {
                    const userName = comment.userName || 'Unknown User';
                    const userIcon = comment.ProfilePicture || '<i class="fa-solid fa-user-circle"></i>';
                    const formattedDate = formatDate(comment.createdAt);

                    const commentElement = $('<div>').addClass('comment').attr('id', `comment-${comment.id}`);

                    let optionsMenu = `
    <div class="dropdown comment-options">
        <button class="btn btn-link dropdown-toggle" type="button" id="dropdownMenuButton-${comment.id}" data-bs-toggle="dropdown" aria-expanded="false">
            <i class="fas fa-ellipsis-v"></i>
        </button>
        <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton-${comment.id}">
            <li><a class="dropdown-item report-comment" href="#" data-comment-id="${comment.id}"><i class="fas fa-flag"></i>Поскаржитися</a></li>
`;

                    if (comment.userId === currentUserId) {
                        optionsMenu += `
        <form action="/manga/comment/${comment.id}/edit" method="post">
            <input type="hidden" name="comment-id" th:value="${comment.id}">
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
    </div>
    <hr>
`);
                    $('#comments-list').append(commentElement);
                });

                $('.report-comment').click(function(e) {
                    e.preventDefault();
                    const commentId = $(this).data('comment-id');
                    console.log('Надіслано скаргу на коментар:', commentId);
                });

                $('#comments-list').on('click', '.edit-comment', function(e) {
                    e.preventDefault();
                    const commentElement = $(this).closest('.comment');
                    const commentId = commentElement.data('comment-id');
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


                $('#comments-list').on('click', '.delete-comment', function(e) {
                    e.preventDefault();
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
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error('Error:', textStatus, errorThrown);
            }
        });
    }
});