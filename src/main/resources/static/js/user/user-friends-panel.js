const tabs = document.querySelectorAll('.nav-tabs-friends .nav-link');

function updateUrlWithHash(tabId) {
    window.location.hash = tabId;
}

function isTabEmpty(tabId) {
    const content = document.getElementById(`${tabId}-content`);
    return content && content.innerHTML.trim() === '';
}

function showTabContent(tabId) {
    document.querySelectorAll('.tab-pane-friends').forEach(content => {
        content.classList.remove('show', 'active');
    });
    const activeContent = document.getElementById(`${tabId}-content`);
    if (activeContent) {
        activeContent.classList.add('show', 'active');
    }
    tabs.forEach(tab => {
        tab.classList.remove('active');
        if (tab.getAttribute('data-list') === tabId) {
            tab.classList.add('active');
        }
    });
    if (tabId === 'userFriends' && isTabEmpty(tabId)) {
        showTabContent('friends');
    }
}
tabs.forEach(tab => {
    tab.addEventListener('click', function(event) {
        event.preventDefault();
        const tabId = this.getAttribute('data-list');
        updateUrlWithHash(tabId);
        showTabContent(tabId);
    });
});
window.addEventListener('hashchange', function() {
    const hash = window.location.hash.substring(1);
    showTabContent(hash);
});
document.addEventListener('DOMContentLoaded', function() {
    const hash = window.location.hash.substring(1);
    if (hash) {
        showTabContent(hash);
    } else {
        showTabContent('friends');
    }
});