async function fetchSummary() {
    try {
        const res = await fetch('/api/summary');
        const data = await res.json();
        
        let totalWords = 0;
        let totalEvents = 0;
        let html = '';

        data.forEach(app => {
            totalWords += app.totalWords;
            totalEvents += app.eventCount;
            
            html += `
                <div class="app-row">
                    <div>
                        <div class="app-name">${app.appName || app._id}</div>
                        <div class="app-pkg">${app._id}</div>
                    </div>
                    <div class="app-count">${app.totalWords} words</div>
                </div>
            `;
        });

        document.getElementById('totalWords').innerText = totalWords;
        document.getElementById('totalEvents').innerText = totalEvents;
        document.getElementById('appList').innerHTML = html || '<p>No app data available.</p>';
    } catch (error) {
        console.error('Error fetching summary:', error);
        document.getElementById('appList').innerHTML = '<p class="error">Failed to load app data.</p>';
    }
}

async function fetchRecent() {
    try {
        const res = await fetch('/api/events');
        const events = await res.json();
        
        let html = '';

        events.forEach(event => {
            const date = new Date(event.timestamp).toLocaleString();
            html += `
                <div class="recent-row">
                    <div class="recent-header">
                        <div class="app-name">${event.appName}</div>
                        <div class="recent-time">${date} • ${event.wordCount} words</div>
                    </div>
                    ${event.typedText ? `<div class="recent-text">${event.typedText}</div>` : ''}
                </div>
            `;
        });

        document.getElementById('recentList').innerHTML = html || '<p>No recent events.</p>';
    } catch (error) {
        console.error('Error fetching events:', error);
        document.getElementById('recentList').innerHTML = '<p class="error">Failed to load recent events.</p>';
    }
}

// Initial load
fetchSummary();
fetchRecent();

// Auto refresh every 30 seconds
setInterval(() => {
    fetchSummary();
    fetchRecent();
}, 30000);
