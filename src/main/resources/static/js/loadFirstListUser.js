document.addEventListener('DOMContentLoaded', function() {
    var hash = window.location.hash;
    if (!hash) {
        document.getElementById('reading-tab').classList.add('active');
        document.getElementById('reading').classList.add('show', 'active');
    }
});

$(document).ready(function() {
    var hash = window.location.hash;
    if (hash) {
        $('.nav-link[href="' + hash + '"]').tab('show');
    }

    $('.nav-link').on('shown.bs.tab', function(event) {
        var tabId = $(event.target).attr('href');
        history.replaceState({}, '', tabId);
    });
});