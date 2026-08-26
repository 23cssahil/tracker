const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const dotenv = require('dotenv');
const path = require('path');
const http = require('http');
const { Server } = require('socket.io');

dotenv.config();

const app = express();
const server = http.createServer(app);
const io = new Server(server, { cors: { origin: '*' } });
app.use(cors());
app.use(express.json());

const basicAuth = require('express-basic-auth');

// Define basic auth for dashboard and GET APIs
const password = process.env.DASHBOARD_PASSWORD || 'test@122333';
const authMiddleware = basicAuth({
    users: { 'admin': password },
    challenge: true,
    realm: 'TypingTracker'
});

app.use((req, res, next) => {
    // Socket.io handles its own connections, basic auth can interfere with WebSocket upgrades
    if (req.path.startsWith('/socket.io/')) {
        return next();
    }
    // Protect all GET routes (Dashboard UI + API reads)
    if (req.method === 'GET') {
        return authMiddleware(req, res, next);
    }
    next();
});

// Serve static dashboard files
app.use(express.static(path.join(__dirname, 'public')));

// MongoDB connection
const MONGODB_URI = process.env.MONGODB_URI;

if (MONGODB_URI) {
  mongoose.connect(MONGODB_URI).then(() => {
    console.log("Connected to MongoDB!");
  }).catch(err => {
    console.error("MongoDB connection error:", err);
  });
} else {
  console.log("MONGODB_URI is not set. Dashboard will not be able to fetch data.");
}

// Mongoose Schema
const TypingEventSchema = new mongoose.Schema({
  appPackage: String,
  appName: String,
  wordCount: Number,
  charCount: Number,
  timestamp: Number,
  hour: Number,
  dateString: String,
  typedText: String,
  syncedAt: { type: Date, default: Date.now }
});

const TypingEvent = mongoose.model('TypingEvent', TypingEventSchema);

// API Routes
app.post('/api/events', async (req, res) => {
  try {
    const events = req.body; // Expecting an array of events
    if (!Array.isArray(events)) {
      return res.status(400).json({ error: "Expected an array of events" });
    }
    await TypingEvent.insertMany(events);
    io.emit('newData');
    res.status(200).json({ success: true, count: events.length });
  } catch (error) {
    console.error("Error saving events:", error);
    res.status(500).json({ error: "Failed to save events" });
  }
});

app.get('/api/events', async (req, res) => {
  try {
    const events = await TypingEvent.find().sort({ timestamp: -1 }).limit(1000);
    res.status(200).json(events);
  } catch (error) {
    res.status(500).json({ error: "Failed to fetch events" });
  }
});

app.get('/api/summary', async (req, res) => {
  try {
    const summary = await TypingEvent.aggregate([
      {
        $group: {
          _id: "$appPackage",
          appName: { $first: "$appName" },
          totalWords: { $sum: "$wordCount" },
          eventCount: { $sum: 1 }
        }
      },
      { $sort: { totalWords: -1 } }
    ]);
    res.status(200).json(summary);
  } catch (error) {
    res.status(500).json({ error: "Failed to fetch summary" });
  }
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
