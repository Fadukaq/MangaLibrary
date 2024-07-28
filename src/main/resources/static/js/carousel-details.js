document.addEventListener('DOMContentLoaded', function() {
    const getItemsPerSlide = () => {
        if (window.innerWidth < 576) return 1;
        if (window.innerWidth < 777) return 2;
        if (window.innerWidth < 1166) return 3;
        return 4;
    };

    const updateCarousel = () => {
        const itemsPerSlide = getItemsPerSlide();
        const mangaList = window.mangaList || [];

        if (!mangaList || mangaList.length === 0) {
            console.error('No manga data found.');
            return;
        }

        const chunkedMangaList = [];
        for (let i = 0; i < mangaList.length; i += itemsPerSlide) {
            chunkedMangaList.push(mangaList.slice(i, i + itemsPerSlide));
        }

        const carouselInner = document.getElementById('carouselInner');
        carouselInner.innerHTML = '';

        chunkedMangaList.forEach((chunk, index) => {
            const carouselItem = document.createElement('div');
            carouselItem.classList.add('carousel-item');
            if (index === 0) carouselItem.classList.add('active');

            const rowDiv = document.createElement('div');
            rowDiv.classList.add('row', 'justify-content-center');
            carouselItem.appendChild(rowDiv);

            chunk.forEach(manga => {
                const colDiv = document.createElement('div');
                colDiv.classList.add('col-12', 'col-sm-6', 'col-md-4', 'col-lg-3');

                const cardDiv = document.createElement('div');
                cardDiv.classList.add('manga-card');

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

                const ratingDiv = document.createElement('div');
                ratingDiv.classList.add('manga-rating');
                for (let i = 0; i < 5; i++) {
                    const star = document.createElement('span');
                    star.classList.add('star');
                    star.textContent = '★';
                    ratingDiv.appendChild(star);
                }

                textDiv.appendChild(cardTitle);
                textDiv.appendChild(genresDiv);
                textDiv.appendChild(ratingDiv);

                cardDiv.appendChild(img);
                cardDiv.appendChild(textDiv);

                colDiv.appendChild(cardDiv);
                rowDiv.appendChild(colDiv);
            });

            carouselInner.appendChild(carouselItem);
        });

        new bootstrap.Carousel(document.querySelector('#mangaCarousel'), {
            interval: 25000,
            wrap: true
        });
    };

    updateCarousel();
    window.addEventListener('resize', updateCarousel);
});
