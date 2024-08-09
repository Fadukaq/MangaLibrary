let formToSubmit;

function openDeleteConfirmationModal(form) {
    formToSubmit = form;
    $('#deleteConfirmationModal').modal('show');
}

$('#confirmDeleteButton').click(function() {
    if (formToSubmit) {
        formToSubmit.submit();
    }
});

$('#deleteAuthorForm').on('submit', function(event) {
    event.preventDefault();
    openDeleteConfirmationModal(this);
});