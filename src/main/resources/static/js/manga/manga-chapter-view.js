$('#readStyle').select2({
    theme: 'default',
    minimumResultsForSearch: Infinity,
});
$('#pageStyle').select2({
    theme: 'default',
    minimumResultsForSearch: Infinity,
});

let lastScrollTop = 0;
const header = document.querySelector('.fixed-header');

window.addEventListener('scroll', function() {
    let currentScroll = window.pageYOffset || document.documentElement.scrollTop;

    if (currentScroll > lastScrollTop) {
        header.style.top = '-60px';
    } else {
        header.style.top = '0';
    }
    lastScrollTop = currentScroll <= 0 ? 0 : currentScroll;
});

