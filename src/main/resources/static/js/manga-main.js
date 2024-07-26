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

    gridViewBtn.addEventListener('click', function() {
        mangaContainer.classList.remove('manga-list');
        mangaContainer.classList.remove('hide-manga-details');
        gridViewBtn.classList.add('active');
        listViewBtn.classList.remove('active');
    });

    listViewBtn.addEventListener('click', function() {
        mangaContainer.classList.add('manga-list');
        mangaContainer.classList.add('hide-manga-details');
        listViewBtn.classList.add('active');
        gridViewBtn.classList.remove('active');
    });
});