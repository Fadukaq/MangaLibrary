$(document).ready(function() {
    var $noResultsMessage = $('#noResultsMessage');

    $('#search').on('input', function() {
        var searchTerm = $(this).val().toLowerCase();
        var activeTab = $('.tab-pane.active');
        var hasResults = false;

        activeTab.find('.card_manga_container').each(function() {
            var $this = $(this);
            var mangaTitle = $this.find('.card-title').text().toLowerCase();
            var mangaAuthor = $this.find('.card-text[style*="color:#BCA3D3"]').text().toLowerCase();
            var mangaDescription = $this.find('.card-text:last').text().toLowerCase();

            if (mangaTitle.includes(searchTerm) ||
            mangaAuthor.includes(searchTerm) ||
            mangaDescription.includes(searchTerm)) {
                $this.show();
                hasResults = true;
            } else {
                $this.hide();
            }
        });

        if (!hasResults) {
            if (!activeTab.find('#noResultsMessage').length) {
                activeTab.append($noResultsMessage);
            }
            $noResultsMessage.show();
        } else {
            $noResultsMessage.hide();
        }
    });

    $('.nav-link').on('click', function() {
        $('#search').val('');
        $('.card_manga_container').show();
        $noResultsMessage.hide();
    });
});