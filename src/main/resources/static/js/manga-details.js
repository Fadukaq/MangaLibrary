document.addEventListener('DOMContentLoaded', function() {
    var tabList = [].slice.call(document.querySelectorAll('#mangaTabs a'));
    var tabContent = document.getElementById('mangaTabsContent');

    function activateTab(tabId) {
        var tab = document.querySelector('#mangaTabs a[href="#' + tabId + '"]');
        var tabContent = document.getElementById(tabId);

        if (tab && tabContent) {
            tabList.forEach(function(tab) {
                tab.classList.remove('active');
                tab.setAttribute('aria-selected', 'false');
            });

            [].slice.call(tabContent.parentElement.children).forEach(function(content) {
                content.classList.remove('show', 'active');
            });

            tab.classList.add('active');
            tab.setAttribute('aria-selected', 'true');
            tabContent.classList.add('show', 'active');
        }
    }

    tabList.forEach(function(tab) {
        tab.addEventListener('click', function(event) {
            event.preventDefault();
            var tabId = this.getAttribute('href').substring(1);
            history.pushState(null, null, '#' + tabId);
            activateTab(tabId);
        });
    });

    var hash = window.location.hash.substring(1);
    if (hash) {
        activateTab(hash);
    }

    window.addEventListener('popstate', function() {
        var hash = window.location.hash.substring(1);
        if (hash) {
            activateTab(hash);
        }
    });
});