document.addEventListener('DOMContentLoaded', function () {
    function formatDate(isoDate) {
        const options = { day: '2-digit', month: '2-digit', year: 'numeric' };
        return new Date(isoDate).toLocaleDateString('uk-UA', options);
    }
    document.querySelectorAll('td[data-date]').forEach(function(td) {
        td.textContent = formatDate(td.getAttribute('data-date'));
    });
});
