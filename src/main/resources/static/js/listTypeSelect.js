document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('listTypeSelect').addEventListener('change', function() {
    document.getElementById('addMangaForm').submit();
    });
});
document.addEventListener('DOMContentLoaded', function() {
    var select = document.getElementById('listTypeSelect');

    var selectedOption = select.querySelector('option:checked');
    if (selectedOption) {
        selectedOption.hidden = true;
    }

    select.addEventListener('change', function() {
        if (selectedOption) {
            selectedOption.hidden = false;
        }
        selectedOption = select.querySelector('option:checked');
        if (selectedOption) {
            selectedOption.hidden = true;
        }
    });
});