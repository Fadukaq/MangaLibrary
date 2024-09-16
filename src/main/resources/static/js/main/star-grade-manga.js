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