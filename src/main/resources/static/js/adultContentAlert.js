document.addEventListener('DOMContentLoaded', function() {
    var myModal = new bootstrap.Modal(document.getElementById('adultContentModal'));
    myModal.show();

    document.getElementById('agreeButton').addEventListener('click', function() {
        var rememberChoice = document.getElementById('rememberChoice').checked;
        if (rememberChoice) {
            var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            fetch('/adult-content-agreement', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify({ agreement: true })
            })
                .then(response => response.json())
                .then(data => {
                if (data.success) {
                    myModal.hide();
                }
            })
        } else {
            myModal.hide();
        }
    });
});