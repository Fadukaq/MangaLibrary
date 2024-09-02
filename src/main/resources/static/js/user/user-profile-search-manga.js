document.addEventListener('DOMContentLoaded', function() {
    const filterInput = document.getElementById('filterInput');
    const mangaContainers = document.querySelectorAll('.custom-container');

    filterInput.addEventListener('input', function() {
        const filterValue = filterInput.value.toLowerCase();

        mangaContainers.forEach(function(container) {
            const mangaName = container.querySelector('.custom-title span').textContent.toLowerCase();

            if (mangaName.includes(filterValue)) {
                container.style.display = '';
            } else {
                container.style.display = 'none';
            }
        });
    });
});