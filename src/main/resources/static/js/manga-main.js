document.addEventListener('DOMContentLoaded', function() {
    const filterBtn = document.getElementById('filter-btn');
    const filterModal = document.getElementById('filter-modal');
    const closeBtn = document.getElementById('close-btn');
    const body = document.body;

    filterBtn.addEventListener('click', openModal);
    closeBtn.addEventListener('click', closeModal);

    function openModal() {
        filterModal.style.right = '0';
        createOverlay();
        body.style.overflow = 'hidden';
        filterModal.setAttribute('aria-hidden', 'false');
    }

    function closeModal() {
        filterModal.style.right = '-100%';
        removeOverlay();
        body.style.overflow = '';
        filterModal.setAttribute('aria-hidden', 'true');
    }

    function createOverlay() {
        const overlay = document.createElement('div');
        overlay.className = 'overlay';
        document.body.appendChild(overlay);
        overlay.addEventListener('click', closeModal);
    }

    function removeOverlay() {
        const overlay = document.querySelector('.overlay');
        if (overlay) {
            overlay.remove();
        }
    }

    function updateStarRatings() {
        const gradeDetails = document.querySelectorAll('.custom-grade-details');

        gradeDetails.forEach(detail => {
            const rating = parseFloat(detail.dataset.rating);
            const starsContainer = detail.querySelector('.stars');
            const ratingNumber = detail.querySelector('.rating-number');

            starsContainer.innerHTML = '';
            for (let i = 1; i <= 5; i++) {
                const star = document.createElement('i');
                star.className = i <= Math.floor(rating) ? 'fas fa-star'
                : (i - 0.8 < rating ? 'fas fa-star-half-alt' : 'far fa-star');
                starsContainer.appendChild(star);
            }
            ratingNumber.textContent = rating.toFixed(1);
        });
    }

    updateStarRatings();

    function initializeViewSwitch() {
        const gridViewBtn = document.getElementById('grid-view');
        const listViewBtn = document.getElementById('list-view');
        const mangaContainer = document.querySelector('.manga-grid');

        mangaContainer.classList.add('hide-manga-details');
        gridViewBtn.classList.add('active');

        function applyViewSwitchAnimation() {
            mangaContainer.classList.add('switching');
            setTimeout(() => mangaContainer.classList.remove('switching'), 500);
        }

        gridViewBtn.addEventListener('click', () => {
            mangaContainer.classList.remove('manga-list', 'hide-manga-details');
            gridViewBtn.classList.add('active');
            listViewBtn.classList.remove('active');
            applyViewSwitchAnimation();
        });

        listViewBtn.addEventListener('click', () => {
            mangaContainer.classList.add('manga-list', 'hide-manga-details');
            listViewBtn.classList.add('active');
            gridViewBtn.classList.remove('active');
            applyViewSwitchAnimation();
        });
    }
    initializeViewSwitch();
    let sortDirection = new URLSearchParams(window.location.search).get('direction') || 'desc';
    let currentSort = new URLSearchParams(window.location.search).get('sort') || 'byNew';

    function updateSortButton(direction) {
        const sortButton = $('#sort-up');
        sortButton.toggleClass('sort-asc', direction === 'asc')
            .toggleClass('sort-desc', direction === 'desc');
        sortButton.find('i').toggleClass('fa-arrow-up-wide-short', direction === 'desc')
            .toggleClass('fa-arrow-down-wide-short', direction === 'asc');
    }

    function updateSortType(sortType) {
        $('#dropdownMenuText').text(sortType === 'byNew' ? 'За новинкою' : 'За рейтингом');
        $('.dropdown-menu-sortBy a').removeClass('active');
        $(`.dropdown-menu-sortBy a[data-sort="${sortType}"]`).addClass('active');
    }

    function loadManga(sort, direction, page = 1) {
        $.ajax({
            url: '/manga',
            data: { sort, direction, page },
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            success: function(data) {
                window.scrollTo({ top: 0, behavior: 'smooth' });
                setTimeout(() => {
                $('.manga-grid').html($(data).find('.manga-grid').html());
                $('.manga-grid').addClass('switching');
                $('.pagination').html($(data).find('.pagination').html());
                history.pushState(null, '', `/manga?sort=${sort}&direction=${direction}&page=${page}`);
                updateStarRatings();
                    setTimeout(() => {
                        $('.manga-grid').removeClass('switching');
                    }, 800);
                }, 800);
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

    const sidebar = document.querySelector('.sidebar');
    const footer = document.querySelector('footer');
    const initialTopOffset = 100;

    function updateSidebarPosition() {
        const scrollPosition = window.pageYOffset;
        const footerTop = footer.offsetTop;
        const sidebarHeight = sidebar.offsetHeight;
        const maxTop = footerTop - sidebarHeight - 20;

        if (scrollPosition + initialTopOffset > maxTop) {
            sidebar.style.position = 'absolute';
            sidebar.style.top = `${maxTop}px`;
        } else {
            sidebar.style.position = 'fixed';
            sidebar.style.top = `${initialTopOffset}px`;
        }
    }

    window.addEventListener('scroll', updateSidebarPosition);
    window.addEventListener('resize', updateSidebarPosition);
    updateSidebarPosition();

    window.updateStarRatings = updateStarRatings;
    window.initializeViewSwitch = initializeViewSwitch;
});

