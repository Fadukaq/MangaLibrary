$(document).ready(function() {
    $('#search').on('input', function() {
        var searchText = $(this).val().toLowerCase();

        $('.tab-pane.active .card_manga').each(function() {
            var $card = $(this);
            var cardText = $card.find('.card-title').text().toLowerCase() + ' ' +
            $card.find('.card-text[style*="color:#BCA3D3"]').text().toLowerCase() + ' ' +
            $card.find('.card-text:last').text().toLowerCase();

            if (cardText.includes(searchText)) {
                $card.show();
            } else {
                $card.hide();
            }
        });

        var visibleCards = $('.tab-pane.active .card_manga:visible');
        var $noResultsMessage = $('.tab-pane.active #noResultsMessage');

        if (visibleCards.length === 0) {
            $noResultsMessage.show();
        } else {
            $noResultsMessage.hide();
        }
    });
});