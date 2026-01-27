const express = require("express");
const bodyParser = require('body-parser');
const service = require('./service');
const mongoose = require('mongoose');

const app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

const port = process.env.PORT || 8080;

// connect to MongoDB (local) - use a project-specific DB name to avoid old data
const mongoUrl = process.env.MONGO_URL || "mongodb://127.0.0.1:27017/votingapp_piars";
mongoose.connect(mongoUrl, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});
mongoose.set('strictQuery', false);

const router = express.Router();

/*
  Session routes
*/
// root index for /api
router.get('/', (req, res) => {
  res.status(200).send({
    ok: true,
    message: 'API root',
    endpoints: [
      'GET /api/health',
      'GET /api/sessions',
      'POST /api/session',
      'POST /api/results/vote',
      'GET /api/votes?sessionId=...'
    ]
  });
});
// get all sessions
router.get('/sessions', service.getSessions);

// create new session
router.post('/session', service.createSession);

/*
  Voting / Results routes
*/
// submit anonymous vote
router.post('/results/vote', service.submitVote);

// get all votes (optionally filter by sessionId or sessionName query params)
router.get('/votes', service.getVotes);

app.use("/api", router);

// simple health endpoint to verify correct server instance
app.get('/api/health', (req, res) => {
  res.status(200).send({ ok: true, port, mongoUrl });
});

app.listen(port, () => {
  console.log('Voting server is running on port ' + port);
});
