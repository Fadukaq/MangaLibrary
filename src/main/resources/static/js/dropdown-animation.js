document.addEventListener('DOMContentLoaded', function() {
    const dropdowns = document.querySelectorAll('.admin-dropdown, .site-dropdown');

    dropdowns.forEach(dropdown => {
        dropdown.addEventListener('show.bs.dropdown', function () {
            this.querySelector('.dropdown-arrow').style.transform = 'rotate(90deg)';
        });

        dropdown.addEventListener('hide.bs.dropdown', function () {
            this.querySelector('.dropdown-arrow').style.transform = 'rotate(0deg)';
        });
    });
});