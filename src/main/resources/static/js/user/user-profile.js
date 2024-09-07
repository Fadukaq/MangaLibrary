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

document.addEventListener('DOMContentLoaded', () => {
    const reportButtons = document.querySelectorAll('.report-user-btn');

    reportButtons.forEach(button => {
        button.addEventListener('click', () => {
            const reportedUserId = button.getAttribute('data-user-id');
            const reporterUserId = button.getAttribute('data-currentUser-id');
            document.getElementById('reportedUserId').value = reportedUserId;
            document.getElementById('reporterUserId').value = reporterUserId;
            const reportUserModal = new bootstrap.Modal(document.getElementById('reportUserModal'));
            reportUserModal.show();
        });
    });

    document.getElementById('submit-report-btn').addEventListener('click', () => {
        submitReportForm();
    });
});

function submitReportForm() {
    const form = document.getElementById('reportUserForm');
    const formData = new FormData(form);
    const url = form.action;
    const reason = document.getElementById('reportReasonProfile').value;
    const reportUserModal = bootstrap.Modal.getInstance(document.getElementById('reportUserModal'));
    const maxLength = 255;

    if (!reason || reason.trim() === '') {
        reportUserModal.hide();
        $('#errorMessage').text('Будь ласка, введіть причину скарги.');
        $('#errorModal').modal('show');
        return;
    } else if (reason.length > maxLength) {
        reportUserModal.hide();
        $('#errorMessage').text(`Причина занадто довга. Максимальна довжина: ${maxLength} символів.`);
        $('#errorModal').modal('show');
        $('#reportReasonProfile').val('');
        return;
    }
    document.getElementById('reportReasonProfileForm').value = reason;
    formData.set('reason', reason);

    fetch(url, {
        method: 'POST',
        body: formData,
    })
        .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            reportUserModal.hide();
            $('#errorMessage').text('Помилка надсилання звіту');
            $('#errorModal').modal('show');
            throw new Error('Помилка надсилання звіту');
        }
    })
        .then(data => {
        if (data.status === 'success') {
            reportUserModal.hide();
            $('#successMessage').text('Звіт успішно надіслано');
            $('#successModal').modal('show');
        } else {
            reportUserModal.hide();
            $('#errorMessage').text(data.message || 'Помилка надсилання звіту');
            $('#errorModal').modal('show');
        }
    })
        .catch(error => {
        reportUserModal.hide();
        $('#errorMessage').text('Помилка надсилання звіту');
        $('#errorModal').modal('show');
    }).finally(() => {
        document.getElementById('reportReasonProfile').value = '';
    });
}

function formatDate(dateString) {
    const options = { day: 'numeric', month: 'short', year: 'numeric' };
    const date = new Date(dateString);
    return date.toLocaleDateString('uk-UA', options);
}

let currentSort = 'sortByDate';
let isAscending = true;

function sortComments(sortBy) {
    const commentsList = document.getElementById('comments-list');
    if (!commentsList) {
        console.error('Comments list not found');
        return;
    }
    const comments = Array.from(commentsList.children);

    if (sortBy === currentSort) {
        isAscending = !isAscending;
    } else {
        isAscending = false;
        currentSort = sortBy;
    }

    comments.sort((a, b) => {
        let comparison = 0;
        if (sortBy === 'sortByDate') {
            const dateA = new Date(a.dataset.date);
            const dateB = new Date(b.dataset.date);
            if (isNaN(dateA.getTime()) || isNaN(dateB.getTime())) {
                console.error('Invalid date data:', a.dataset.date, b.dataset.date);
                return 0;
            }
            comparison = dateB - dateA;
        } else if (sortBy === 'sortByRating') {
            const ratingA = parseInt(a.dataset.rating);
            const ratingB = parseInt(b.dataset.rating);
            if (isNaN(ratingA) || isNaN(ratingB)) {
                console.error('Invalid rating data:', a.dataset.rating, b.dataset.rating);
                return 0;
            }
            comparison = ratingB - ratingA;
        }
        return isAscending ? -comparison : comparison;
    });
    comments.forEach(comment => commentsList.appendChild(comment));
    updateSortIcons();
}

function updateSortIcons() {
    const sortTabs = document.querySelectorAll('.sortBy .nav-link');
    sortTabs.forEach(tab => {
        const sortBy = tab.getAttribute('href').substring(1);
        const icon = tab.querySelector('i');
        if (icon) {
            if (sortBy === currentSort) {
                icon.style.display = 'inline-block';
                icon.classList.toggle('rotate-up', isAscending);
                icon.classList.toggle('rotate-down', !isAscending);
            } else {
                icon.style.display = 'none';
            }
        }
    });
}
function sortReplies() {
    const repliesList = document.getElementById('replies-list');
    if (!repliesList) {
        console.error('Replies list not found');
        return;
    }

    const replies = Array.from(repliesList.children);

    replies.sort((a, b) => {
        const dateA = new Date(a.dataset.date);
        const dateB = new Date(b.dataset.date);

        if (isNaN(dateA.getTime()) || isNaN(dateB.getTime())) {
            console.error('Invalid date data:', a.dataset.date, b.dataset.date);
            return 0;
        }
        let comparison = dateA - dateB;
        if (!isAscending) {
            comparison = -comparison;
        }
        return comparison;
    });
    replies.forEach(reply => repliesList.appendChild(reply));
}

document.addEventListener('DOMContentLoaded', () => {
    const sortTabs = document.querySelectorAll('.sortBy .nav-link');
    sortTabs.forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            const sortBy = e.target.closest('.nav-link').getAttribute('href').substring(1);
            sortComments(sortBy);
            sortReplies();
            sortTabs.forEach(t => t.classList.remove('active'));
            e.target.closest('.nav-link').classList.add('active');
        });
    });
    const commentDates = document.querySelectorAll('.comment-date');
    commentDates.forEach(dateElement => {
        const dateString = dateElement.closest('.comment-item').dataset.date;
        if (dateString) {
            dateElement.textContent = formatDate(dateString);
        }
    });
    sortComments('sortByDate');
});

document.addEventListener('DOMContentLoaded', function() {
    const toReplyTab = document.getElementById('comment-to-reply-tab');
    const ratingNavItem = document.getElementById('rating-nav-item');

    toReplyTab.addEventListener('shown.bs.tab', function(event) {
        ratingNavItem.classList.add('d-none');
    });

    const toMangaTab = document.getElementById('comment-manga-tab');

    toMangaTab.addEventListener('shown.bs.tab', function(event) {
        ratingNavItem.classList.remove('d-none');
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const replyDates = document.querySelectorAll('.reply-date');
    replyDates.forEach(dateElement => {
        const dateString = dateElement.closest('.reply-item').dataset.date;
        if (dateString) {
            dateElement.textContent = formatDate(dateString);
        }
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const items = Array.from(document.querySelectorAll('#replies-list .reply-item'));
    const loadingIndicator = document.getElementById('loading-indicator');
    const itemsPerPage = 4;
    let currentIndex = 0;
    let loading = false;
    function showMoreItems() {
        if (loading) return;
        loading = true;
        loadingIndicator.style.display = 'block';
        setTimeout(() => {
            const endIndex = Math.min(currentIndex + itemsPerPage, items.length);
            for (let i = currentIndex; i < endIndex; i++) {
                items[i].style.display = 'block';
            }
            currentIndex = endIndex;
            loadingIndicator.style.display = 'none';
            loading = false;

            if (currentIndex >= items.length) {
                window.removeEventListener('scroll', onScroll);
            }
        }, 500);
    }
    function onScroll() {
        if (!document.getElementById('toReply').classList.contains('show')) return;
        const scrollTop = window.scrollY || document.documentElement.scrollTop;
        const windowHeight = window.innerHeight;
        const documentHeight = document.documentElement.scrollHeight;
        if (scrollTop + windowHeight >= documentHeight - 100) {
            showMoreItems();
        }
    }
    function onTabChange(event) {
        if (event.target.getAttribute('href') === '#toReply') {
            showMoreItems();
        }
    }
    if (document.getElementById('toReply').classList.contains('show')) {
        showMoreItems();
    }
    document.querySelectorAll('a[data-bs-toggle="tab"]').forEach(tab => {
        tab.addEventListener('shown.bs.tab', onTabChange);
    });
    window.addEventListener('scroll', onScroll);
});