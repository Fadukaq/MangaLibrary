document.addEventListener('DOMContentLoaded', function() {
    var tabList = [].slice.call(document.querySelectorAll('#mangaTabs a'));
    var tabContent = document.getElementById('mangaTabsContent');

    function activateTab(tabId) {
        var tab = document.querySelector('#mangaTabs a[href="#' + tabId + '"]');
        var tabContent = document.getElementById(tabId);

        if (tab && tabContent) {
            tabList.forEach(function(tab) {
                tab.classList.remove('active');
                tab.setAttribute('aria-selected', 'false');
            });

            [].slice.call(tabContent.parentElement.children).forEach(function(content) {
                content.classList.remove('show', 'active');
            });

            tab.classList.add('active');
            tab.setAttribute('aria-selected', 'true');
            tabContent.classList.add('show', 'active');
        }
    }

    tabList.forEach(function(tab) {
        tab.addEventListener('click', function(event) {
            event.preventDefault();
            var tabId = this.getAttribute('href').substring(1);
            history.pushState(null, null, '#' + tabId);
            activateTab(tabId);
        });
    });

    var hash = window.location.hash.substring(1);
    if (hash) {
        activateTab(hash);
    }

    window.addEventListener('popstate', function() {
        var hash = window.location.hash.substring(1);
        if (hash) {
            activateTab(hash);
        }
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const selects = $('#listTypeSelect, #listTypeSelect-mobile');
    const iconPreviews = ['#icon-preview', '#icon-preview-mobile'];

    const icons = {
        '': 'fa-folder',
        'reading': 'fa-book',
        'want-read': 'fa-calendar',
        'recited': 'fa-check',
        'read-stopped': 'fa-times'
    };

    function updateIcon(select, iconPreview) {
        const selectedValue = select.val();
        const iconClass = icons[selectedValue] || '';
        $(iconPreview).html(iconClass ? `<i class="fa-solid ${iconClass}"></i>` : '');
    }

    selects.each(function(index) {
        const selectElement = $(this);
        const iconPreview = iconPreviews[index];
        updateIcon(selectElement, iconPreview);

        selectElement.on('change', function() {
            updateIcon(selectElement, iconPreview);
        });
    });

    window.addEventListener('resize', function() {
        selects.each(function(index) {
            const selectElement = $(this);
            const iconPreview = iconPreviews[index];
            updateIcon(selectElement, iconPreview);
        });
    });
});
document.addEventListener('DOMContentLoaded', function() {
    const hash = window.location.hash;
    if (hash) {
        document.querySelector(`.nav-link[href="${hash}"]`).click();
    }
    document.querySelectorAll('#myTab .nav-link').forEach(link => {
        link.addEventListener('click', function() {
            history.replaceState(null, null, this.getAttribute('href'));
        });
    });
});

let deleteUrl = '';

function confirmDeleteManga(event, id) {
    event.preventDefault();
    deleteUrl = `/manga/delete/${id}`;
    $('#deleteConfirmationModal').modal('show');
}

function confirmDeleteComment(event, id) {
    event.preventDefault();
    deleteUrl = `/comment/delete/${id}`;
    $('#deleteConfirmationModal').modal('show');
}

$('#confirmDeleteButton').click(function() {
    if (deleteUrl) {
        window.location.href = deleteUrl;
        $('#deleteConfirmationModal').modal('hide');
        deleteUrl = '';
    }
});

$('#cancelDeleteButton').click(function() {
    deleteUrl = '';
    $('#deleteConfirmationModal').modal('hide');
});

document.addEventListener('DOMContentLoaded', function() {
    const shortDescription = document.getElementById('shortDescription');
    const fullDescription = document.getElementById('fullDescription');
    const button = document.getElementById('toggleDescriptionBtn');

    if (fullDescription.innerText.length > shortDescription.innerText.length) {
        shortDescription.style.display = 'block';
        fullDescription.style.display = 'none';
        button.style.display = 'block';

        button.addEventListener('click', function() {
            if (fullDescription.style.display === 'none') {
                shortDescription.style.display = 'none';
                fullDescription.style.display = 'block';
                button.innerText = 'Показати менше';
            } else {
                shortDescription.style.display = 'block';
                fullDescription.style.display = 'none';
                button.innerText = 'Показати більше';
            }
        });
    } else {
        button.style.display = 'none';
    }
});
document.addEventListener('DOMContentLoaded', function() {
    const items = Array.from(document.querySelectorAll('#comments-list .list-group-item-comments'));
    if(items.length > 0)
    {
        const loadingIndicator = document.getElementById('loading-indicator');
        const itemsPerPage = 4;
        let currentIndex = 0;
        let loading = false;

        function showMoreItems() {
            if (loading) return;
            loading = true;
            loadingIndicator.style.display = 'block';
            setTimeout(() => {
                const endIndex = Math.min(currentIndex + itemsPerPage, items.length);
                for (let i = currentIndex; i < endIndex; i++) {
                    items[i].style.display = 'block';
                }
                currentIndex = endIndex;
                loadingIndicator.style.display = 'none';
                loading = false;

                if (currentIndex >= items.length) {
                    window.removeEventListener('scroll', onScroll);
                }
            }, 500);
        }

        function onScroll() {
            const scrollTop = window.scrollY || document.documentElement.scrollTop;
            const windowHeight = window.innerHeight;
            const documentHeight = document.documentElement.scrollHeight;
            if (scrollTop + windowHeight >= documentHeight - 100) {
                showMoreItems();
            }
        }

        function onTabChange(event) {
            if (event.target.getAttribute('href') === '#comments') {
                showMoreItems();
            }
        }
        if (document.getElementById('comments').classList.contains('show')) {
            showMoreItems();
        }
        document.querySelectorAll('a[data-bs-toggle="tab"]').forEach(tab => {
            tab.addEventListener('shown.bs.tab', onTabChange);
        });
        window.addEventListener('scroll', onScroll);
        showMoreItems();
    }
});

document.addEventListener('DOMContentLoaded', function() {
    let deleteForm = null;
    document.querySelectorAll('[data-bs-toggle="modal"]').forEach(button => {
        button.addEventListener('click', function() {
            deleteForm = this.closest('form');
        });
    });

    document.getElementById('confirmDeleteButton').addEventListener('click', function() {
        if (deleteForm) {
            deleteForm.submit();
        }
    });
});
document.addEventListener('DOMContentLoaded', function() {
    function updateSortButtonState() {
        const comments = document.querySelectorAll('#comments-list .list-group-item-comments');
        const sortButton = document.getElementById('dropdownMenuButton');
        if (comments.length === 0) {
            sortButton.disabled = true;
            sortButton.classList.add('disabled');
        } else {
            sortButton.disabled = false;
            sortButton.classList.remove('disabled');
        }
    }
    updateSortButtonState();
});