document.addEventListener('DOMContentLoaded', function() {
    const getItemsPerSlide = () => {
        if (window.innerWidth < 576) return 1;
        if (window.innerWidth < 777) return 2;
        if (window.innerWidth < 1166) return 3;
        return 4;
    };

    const updateCarousel = (carouselId, mangaList) => {
        const itemsPerSlide = getItemsPerSlide();
        if (!mangaList || mangaList.length === 0) {
            return;
        }

        mangaList.sort((a, b) => b.averageRating - a.averageRating);

        const chunkedMangaList = [];
        for (let i = 0; i < mangaList.length; i += itemsPerSlide) {
            chunkedMangaList.push(mangaList.slice(i, i + itemsPerSlide));
        }

        const carouselInner = document.getElementById(`${carouselId}Inner`);
        carouselInner.innerHTML = '';

        chunkedMangaList.forEach((chunk, index) => {
            const carouselItem = document.createElement('div');
            carouselItem.classList.add('carousel-item');
            if (index === 0) carouselItem.classList.add('active');

            const rowDiv = document.createElement('div');
            rowDiv.classList.add('row', 'justify-content-start');
            carouselItem.appendChild(rowDiv);

            chunk.forEach(manga => {
                const colDiv = document.createElement('div');
                colDiv.classList.add('col-12', 'col-sm-6', 'col-md-4', 'col-lg-3');

                const link = document.createElement('a');
                link.href = `/manga/${manga.id}`;
                link.classList.add('d-block', 'text-decoration-none');
                link.style.color = 'inherit';
                const cardDiv = document.createElement('div');
                cardDiv.classList.add('manga-card', 'mb-3');

                const img = document.createElement('img');
                img.src = manga.mangaPosterImg;
                img.alt = 'IMG';
                img.classList.add('manga-img');

                const textDiv = document.createElement('div');
                textDiv.classList.add('manga-text');

                const cardTitle = document.createElement('h5');
                cardTitle.classList.add('manga-title');
                cardTitle.textContent = manga.mangaName;

                const genresDiv = document.createElement('div');
                genresDiv.classList.add('manga-genres');
                if (manga.genres) {
                    manga.genres.slice(0, 2).forEach(genre => {
                        const genreTag = document.createElement('span');
                        genreTag.classList.add('genre-tag');
                        genreTag.textContent = genre.genreName || genre.name;
                        genresDiv.appendChild(genreTag);
                    });
                }

                const ratingDiv = createRatingStars(manga.averageRating);

                cardDiv.appendChild(img);
                textDiv.appendChild(cardTitle);
                textDiv.appendChild(genresDiv);
                textDiv.appendChild(ratingDiv);
                cardDiv.appendChild(textDiv);

                link.appendChild(cardDiv);

                colDiv.appendChild(link);
                rowDiv.appendChild(colDiv);
            });

            carouselInner.appendChild(carouselItem);
        });

        new bootstrap.Carousel(document.querySelector(`#${carouselId}`), {
            interval: 25000,
            wrap: true
        });
    };

    updateCarousel('mangaCarouselRelated', window.relatedMangas || []);
    updateCarousel('mangaCarouselSimilar', window.similarMangas || []);

    window.addEventListener('resize', () => {
        updateCarousel('mangaCarouselRelated', window.relatedMangas || []);
        updateCarousel('mangaCarouselSimilar', window.similarMangas || []);
    });
});

function createRatingStars(rating) {
    const maxRating = 5;
    const ratingDiv = document.createElement('div');
    ratingDiv.classList.add('manga-rating', 'mb-2');

    for (let i = 1; i <= maxRating; i++) {
        const star = document.createElement('span');
        star.classList.add('star');

        if (i <= Math.floor(rating)) {
            star.className = 'fas fa-star';
        } else if (i - 0.8 < rating) {
            star.className = 'fas fa-star-half-alt';
        } else {
            star.className = 'far fa-star';
        }

        ratingDiv.appendChild(star);
    }

    return ratingDiv;
}
