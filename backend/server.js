const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const dotenv = require('dotenv');
const path = require('path');

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

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
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
