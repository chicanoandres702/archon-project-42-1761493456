document.addEventListener('DOMContentLoaded', () => {
    const processButton = document.getElementById('processButton');
    const loadingIndicator = document.getElementById('loadingIndicator');
    const notificationArea = document.getElementById('notificationArea');

    const API_ENDPOINT = '/api/process_ai';

    // Helper function to prepare the UI for processing
    function showLoading() {
        processButton.disabled = true; // Disable button to prevent double submissions
        loadingIndicator.style.display = 'block'; // Show loading spinner
        
        // Clear previous notifications
        notificationArea.style.display = 'none';
        notificationArea.className = 'notification';
        notificationArea.innerHTML = '';
    }

    // Helper function to finalize the process
    function hideLoading() {
        processButton.disabled = false;
        loadingIndicator.style.display = 'none';
    }

    // Helper function to display the result
    function showNotification(status, message) {
        // Status should be 'success' or 'error'
        notificationArea.textContent = message;
        notificationArea.classList.add(status);
        notificationArea.style.display = 'block';
    }

    processButton.addEventListener('click', async () => {
        showLoading();

        try {
            const response = await fetch(API_ENDPOINT, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
                // No body needed for this simulation, but structure remains for future input data
            });

            const data = await response.json();

            if (response.ok) {
                // Server returned 200 (Success path)
                showNotification('success', data.message);
            } else {
                // Server returned 4xx or 5xx (Error path)
                showNotification('error', data.message || `Server returned status ${response.status}.`)
            }

        } catch (error) {
            console.error('Fetch error:', error);
            showNotification('error', 'Network failure: Could not communicate with the processing server.');
        } finally {
            hideLoading();
        }
    });
});