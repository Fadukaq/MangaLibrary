function toggleEmail() {
    var emailShown = document.getElementById('emailShown');
    var emailToggle = document.getElementById('emailToggle');

    emailShown.classList.toggle('email-hidden');
    emailToggle.style.display = 'none';
}