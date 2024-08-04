$(document).ready(function() {
    $('.dropdown-menu-sortBy').on('click', 'a', function(event) {
        event.preventDefault();
        const selectedText = $(this).text();
        $('#dropdownMenuText').text(`${selectedText}`);
        $(this).parent().siblings().find('a').removeClass('active');
        $(this).addClass('active');
    });
});
document.addEventListener('DOMContentLoaded', function() {
    const filterBtn = document.getElementById('filter-btn');
    const filterModal = document.getElementById('filter-modal');
    const closeBtn = document.getElementById('close-btn');

    filterBtn.addEventListener('click', function() {
        filterModal.style.display = 'block';
    });

    closeBtn.addEventListener('click', function() {
        filterModal.style.display = 'none';
    });

    window.addEventListener('click', function(event) {
        if (event.target === filterModal) {
            filterModal.style.display = 'none';
        }
    });
});
document.addEventListener('DOMContentLoaded', updateStarRatings);
function updateStarRatings() {
    const gradeDetails = document.querySelectorAll('.custom-grade-details');

    gradeDetails.forEach(function(detail) {
        const rating = parseFloat(detail.dataset.rating);
        const starsContainer = detail.querySelector('.stars');
        const ratingNumber = detail.querySelector('.rating-number');

        starsContainer.innerHTML = '';

        for (let i = 1; i <= 5; i++) {
            const star = document.createElement('i');

            if (i <= Math.floor(rating)) {
                star.className = 'fas fa-star';
            } else if (i - 0.8 < rating) {
                star.className = 'fas fa-star-half-alt';
            } else {
                star.className = 'far fa-star';
            }

            starsContainer.appendChild(star);
        }

        ratingNumber.textContent = rating.toFixed(1);
    });
}
const filterTemplate = document.getElementById('filter-template');
const sidebarContent = document.getElementById('sidebar-content');
const modalContent = document.getElementById('modal-content');

sidebarContent.appendChild(filterTemplate.content.cloneNode(true));
modalContent.appendChild(filterTemplate.content.cloneNode(true));

document.addEventListener('DOMContentLoaded', function() {
    const gridViewBtn = document.getElementById('grid-view');
    const listViewBtn = document.getElementById('list-view');
    const mangaContainer = document.querySelector('.manga-grid');

    mangaContainer.classList.add('hide-manga-details');
    gridViewBtn.classList.add('active');

    function applyViewSwitchAnimation() {
        mangaContainer.classList.add('switching');

        setTimeout(() => {
            mangaContainer.classList.remove('switching');
        }, 500);
    }

    gridViewBtn.addEventListener('click', function() {
        mangaContainer.classList.remove('manga-list');
        mangaContainer.classList.remove('hide-manga-details');
        gridViewBtn.classList.add('active');
        listViewBtn.classList.remove('active');

        applyViewSwitchAnimation();
    });

    listViewBtn.addEventListener('click', function() {
        mangaContainer.classList.add('manga-list');
        mangaContainer.classList.add('hide-manga-details');
        listViewBtn.classList.add('active');
        gridViewBtn.classList.remove('active');

        applyViewSwitchAnimation();
    });
});
$(document).ready(function() {
    let sortDirection = new URLSearchParams(window.location.search).get('direction') || 'desc';
    let currentSort = new URLSearchParams(window.location.search).get('sort') || 'byNew';

    function updateSortButton(direction) {
        const sortButton = $('#sort-up');
        if (direction === 'asc') {
            sortButton.removeClass('sort-desc').addClass('sort-asc');
            sortButton.find('i').removeClass('fa-arrow-up-wide-short').addClass('fa-arrow-down-wide-short');
        } else {
            sortButton.removeClass('sort-asc').addClass('sort-desc');
            sortButton.find('i').removeClass('fa-arrow-down-wide-short').addClass('fa-arrow-up-wide-short');
        }
    }

    function updateSortType(sortType) {
        $('#dropdownMenuText').text(sortType === 'byNew' ? 'За новинкою' : 'За рейтингом');
        $('.dropdown-menu-sortBy a').removeClass('active');
        $(`.dropdown-menu-sortBy a[data-sort="${sortType}"]`).addClass('active');
    }

    function loadManga(sort, direction, page = 1) {
        $.ajax({
            url: '/manga',
            data: { sort: sort, direction: direction, page: page },
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            success: function(data) {
                const mangaContainer = document.querySelector('.manga-grid');
                $('.manga-grid').html($(data).find('.manga-grid').html());
                $('.pagination').html($(data).find('.pagination').html());
                history.pushState(null, '', `/manga?sort=${sort}&direction=${direction}&page=${page}`);
                updateStarRatings();
                mangaContainer.classList.add('switching');
                setTimeout(() => {
                    mangaContainer.classList.remove('switching');
                }, 500);
            }
        });
    }

    updateSortButton(sortDirection);
    updateSortType(currentSort);

    $('#sort-up').on('click', function() {
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
        updateSortButton(sortDirection);
        loadManga(currentSort, sortDirection);
    });

    $('.dropdown-menu-sortBy a').on('click', function(event) {
        event.preventDefault();
        currentSort = $(this).data('sort');
        updateSortType(currentSort);
        loadManga(currentSort, sortDirection);
    });

    $(document).on('click', '.pagination a', function(event) {
        event.preventDefault();
        const page = $(this).text();
        loadManga(currentSort, sortDirection, page);
    });
});