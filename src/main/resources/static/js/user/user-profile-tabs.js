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
    // Получаем текущую панель, которая активна
    const activePaneId = document.querySelector('.tab-pane.show').id;

    // Находим вкладку, соответствующую активной панели
    const activeTab = document.querySelector(`#myTabLists .nav-link[href="#${activePaneId}"]`);

    // Добавляем класс active к активной вкладке
    if (activeTab) {
        activeTab.classList.add('active');
    }

    // Добавляем обработчик кликов для обновления активной вкладки
    const tabs = document.querySelectorAll('#myTabLists .nav-link');
    tabs.forEach(tab => {
        tab.addEventListener('click', function(event) {
            event.preventDefault();

            // Получаем id панели, соответствующей вкладке
            const targetPaneId = this.getAttribute('href').substring(1);

            // Обновляем URL с учетом текущей вкладки и сбрасываем параметры пагинации
            const url = new URL(window.location.href);
            url.searchParams.set('list', this.getAttribute('data-list'));
            url.searchParams.set('page', '0');
            url.searchParams.set('size', '15');

            // Переход к новому URL
            window.location.href = url.toString();
        });
    });
});