document.addEventListener('DOMContentLoaded', () => {
    const dropdownToggle = document.querySelector('.burger-menu .dropdown-toggle');
    const dropdownMenu = document.querySelector('.burger-menu .dropdown-menu');

    dropdownToggle.addEventListener('click', (event) => {
        event.preventDefault();
        dropdownMenu.classList.toggle('show');
    });
    document.addEventListener('click', (event) => {
        if (!dropdownToggle.contains(event.target) && !dropdownMenu.contains(event.target)) {
            dropdownMenu.classList.remove('show');
        }
    });
});
document.addEventListener('DOMContentLoaded', () => {
    const navbarToggler = document.querySelector('.navbar-toggler');
    const navbarContent = document.querySelector('#navbarContent');

    navbarToggler.addEventListener('click', () => {
        const isOpen = navbarContent.classList.contains('show');

        if (isOpen) {
            navbarContent.classList.remove('show');
        } else {
            navbarContent.classList.add('show');
        }
    });
});
