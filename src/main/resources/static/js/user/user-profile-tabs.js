$(document).ready(function() {
    const urlParams = new URLSearchParams(window.location.search);

    const activeViewTab = urlParams.get('view') || 'gridType';
    const activeMainTab = urlParams.get('tab') || 'userList';

    $('#myTabType a[href="#' + activeViewTab + '"]').tab('show');
    $('#myTab a[href="#' + activeMainTab + '"]').tab('show');

    $('#myTab a').on('click', function (e) {
        e.preventDefault();
        const target = $(this).attr('href').substring(1);

        const newParams = new URLSearchParams();
        newParams.set('tab', target);
        history.pushState(null, null, '?' + newParams.toString());
        $(this).tab('show');
    });

    $('#myTabLists a').on('click', function (e) {
        e.preventDefault();
        const target = $(this).attr('href').substring(1);
        const currentMainTab = $('#myTab .nav-link.active').attr('href').substring(1);

        const newParams = new URLSearchParams(window.location.search);
        newParams.set('tab', currentMainTab);
        newParams.set('list', target);
        history.pushState(null, null, '?' + newParams.toString());
        $(this).tab('show');
    });

    $('#myTabType a').on('click', function (e) {
        e.preventDefault();
        const target = $(this).attr('href').substring(1);
        const currentMainTab = $('#myTab .nav-link.active').attr('href').substring(1);

        const newParams = new URLSearchParams(window.location.search);
        newParams.set('tab', currentMainTab);
        newParams.set('view', target);
        history.pushState(null, null, '?' + newParams.toString());
        $(this).tab('show');
    });

    $(window).on('popstate', function () {
        const urlParams = new URLSearchParams(window.location.search);
        const activeMainTab = urlParams.get('tab') || 'userList';
        const activeViewTab = urlParams.get('view') || 'gridType';
        $('#myTab a[href="#' + activeMainTab + '"]').tab('show');
        $('#myTabType a[href="#' + activeViewTab + '"]').tab('show');
    });
});
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get('tab') || 'userList';
    const list = urlParams.get('list') || 'reading';

    const tabId = list; // 'reading', 'recited', 'wantToRead', 'favorite', 'stoppedReading'
    const tabElement = document.getElementById(tabId);

    if (tabElement) {
        document.querySelectorAll('.tab-pane').forEach(pane => {
            pane.classList.remove('active', 'show');
        });

        tabElement.classList.add('active', 'show');
    }
});
document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('#myTabLists .nav-link');

    tabs.forEach(tab => {
        tab.addEventListener('click', function(event) {
            event.preventDefault();
            const list = this.getAttribute('data-list');
            const targetPane = document.querySelector(this.getAttribute('href'));
            const activeTab = document.querySelector(`#myTabLists .nav-link[href="#${activePaneId}"]`);
            if (activeTab) {
                activeTab.classList.add('active');
            }
            const url = new URL(window.location.href);
            url.searchParams.set('list', list);
            url.searchParams.set('page', '0');
            url.searchParams.set('size', '15');
            window.location.href = url.toString();

            const activePane = document.querySelector('.tab-pane.show');
            if (activePane) {
                activePane.classList.add('hidden');
                setTimeout(() => {
                    activePane.classList.remove('show', 'hidden');
                }, 200);
            }

            targetPane.classList.add('show');
        });
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const activePaneId = document.querySelector('.tab-pane.show').id;

    const activeTab = document.querySelector(`#myTabLists .nav-link[href="#${activePaneId}"]`);

    if (activeTab) {
        activeTab.classList.add('active');
    }

    const tabs = document.querySelectorAll('#myTabLists .nav-link');
    tabs.forEach(tab => {
        tab.addEventListener('click', function(event) {
            event.preventDefault();

            const targetPaneId = this.getAttribute('href').substring(1);
            const url = new URL(window.location.href);
            url.searchParams.set('list', this.getAttribute('data-list'));
            url.searchParams.set('page', '0');
            url.searchParams.set('size', '15');
            window.location.href = url.toString();
        });
    });
});
$('#myTab a, #myTabLists a, #myTabType a').on('click', function(e) {
    e.preventDefault();
    const target = $(this).attr('href').substring(1);

    const urlParams = new URLSearchParams(window.location.search);
    urlParams.set('page', '0');
    urlParams.delete('size');
    urlParams.delete('list');

    if ($(this).closest('#myTab').length) {
        urlParams.set('tab', target);
    } else if ($(this).closest('#myTabLists').length) {
    } else if ($(this).closest('#myTabType').length) {
        urlParams.set('view', target);
        urlParams.set('tab', $('#myTab .nav-link.active').attr('href').substring(1));
    }
    history.pushState(null, null, '?' + urlParams.toString());
    $(this).tab('show');
    location.reload();
});

document.addEventListener('DOMContentLoaded', function() {
    const firstTab = document.getElementById('toManga');
    const tabLink = document.querySelector('a[href="#userComments"]');
    const commentsTab = document.getElementById('comment-manga-tab');
    firstTab.classList.add('active');
    commentsTab.classList.add('active');
    firstTab.classList.remove('fade');
});