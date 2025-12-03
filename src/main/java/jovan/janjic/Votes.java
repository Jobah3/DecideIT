package jovan.janjic;

class Votes {
	private final int yes;
	private final int no;
	private final int abstain;
	private final String sessionName;
	private final String sessionDate;

	Votes(int yes, int no, int abstain, String sessionName, String sessionDate) {
		this.yes = yes;
		this.no = no;
		this.abstain = abstain;
		this.sessionName = sessionName;
		this.sessionDate = sessionDate;
	}

	int getYes() { return yes; }
	int getNo() { return no; }
	int getAbstain() { return abstain; }
	String getSessionName() { return sessionName; }
	String getSessionDate() { return sessionDate; }
}



