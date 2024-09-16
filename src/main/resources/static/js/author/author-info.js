document.addEventListener('DOMContentLoaded', function () {
    const mangaContainers = document.querySelectorAll('.custom-container');
    const dateSortButton = document.getElementById('date-nav-item');
    const ratingSortButton = document.getElementById('rating-nav-item');
    const filterInput = document.getElementById('filterInput');
    const mangaGrid = document.querySelector('.manga-grid');

    function sortByDate() {
        const sortedByDate = Array.from(mangaContainers).sort((a, b) => {
            const idA = parseInt(a.querySelector('a').getAttribute('href').split('/manga/')[1]);
            const idB = parseInt(b.querySelector('a').getAttribute('href').split('/manga/')[1]);
            return idB - idA;
        });
        updateMangaGrid(sortedByDate);
    }

    function sortByRating() {
        const sortedByRating = Array.from(mangaContainers).sort((a, b) => {
            const ratingA = parseFloat(a.querySelector('.custom-grade-details').dataset.rating);
            const ratingB = parseFloat(b.querySelector('.custom-grade-details').dataset.rating);
            return ratingB - ratingA;
        });
        updateMangaGrid(sortedByRating);
    }

    function sortByName() {
        const sortedByName = Array.from(mangaContainers).sort((a, b) => {
            const nameA = a.querySelector('.custom-title').textContent.trim().toLowerCase();
            const nameB = b.querySelector('.custom-title').textContent.trim().toLowerCase();
            return nameA.localeCompare(nameB);
        });
        updateMangaGrid(sortedByName);
    }

    function filterManga() {
        const query = filterInput.value.trim().toLowerCase();
        mangaContainers.forEach(manga => {
            const name = manga.querySelector('.custom-title').textContent.trim().toLowerCase();
            manga.style.display = name.includes(query) ? '' : 'none';
        });
    }

    function updateMangaGrid(sortedMangas) {
        mangaGrid.innerHTML = '';
        sortedMangas.forEach(manga => {
            mangaGrid.appendChild(manga);
        });
    }

    dateSortButton.addEventListener('click', sortByDate);
    ratingSortButton.addEventListener('click', sortByRating);

    filterInput.addEventListener('input', filterManga);
});

document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('mangaBiographyContainer');
    const toggleBtn = document.getElementById('toggleDescriptionBtn');

    toggleBtn.addEventListener('click', function () {
        if (container.classList.contains('description-expanded')) {
            container.classList.remove('description-expanded');
            toggleBtn.textContent = 'Показати більше';
        } else {
            container.classList.add('description-expanded');
            toggleBtn.textContent = 'Приховати';
        }
    });
});