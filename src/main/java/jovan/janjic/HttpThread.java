package jovan.janjic;

import java.util.concurrent.CountDownLatch;

class HttpThread implements Runnable {
	private HttpHelper httpHelper;
	private CommandEnum commandEnum;
	private String jsonBody;
	private StringBuilder retVal;

	private CountDownLatch latch;

	HttpThread(HttpHelper httpHelper, CommandEnum commandEnum, String jsonBody, StringBuilder retVal, CountDownLatch latch) {
		this.httpHelper = httpHelper;
		this.commandEnum = commandEnum;
		this.jsonBody = jsonBody;
		this.retVal = retVal;
		this.latch = latch;
	}

	HttpThread(HttpHelper httpHelper, CommandEnum commandEnum, String jsonBody, StringBuilder retVal) {
		this.httpHelper = httpHelper;
		this.commandEnum = commandEnum;
		this.jsonBody = jsonBody;
		this.retVal = retVal;
		this.latch = null;
	}

	@Override
	public void run() {

		String response;
		switch (commandEnum)
		{
			case POST_SESSION:
				response = httpHelper.postSession(jsonBody);
				break;
			case GET_SESSIONS:
				response = httpHelper.getSessions();
				break;
			case POST_VOTE:
				response = httpHelper.postVote(jsonBody);
				break;
			case GET_VOTES:
				response = httpHelper.getVotes(jsonBody);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + commandEnum);
		}

		if(latch != null)
			latch.countDown();

		retVal.append(response);
	}
}



