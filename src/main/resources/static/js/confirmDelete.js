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
document.addEventListener('DOMContentLoaded', function () {
    const deleteButtons = document.querySelectorAll('.btn-delete');

    deleteButtons.forEach(button => {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            const form = this.closest('form');
            const modal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));
            const confirmButton = document.getElementById('confirmDeleteButton');

            modal.show();

            confirmButton.onclick = function () {
                form.submit();
            };
        });
    });
});