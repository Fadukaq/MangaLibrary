document.addEventListener('DOMContentLoaded', function() {
        const carouselElement = document.getElementById('mangaCarousel');
        const desktopCarousel = document.getElementById('desktopCarousel');
        const mobileCarousel = document.getElementById('mobileCarousel');
        let carousel;

        const initializeCarousel = () => {
            if (carousel) {
                carousel.dispose();
            }
            carousel = new bootstrap.Carousel(carouselElement, {
                wrap: true,
                interval: 9999999999
            });
        };

        const updateCarousel = () => {
            const isMobile = window.matchMedia("(max-width: 768px)").matches;

            if (isMobile) {
                desktopCarousel.style.display = 'none';
                mobileCarousel.style.display = 'block';
                carouselElement.innerHTML = mobileCarousel.innerHTML;
            } else {
                desktopCarousel.style.display = 'block';
                mobileCarousel.style.display = 'none';
                carouselElement.innerHTML = desktopCarousel.innerHTML;
            }

            initializeCarousel();
        };

        updateCarousel();
        window.addEventListener('resize', updateCarousel);
    });

document.addEventListener('DOMContentLoaded', function() {
    const wrapper = document.querySelector('.genre-wrapper');
    const slides = document.querySelectorAll('.genre-slide');
    const prevBtn = document.querySelector('.genre-control.prev');
    const nextBtn = document.querySelector('.genre-control.next');
    let currentIndex = 0;

    function updateCarousel() {
        const slideWidth = slides[0].offsetWidth;
        wrapper.style.transform = `translateX(-${currentIndex * slideWidth}px)`;
    }

    function showNextSlide() {
        currentIndex = (currentIndex + 1) % slides.length;
        updateCarousel();
    }

    function showPrevSlide() {
        currentIndex = (currentIndex - 1 + slides.length) % slides.length;
        updateCarousel();
    }

    nextBtn.addEventListener('click', showNextSlide);
    prevBtn.addEventListener('click', showPrevSlide);

    updateCarousel();
});