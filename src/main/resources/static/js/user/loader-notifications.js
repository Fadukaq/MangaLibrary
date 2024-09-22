let currentMangaIndex = 2;
let currentAuthorIndex = 2;
const mangaNotificationElements = document.querySelectorAll('.notification-element-manga');
const authorNotificationElements = document.querySelectorAll('.notification-element-author');
const loadCount = 2;
const loaderNotify = document.getElementById('loaderNotify');

function showNotifications(type) {
    if ((type === 'manga' && currentMangaIndex >= mangaNotificationElements.length) ||
    (type === 'author' && currentAuthorIndex >= authorNotificationElements.length)) {
        return;
    }

    loaderNotify.style.display = 'block';

    setTimeout(() => {
        if (type === 'manga') {
            for (let i = 0; i < loadCount && currentMangaIndex < mangaNotificationElements.length; i++, currentMangaIndex++) {
                mangaNotificationElements[currentMangaIndex].style.display = 'block';
            }
        } else if (type === 'author') {
            for (let i = 0; i < loadCount && currentAuthorIndex < authorNotificationElements.length; i++, currentAuthorIndex++) {
                authorNotificationElements[currentAuthorIndex].style.display = 'block';
            }
        }
        loaderNotify.style.display = 'none';
    }, 500);
}

function showInitialNotifications() {
    for (let i = 0; i < 2 && i < mangaNotificationElements.length; i++) {
        mangaNotificationElements[i].style.display = 'block';
    }

    for (let i = 0; i < 2 && i < authorNotificationElements.length; i++) {
        authorNotificationElements[i].style.display = 'block';
    }
}

window.addEventListener('scroll', () => {
    const activeTab = document.querySelector('.tab-pane-notification.active');
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight) {
        if (activeTab && activeTab.id === 'manga-notification') {
            showNotifications('manga');
        } else if (activeTab && activeTab.id === 'author-notification') {
            showNotifications('author');
        }
    }
});

document.addEventListener('DOMContentLoaded', () => {
    showInitialNotifications();
});