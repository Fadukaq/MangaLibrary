document.addEventListener('DOMContentLoaded', function () {
    const feedbackContainer = document.querySelector('.feedback-container');
    const voteForms = document.querySelectorAll('.vote-buttons form');
    const ratingScore = document.querySelector('.rating-score');
    const newsId = feedbackContainer.getAttribute('data-news-id');

    function setActiveButton(userRating) {
        voteForms.forEach(form => {
            const button = form.querySelector('button');
            const delta = form.querySelector('input[name="delta"]').value;

            if (parseInt(delta) === userRating) {
                button.classList.add('active');
            } else {
                button.classList.remove('active');
            }
        });
    }

    function updateRatingInfo() {
        fetch(`/news/rating-info/${newsId}`)
            .then(response => response.json())
            .then(data => {
            ratingScore.textContent = data.total;
            ratingScore.title = `Плюсів: ${data.positive} | Мінусів: ${data.negative}`;
        })
            .catch(error => console.error('Error:', error));
    }

    updateRatingInfo();

    if (newsId) {
        fetch(`/news/user-rating?newsId=${newsId}`)
            .then(response => response.json())
            .then(userRating => {
            setActiveButton(userRating);
        })
            .catch(error => console.error('Помилка:', error));

        voteForms.forEach(form => {
            form.addEventListener('submit', function (event) {
                event.preventDefault();

                const formData = new FormData(form);

                fetch(form.action, {
                    method: 'POST',
                    body: formData,
                })
                    .then(response => response.json())
                    .then(data => {
                    updateRatingInfo();
                    setActiveButton(data.userRating);
                })
                    .catch(error => {
                    console.error('Помилка:', error);
                });
            });
        });
    }
});