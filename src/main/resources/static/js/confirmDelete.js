let formToSubmit;

function openDeleteConfirmationModal(form) {
    formToSubmit = form;
    $('#deleteConfirmationModal').modal('show');
}

document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.delete-author').forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            const form = this.closest('form');
            openDeleteConfirmationModal(form);
        });
    });

    document.getElementById('confirmDeleteButton').addEventListener('click', function() {
        if (formToSubmit) {
            formToSubmit.submit();
        }
    });
});