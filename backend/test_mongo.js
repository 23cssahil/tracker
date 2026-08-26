const mongoose = require('mongoose');

const uri = "mongodb+srv://23cssahil_db_user:xsBXlihiFfWrsEZY@cluster0.pmn7via.mongodb.net/tracker_data?retryWrites=true&w=majority&appName=Cluster0";

mongoose.connect(uri, {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => {
  console.log("Connected successfully!");
  process.exit(0);
}).catch(err => {
  console.error("Connection failed:", err.message);
  process.exit(1);
});
