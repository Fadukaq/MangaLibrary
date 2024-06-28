fetch('/api/reset-password', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({ email: email })
})
    .then(response => {
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.text();
})
    .then(text => {
    console.log('Raw response:', text);
    try {
        const data = JSON.parse(text);
        console.log('Parsed data:', data);

        const errorMessage = document.getElementById('errorMessage');
        if (data.message.includes('found')) {
            errorMessage.textContent = data.message;
            errorMessage.style.color = 'green';
        } else {
            errorMessage.textContent = data.message;
            errorMessage.style.color = 'red';
        }
        errorMessage.style.display = 'block';
    } catch (e) {
        console.error('Error parsing JSON:', e);
        throw new Error('Invalid JSON response');
    }
})
    .catch(error => {
    console.error('Error during fetch:', error);
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = 'Error during password reset. Please try again later.';
    errorMessage.style.color = 'red';
    errorMessage.style.display = 'block';
});