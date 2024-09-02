document.addEventListener('DOMContentLoaded', function() {
    const leftBlock = document.getElementById('leftBlock');
    const rightBlock = document.getElementById('rightBlock');
    const threshold = 950;
    const topOffset = 10;

    function updateLayout() {
        const scrollPosition = window.scrollY;

        if (scrollPosition > threshold) {
            leftBlock.style.width = '0';
            leftBlock.style.opacity = '0';
            rightBlock.style.marginLeft = '0';
            setTimeout(() => {
                rightBlock.classList.add('sticky');
            }, 100);
        } else {
            leftBlock.style.width = '210px';
            leftBlock.style.opacity = '1';
            rightBlock.style.marginLeft = '';
            setTimeout(() => {
                rightBlock.classList.remove('sticky');
            }, 100);
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
    window.addEventListener('scroll', updateLayout);
    updateLayout();
});