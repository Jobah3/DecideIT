const mongoose = require('mongoose');

const SessionSchema = new mongoose.Schema({
  date: { type: Date, required: true }, // date/time of session
  sessionName: { type: String, required: true },
  description: { type: String, default: '' },
  endOfVotingTime: { type: Date, required: true },
  createdAt: { type: Date, default: Date.now }
});

// Ensure unique sessionName per date (same calendar day and name not allowed).
// We'll enforce this logic in service when creating (easier than complex index)
module.exports = mongoose.model('Session', SessionSchema);
