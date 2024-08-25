$(document).ready(function () {
    $('.nav-tabs .nav-link').on('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        activateTab($(this));
    });

    $('.header .dropdown-toggle').on('click', function (e) {
        e.stopPropagation();
    });
    function filterTable(input, table) {
        var filter = input.val().toUpperCase();
        var visibleRows = 0;
        table.find('tbody tr').each(function() {
            var text = $(this).find('td:first').text().toUpperCase();
            var visible = text.indexOf(filter) > -1;
            $(this).toggle(visible);
            if (visible) visibleRows++;
        });

        var noResultsMsg = table.siblings('.no-results');
        if (visibleRows === 0) {
            if (noResultsMsg.length === 0) {
                table.after('<p class="no-results">Нічого не знайдено</p>');
            } else {
                noResultsMsg.show();
            }
        } else {
            noResultsMsg.hide();
        }
    }

    $('.table-filter').on('input', function() {
        var table = $(this).closest('.tab-pane').find('table');
        filterTable($(this), table);
    });

    $('.nav-link').on('shown.bs.tab', function() {
        var activePane = $($(this).attr('href'));
        var filterInput = activePane.find('.table-filter');
        filterInput.val('');
        filterTable(filterInput, activePane.find('table'));
    });

    function activateTab($tab) {
        $('.nav-tabs .nav-link').removeClass('active');
        $('.tab-pane').removeClass('active show');
        $tab.addClass('active');
        $($tab.attr('href')).addClass('active show');
    }
    activateTab($('.nav-tabs .nav-link:first'));
});