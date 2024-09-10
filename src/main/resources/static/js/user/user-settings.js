$(document).ready(function() {
    $('#privacySelect').select2({
        theme: 'default',
        minimumResultsForSearch: Infinity,
    });

    $('#readStyle').select2({
        theme: 'default',
        minimumResultsForSearch: Infinity,
    });
    $('.select2').on('select2:open', function() {
        var $container = $(this).parent();
        var containerWidth = $container.outerWidth();
        $('.select2-dropdown').css('width', containerWidth + 'px');
    });
});

function previewImage(event, previewId, uploadIconId) {
    const file = event.target.files[0];
    const reader = new FileReader();

    reader.onload = function() {
        const preview = document.getElementById(previewId);
        preview.src = reader.result;
        preview.style.display = 'block';
        const uploadIcon = document.getElementById(uploadIconId);
        uploadIcon.style.display = 'none';
    };

    if (file) {
        reader.readAsDataURL(file);
    }
}