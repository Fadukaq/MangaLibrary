let formToSubmit;

function openDeleteConfirmationModal(form) {
    formToSubmit = form;
    $('#deleteConfirmationModal').modal('show');
}

document.addEventListener('DOMContentLoaded', function () {
    const deleteButtons = document.querySelectorAll('.btn-delete');

    deleteButtons.forEach(button => {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            const form = this.previousElementSibling;

            if (form && form.classList.contains('delete-form')) {
                openDeleteConfirmationModal(form);
            } else {
                console.error('Форма для удаления не найдена');
            }
        });
    });

    document.getElementById('confirmDeleteButton').addEventListener('click', function () {
        if (formToSubmit) {
            formToSubmit.submit();
        } else {
            console.error('Форма для удаления не определена');
        }
    });
});