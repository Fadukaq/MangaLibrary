function toggleNewPasswordField() {
    var checkbox = document.getElementById('changePasswordCheckbox');
    var newPasswordField = document.getElementById('newPasswordField').querySelector('input');

    if (checkbox.checked) {
        newPasswordField.removeAttribute('disabled');
        document.getElementById('newPasswordField').style.display = 'block';
    } else {
        newPasswordField.setAttribute('disabled', 'disabled');
        document.getElementById('newPasswordField').style.display = 'none';
    }
}