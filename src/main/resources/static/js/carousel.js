document.addEventListener('DOMContentLoaded', function() {
    const getItemsPerSlide = () => {
        if (window.innerWidth < 777) return 1;
        if (window.innerWidth < 1100) return 2;
        if (window.innerWidth < 1366) return 3;
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
                colDiv.classList.add('col-12', 'col-sm-6', 'col-md-4', 'col-lg-3', 'manga-card', 'new-manga-card');

                const mangaLink = document.createElement('a');
                mangaLink.href = `/manga/${manga.id}`;
                mangaLink.classList.add('manga-link');

                const mangaImage = document.createElement('div');
                mangaImage.classList.add('manga-image', 'manga-image-overlay');

                const mangaCover = document.createElement('img');
                mangaCover.src = manga.mangaPosterImg;
                mangaCover.alt = 'Manga Cover';
                mangaCover.classList.add('manga-cover');

                const mangaInfo = document.createElement('div');
                mangaInfo.classList.add('manga-info');

                const mangaTitle = document.createElement('h3');
                mangaTitle.classList.add('manga-title');
                mangaTitle.textContent = manga.mangaName;

                const mangaGenres = document.createElement('div');
                mangaGenres.classList.add('manga-genres');

                if (manga.genres && manga.genres.length > 0) {
                    manga.genres.slice(0, 2).forEach((genre, index) => {
                        const genreSpan = document.createElement('span');
                        genreSpan.textContent = genre.name;
                        mangaGenres.appendChild(genreSpan);

                        if (index === 0 && manga.genres.length > 1) {
                            const ellipsisSpan = document.createElement('span');
                            ellipsisSpan.textContent = ' - ';
                            mangaGenres.appendChild(ellipsisSpan);
                        }
                    });
                }

                mangaInfo.appendChild(mangaTitle);
                mangaInfo.appendChild(mangaGenres);
                mangaImage.appendChild(mangaCover);
                mangaImage.appendChild(mangaInfo);
                mangaLink.appendChild(mangaImage);
                colDiv.appendChild(mangaLink);
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
document.addEventListener('DOMContentLoaded', function() {
    const getItemsPerSlide = () => {
        if (window.innerWidth < 777) return 2;
        if (window.innerWidth < 992) return 4;
        if (window.innerWidth < 1366) return 6;
        return 8;
    };

    const updateLatestUpdatesCarousel = () => {
        const itemsPerSlide = getItemsPerSlide();
        const latestUpdatesList = window.latestUpdatesList || [];

        if (!latestUpdatesList || latestUpdatesList.length === 0) {
            console.error('No latest updates data found.');
            return;
        }

        const chunkedLatestUpdatesList = [];
        for (let i = 0; i < latestUpdatesList.length; i += itemsPerSlide) {
            chunkedLatestUpdatesList.push(latestUpdatesList.slice(i, i + itemsPerSlide));
        }

        const carouselInner = document.getElementById('latestUpdatesCarouselInner');
        carouselInner.innerHTML = '';

        chunkedLatestUpdatesList.forEach((chunk, index) => {
            const carouselItem = document.createElement('div');
            carouselItem.classList.add('carousel-item');
            if (index === 0) carouselItem.classList.add('active');

            const rowDiv = document.createElement('div');
            rowDiv.classList.add('row', 'justify-content-center');
            carouselItem.appendChild(rowDiv);

            chunk.forEach(update => {
                const colDiv = document.createElement('div');
                colDiv.classList.add('col-12', 'col-sm-6', 'col-md-4', 'col-lg-3', 'manga-card', 'latest-update-card');

                const mangaLink = document.createElement('a');
                mangaLink.href = `/manga/${update.mangaId}`;
                mangaLink.classList.add('manga-link');

                const mangaImage = document.createElement('div');
                mangaImage.classList.add('manga-image', 'manga-image-overlay');

                const mangaCover = document.createElement('img');
                mangaCover.src = update.mangaPosterImg;
                mangaCover.alt = 'Manga Cover';
                mangaCover.classList.add('manga-cover');

                const mangaInfo = document.createElement('div');
                mangaInfo.classList.add('manga-info');

                const mangaTitle = document.createElement('h3');
                mangaTitle.classList.add('manga-title');
                mangaTitle.textContent = update.mangaName;

                const chapterInfo = document.createElement('div');
                chapterInfo.classList.add('chapter-info');
                chapterInfo.textContent = `Новая глава: ${update.chapterTitle}`;

                const updateTime = document.createElement('div');
                updateTime.classList.add('update-time');
                updateTime.textContent = new Date(update.creationTime).toLocaleString();

                mangaInfo.appendChild(mangaTitle);
                mangaInfo.appendChild(chapterInfo);
                mangaInfo.appendChild(updateTime);
                mangaImage.appendChild(mangaCover);
                mangaImage.appendChild(mangaInfo);
                mangaLink.appendChild(mangaImage);
                colDiv.appendChild(mangaLink);
                rowDiv.appendChild(colDiv);
            });

            carouselInner.appendChild(carouselItem);
        });

        new bootstrap.Carousel(document.querySelector('#latestUpdatesCarousel'), {
            interval: 35000,
            wrap: true
        });
    };

    updateLatestUpdatesCarousel();
    window.addEventListener('resize', updateLatestUpdatesCarousel);
});