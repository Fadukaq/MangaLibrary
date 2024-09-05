document.addEventListener('DOMContentLoaded', function() {
    const profileModal = new bootstrap.Modal(document.getElementById('profileModal'));
    const profileButton = document.querySelector('#openProfileModal');
    if (profileButton) {
        profileButton.addEventListener('click', function() {
            profileModal.show();
        });
    }
});
document.addEventListener('DOMContentLoaded', function() {
    const roleElement = document.querySelector('.user-role-modal');
    if (roleElement) {
        const roleText = roleElement.textContent.trim();
        switch (roleText) {
            case 'Адміністратор':
                roleElement.classList.add('highlight-admin');
                break;
            case 'Модератор':
                roleElement.classList.add('highlight-moderator');
                break;
            case 'Автор':
                roleElement.classList.add('highlight-author');
                break;
            case 'Користувач':
                roleElement.classList.add('highlight-user');
                break;
            default:
                break;
        }
    }
    const statusElement = document.querySelector('.status-modal');
    if (statusElement) {
        const statusText = statusElement.textContent.trim();
        if (statusText === 'Активований') {
            statusElement.classList.add('status-active');
        } else if (statusText === 'Не активований') {
            statusElement.classList.add('status-inactive');
        }
    }
});