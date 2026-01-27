const mongoose = require('mongoose');

const VoteSchema = new mongoose.Schema({
  sessionId: { type: mongoose.Schema.Types.ObjectId, ref: 'Session', required: true },
  sessionName: { type: String, required: true },
  sessionDate: { type: Date, required: true },

  yes: { type: Number, default: 0 },
  no: { type: Number, default: 0 },
  abstain: { type: Number, default: 0 },

  // optional: track when votes were last updated
  updatedAt: { type: Date, default: Date.now }
});

// We'll maintain one Vote document per session (create when session created, or on first vote)
module.exports = mongoose.model('Vote', VoteSchema);
