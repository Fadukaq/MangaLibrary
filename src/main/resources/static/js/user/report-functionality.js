document.addEventListener('DOMContentLoaded', () => {
    const reportButtons = document.querySelectorAll('.report-user-btn');

    reportButtons.forEach(button => {
        button.addEventListener('click', () => {
            const reportedUserId = button.getAttribute('data-user-id');
            const reporterUserId = button.getAttribute('data-currentUser-id');
            document.getElementById('reportedUserId').value = reportedUserId;
            document.getElementById('reporterUserId').value = reporterUserId;
            const reportUserModal = new bootstrap.Modal(document.getElementById('reportUserModal'));
            reportUserModal.show();
        });
    });

    document.getElementById('submit-report-btn').addEventListener('click', () => {
        submitReportForm();
    });
});

function submitReportForm() {
    const form = document.getElementById('reportUserForm');
    const formData = new FormData(form);
    const url = form.action;
    const reason = document.getElementById('reportReasonProfile').value;
    const reportUserModal = bootstrap.Modal.getInstance(document.getElementById('reportUserModal'));
    const maxLength = 255;

    if (!reason || reason.trim() === '') {
        reportUserModal.hide();
        $('#errorMessage').text('Будь ласка, введіть причину скарги.');
        $('#errorModal').modal('show');
        return;
    } else if (reason.length > maxLength) {
        reportUserModal.hide();
        $('#errorMessage').text(`Причина занадто довга. Максимальна довжина: ${maxLength} символів.`);
        $('#errorModal').modal('show');
        $('#reportReasonProfile').val('');
        return;
    }
    document.getElementById('reportReasonProfileForm').value = reason;
    formData.set('reason', reason);

    fetch(url, {
        method: 'POST',
        body: formData,
    })
        .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            reportUserModal.hide();
            $('#errorMessage').text('Помилка надсилання звіту');
            $('#errorModal').modal('show');
            throw new Error('Помилка надсилання звіту');
        }
    })
        .then(data => {
        if (data.status === 'success') {
            reportUserModal.hide();
            $('#successMessage').text('Звіт успішно надіслано');
            $('#successModal').modal('show');
        } else {
            reportUserModal.hide();
            $('#errorMessage').text('Помилка надсилання звіту');
            $('#errorModal').modal('show');
        }
    })
        .catch(error => {
        reportUserModal.hide();
        $('#errorMessage').text('Помилка надсилання звіту');
        $('#errorModal').modal('show');
    }).finally(() => {
        document.getElementById('reportReasonProfile').value = '';
    });
}