const Session = require('./models/Session');
const Vote = require('./models/Vote');
const APIError = require('./APIError');

// Helper: check if two dates are same calendar day
function sameDay(d1, d2) {
  const a = new Date(d1);
  const b = new Date(d2);
  return a.getFullYear() === b.getFullYear() &&
         a.getMonth() === b.getMonth() &&
         a.getDate() === b.getDate();
}

/*
 * Create a new session
 * expected body: { date: <ISO date or timestamp>, sessionName: string, description?: string, endOfVotingTime: <ISO date> }
 */
exports.createSession = async function (req, res, next) {
  try {
    const body = req.body;
    if (!body || !body.date || !body.sessionName || !body.endOfVotingTime) {
      return res.status(400).send(APIError.NO_BODY);
    }

    const sessionDate = new Date(body.date);
    const endOfVoting = new Date(body.endOfVotingTime);

    if (isNaN(sessionDate.getTime()) || isNaN(endOfVoting.getTime())) {
      return res.status(400).send({ message: 'Invalid date format', code: 400 });
    }

    // endOfVoting must be after session date
    if (endOfVoting <= sessionDate) {
      return res.status(400).send({ message: 'endOfVotingTime must be after session date', code: 400 });
    }

    // Check: same sessionName on same calendar day is not allowed
    const existing = await Session.find();
    const conflict = existing.find(s => s.sessionName === body.sessionName && sameDay(s.date, sessionDate));
    if (conflict) {
      return res.status(409).send({ message: 'Session with same name already exists on this date', code: 409 });
    }

    const session = new Session({
      date: sessionDate,
      sessionName: body.sessionName,
      description: body.description || '',
      endOfVotingTime: endOfVoting
    });

    const saved = await session.save();

    // create initial Vote document for this session (counts zero)
    const vote = new Vote({
      sessionId: saved._id,
      sessionName: saved.sessionName,
      sessionDate: saved.date,
      yes: 0, no: 0, abstain: 0
    });
    await vote.save();

    return res.status(200).send({ message: 'Session created', session: saved });
  } catch (err) {
    console.error('createSession error:', err);
    return res.status(500).send({ message: 'Server error', code: 500 });
  }
};

/*
 * Get all sessions
 * GET /api/sessions
 */
exports.getSessions = async function (req, res, next) {
  try {
    const all = await Session.find().sort({ date: 1 });
    return res.status(200).send(all);
  } catch (err) {
    console.error('getSessions error:', err);
    return res.status(500).send({ message: 'Server error', code: 500 });
  }
};

/*
 * Submit an anonymous vote
 * expected body: { sessionId: <id> OR sessionName & sessionDate, vote: 'yes'|'no'|'abstain' }
 */
exports.submitVote = async function (req, res, next) {
  try {
    const body = req.body;
    if (!body || !body.vote) {
      return res.status(400).send(APIError.NO_BODY);
    }
    const voteType = body.vote;
    if (!['yes','no','abstain'].includes(voteType)) {
      return res.status(400).send({ message: 'Invalid vote type', code: 400 });
    }

    let voteDoc = null;

    if (body.sessionId) {
      voteDoc = await Vote.findOne({ sessionId: body.sessionId });
    } else if (body.sessionName && body.sessionDate) {
      const sd = new Date(body.sessionDate);
      voteDoc = await Vote.findOne({ sessionName: body.sessionName, sessionDate: sd });
    } else {
      return res.status(400).send({ message: 'Provide sessionId or (sessionName + sessionDate)', code: 400 });
    }

    if (!voteDoc) {
      return res.status(404).send({ message: 'Session votes not found', code: 404 });
    }

    // Check voting window: load session and compare current time vs endOfVotingTime
    const session = await Session.findById(voteDoc.sessionId);
    const now = new Date();
    if (!session) {
      return res.status(404).send({ message: 'Session not found', code: 404 });
    }
    if (now > session.endOfVotingTime) {
      return res.status(400).send({ message: 'Voting for this session is finished', code: 400 });
    }

    // Atomic increment
    const update = {};
    if (voteType === 'yes') update.$inc = { yes: 1 };
    if (voteType === 'no') update.$inc = { no: 1 };
    if (voteType === 'abstain') update.$inc = { abstain: 1 };
    update.$set = { updatedAt: new Date() };

    const updated = await Vote.findByIdAndUpdate(voteDoc._id, update, { new: true });

    return res.status(200).send({ message: 'Vote recorded', votes: updated });
  } catch (err) {
    console.error('submitVote error:', err);
    return res.status(500).send({ message: 'Server error', code: 500 });
  }
};

/*
 * Get votes
 * Optional query params: sessionId, sessionName
 * GET /api/votes?sessionId=...  or GET /api/votes?sessionName=... 
 */
exports.getVotes = async function (req, res, next) {
  try {
    const { sessionId, sessionName } = req.query;
    let query = {};
    if (sessionId) query.sessionId = sessionId;
    if (sessionName) query.sessionName = sessionName;

    const votes = await Vote.find(query).sort({ sessionDate: 1 });
    return res.status(200).send(votes);
  } catch (err) {
    console.error('getVotes error:', err);
    return res.status(500).send({ message: 'Server error', code: 500 });
  }
};
