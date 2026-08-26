// State
let eventsData = [];
let summaryData = [];

// Tab Switching
function switchTab(tabId, btnElement) {
    // Update buttons
    document.querySelectorAll('.nav-item').forEach(btn => btn.classList.remove('active'));
    btnElement.classList.add('active');

    // Update views
    document.querySelectorAll('.view').forEach(view => view.classList.remove('active'));
    document.getElementById(`view-${tabId}`).classList.add('active');
}

// Bottom Sheet
function openBottomSheet(title, contentHtml) {
    document.getElementById('sheet-title').innerText = title;
    document.getElementById('sheet-content').innerHTML = contentHtml;
    
    document.getElementById('bottom-sheet-backdrop').classList.add('show');
    document.getElementById('bottom-sheet').classList.add('show');
}

function closeBottomSheet() {
    document.getElementById('bottom-sheet-backdrop').classList.remove('show');
    document.getElementById('bottom-sheet').classList.remove('show');
}

// Data Fetching
async function fetchData() {
    try {
        const [summaryRes, eventsRes] = await Promise.all([
            fetch('/api/summary'),
            fetch('/api/events')
        ]);
        
        summaryData = await summaryRes.json();
        eventsData = await eventsRes.json();
        
        renderAppStats();
        renderTimeLog();
    } catch (error) {
        console.error("Error fetching data:", error);
    }
}

// Render App Stats
function renderAppStats() {
    const list = document.getElementById('app-stats-list');
    
    if (!Array.isArray(summaryData) || summaryData.length === 0) {
        list.innerHTML = '<div class="empty-state">No app data available.</div>';
        return;
    }

    let html = '';
    summaryData.forEach(app => {
        html += `
            <div class="card" onclick="showAppDetails('${app._id}', '${app.appName || app._id}')">
                <div>
                    <div class="card-title">${app.appName || app._id}</div>
                    <div class="card-subtitle">${app.totalWords} words typed</div>
                </div>
                <div class="card-value">${app.eventCount} logs</div>
            </div>
        `;
    });
    list.innerHTML = html;
}

// Render Time Log
function renderTimeLog() {
    const list = document.getElementById('time-log-list');
    
    if (!Array.isArray(eventsData) || eventsData.length === 0) {
        list.innerHTML = '<div class="empty-state">No recent events.</div>';
        return;
    }

    let html = '';
    eventsData.forEach(event => {
        const timeStr = new Date(event.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        const dateStr = new Date(event.timestamp).toLocaleDateString();
        
        // Stringify the event object to pass to click handler
        // using base64 or encodeURIComponent to avoid quote issues
        const eventJson = encodeURIComponent(JSON.stringify(event));

        html += `
            <div class="card" onclick="showEventDetails('${eventJson}')">
                <div>
                    <div class="card-title">${event.appName || event.appPackage}</div>
                    <div class="card-subtitle">${dateStr} • ${timeStr}</div>
                </div>
                <div class="card-value">${event.wordCount}W</div>
            </div>
        `;
    });
    list.innerHTML = html;
}

// Detail Views
window.showAppDetails = function(appPackage, appName) {
    const appEvents = eventsData.filter(e => e.appPackage === appPackage);
    
    let html = '';
    if (appEvents.length === 0) {
        html = '<div class="empty-state">No typing data recorded.</div>';
    } else {
        appEvents.forEach(event => {
            const timeStr = new Date(event.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', month: 'short', day: 'numeric'});
            html += `
                <div class="log-meta">${timeStr} • ${event.wordCount} words</div>
                <div class="log-message">${event.typedText || '(No text)'}</div>
            `;
        });
    }

    openBottomSheet(appName, html);
};

window.showEventDetails = function(eventJsonEncoded) {
    const event = JSON.parse(decodeURIComponent(eventJsonEncoded));
    const timeStr = new Date(event.timestamp).toLocaleString();
    
    const html = `
        <div class="log-meta">${timeStr} • ${event.wordCount} words</div>
        <div class="log-message">${event.typedText || '(No text)'}</div>
    `;

    openBottomSheet(event.appName || event.appPackage, html);
};

// Initial load
fetchData();

// Setup Socket.io
const socket = io();
socket.on('newData', () => {
    console.log("New data received, refreshing...");
    fetchData();
});
