document.addEventListener('DOMContentLoaded', function() {
    $(document).ready(function() {
        $('#mangaStatus').select2({
            width: '100%',
            closeOnSelect: true,
            minimumResultsForSearch: Infinity
        });
        $('#adultContentSelect').select2({
            width: '100%',
            closeOnSelect: true,
            minimumResultsForSearch: Infinity
        });
    });

    $('#genreSelect').select2({
        placeholder: 'Оберіть жанри',
        allowClear: true,
        width: '100%',
        closeOnSelect: false,
        templateResult: function(genre) {
            if (!genre.id) {
                return genre.text;
            }

            var selectedGenres = $('#genreSelect').val();
            if (selectedGenres && selectedGenres.includes(genre.id)) {
                return null;
            }

            return genre.text;
        }
    });

    function checkIfAllGenresSelected() {
        var totalOptions = $('#genreSelect option').length - 1;
        var selectedGenres = $('#genreSelect').val() || [];

        if (selectedGenres.length === totalOptions) {
            $('#genreSelect').hide();
            $('#noGenresLeft').show();
        } else {
            $('#genreSelect').show();
            $('#noGenresLeft').hide();
        }
    }

    $('#genreSelect').on('select2:select select2:unselect', function() {
        $('#genreSelect').select2('close');
        $('#genreSelect').select2('open');
        checkIfAllGenresSelected();
    });

    checkIfAllGenresSelected();
});

document.addEventListener('DOMContentLoaded', function() {
    $('#authorSelect').select2({
        placeholder: 'Оберіть автора',
        allowClear: true,
        width: '100%'
    });

    var searchInput = document.getElementById('authorSearch');
    var selectElement = document.getElementById('authorSelect');

    if (searchInput && selectElement) {
        searchInput.addEventListener('input', function() {
            var filter = searchInput.value.toLowerCase();
            var options = selectElement.querySelectorAll('option');

            options.forEach(function(option) {
                var optionText = option.textContent.toLowerCase();
                option.style.display = optionText.includes(filter) ? 'block' : 'none';
            });
        });
    }
});

function previewImage(event, previewId, iconId) {
    const input = event.target;
    const file = input.files[0];
    const preview = document.getElementById(previewId);
    const icon = document.getElementById(iconId);

    if (file) {
        const reader = new FileReader();

        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            if(icon!=null)
            icon.style.display = 'none';
        }

        reader.readAsDataURL(file);
    } else {
        preview.src = '';
        preview.style.display = 'none';
        if(icon!=null)
        icon.style.display = 'block';
    }
}