document.addEventListener('DOMContentLoaded', function() {
    const sortButton = document.getElementById('sortChapters');
    const sortIcon = document.getElementById('sortIcon');
    const chaptersList = document.getElementById('chaptersList');
    let isAscending = false;

    function sortChapters() {
        const chapters = Array.from(chaptersList.children);
        chapters.sort((a, b) => {
            const dateA = new Date(a.getAttribute('data-date'));
            const dateB = new Date(b.getAttribute('data-date'));

            return isAscending ? dateA - dateB : dateB - dateA;
        });

        chapters.forEach(chapter => chaptersList.appendChild(chapter));
    }
    sortChapters();

    sortButton.addEventListener('click', function() {
        isAscending = !isAscending;

        sortChapters();

        if (isAscending) {
            sortIcon.classList.remove('fa-sort-amount-down');
            sortIcon.classList.add('fa-sort-amount-up');
        } else {
            sortIcon.classList.remove('fa-sort-amount-up');
            sortIcon.classList.add('fa-sort-amount-down');
        }
    });
});