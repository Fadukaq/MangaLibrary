$(document).ready(function() {
    const urlParams = new URLSearchParams(window.location.search);

    function updateTabState(tabGroup, paramName, defaultValue) {
        const activeTab = urlParams.get(paramName) || defaultValue;
        $(`${tabGroup} a[href="#${activeTab}"]`).tab('show');
    }

    updateTabState('#myTab', 'tab', 'userList');
    updateTabState('#myTabType', 'view', 'gridType');
    updateTabState('#myTabLists', 'list', 'reading');

    function handleTabClick(e, paramName) {
        e.preventDefault();
        const $this = $(this);
        const target = $this.attr('href').substring(1);
        const urlParams = new URLSearchParams(window.location.search);
        if (paramName === 'tab') {
            urlParams.set('tab', target);
            urlParams.delete('list');
            urlParams.set('page', '1');
            history.pushState(null, null, '?' + urlParams.toString());
            location.reload();
        } else if (paramName === 'list') {
            urlParams.set('list', target);
            urlParams.set('page', '1');
            history.pushState(null, null, '?' + urlParams.toString());
            location.reload();
        } else {
            urlParams.set(paramName, target);
            urlParams.delete('tab');
            urlParams.delete('list');
            history.pushState(null, null, '?' + urlParams.toString());
            updateContent(paramName, target);
        }

        $this.tab('show');
    }

    $('#myTab a').on('click', function(e) { handleTabClick.call(this, e, 'tab'); });
    $('#myTabLists a').on('click', function(e) { handleTabClick.call(this, e, 'list'); });
    $('#myTabType a').on('click', function(e) { handleTabClick.call(this, e, 'view'); });

    $(window).on('popstate', function () {
        updateTabState('#myTab', 'tab', 'userList');
        updateTabState('#myTabType', 'view', 'gridType');
    });

    function updateContent(paramName, target) {
        console.log(`Updating content for ${paramName}: ${target}`);
    }
    const firstTab = document.getElementById('toManga');
    const commentsTab = document.getElementById('comment-manga-tab');
    if (firstTab) {
        firstTab.classList.add('active');
        firstTab.classList.remove('fade');
    }
    if (commentsTab) {
        commentsTab.classList.add('active');
    }
});

/*document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.nav-link[data-list]');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const list = this.getAttribute('data-list');
            updatePaginationLinks(list);
        });
    });

    function updatePaginationLinks(list) {
        const paginationLinks = document.querySelectorAll('.pagination a');
        paginationLinks.forEach(link => {
            let href = link.getAttribute('href');
            href = href.replace(/list=[\w-]+/, `list=${list}`);
            link.setAttribute('href', href);
        });
    }
});*/