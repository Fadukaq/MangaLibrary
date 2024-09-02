$(document).ready(function() {
    const urlParams = new URLSearchParams(window.location.search);

    const activeListTab = urlParams.get('list') || 'reading';
    const activeViewTab = urlParams.get('view') || 'gridType';
    const activeMainTab = urlParams.get('tab') || 'userList';

    $('#myTabLists a[href="#' + activeListTab + '"]').tab('show');
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
        const activeListTab = urlParams.get('list') || 'reading';
        const activeViewTab = urlParams.get('view') || 'gridType';
        $('#myTab a[href="#' + activeMainTab + '"]').tab('show');
        $('#myTabLists a[href="#' + activeListTab + '"]').tab('show');
        $('#myTabType a[href="#' + activeViewTab + '"]').tab('show');
    });
});
