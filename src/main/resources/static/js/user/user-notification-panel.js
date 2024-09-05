const notificationTabs = document.querySelectorAll('.nav-tabs-notifications .nav-link');

function updateUrlWithHash(tabId) {
    window.location.hash = tabId;
}

function isTabEmpty(tabId) {
    const content = document.getElementById(`${tabId}-content`);
    return content && content.innerHTML.trim() === '';
}

function showNotificationContent(tabId) {
    document.querySelectorAll('.tab-pane-notification').forEach(content => {
        content.classList.remove('show', 'active');
    });

    const activeContent = document.getElementById(tabId);
    if (activeContent) {
        activeContent.classList.add('show', 'active');
    }

    notificationTabs.forEach(tab => {
        tab.classList.remove('active');
        if (tab.getAttribute('data-list') === tabId) {
            tab.classList.add('active');
        }
    });

    if (tabId === 'manga-notification' && isTabEmpty(tabId)) {
        showNotificationContent('author-notification');
    }
}

notificationTabs.forEach(tab => {
    tab.addEventListener('click', function(event) {
        event.preventDefault();
        const tabId = this.getAttribute('data-list');
        updateUrlWithHash(tabId);
        showNotificationContent(tabId);
    });
});

window.addEventListener('hashchange', function() {
    const hash = window.location.hash.substring(1);
    showNotificationContent(hash);
});

document.addEventListener('DOMContentLoaded', function() {
    const hash = window.location.hash.substring(1);
    if (hash) {
        showNotificationContent(hash);
    } else {
        showNotificationContent('manga-notification');
    }
});