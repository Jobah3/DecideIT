package jovan.janjic;

public class Session {

    private String date;
    private String sessionName;
    private String description;
    private String endOfVotingTime;


    private String title;
    private String status; // UPCOMING or PAST

    public Session(String title, String date, String status) {
        this.title = title;
        this.date = date;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getStatus() { return status; }


// Added by ChatGPT: constructor with 4 String parameters to match database fields
    public Session(String date, String sessionName, String description, String endOfVotingTime) {
        this.date = date;
        this.sessionName = sessionName;
        this.description = description;
        this.endOfVotingTime = endOfVotingTime;
    }

    public String getSessionName() { return sessionName; }
    public String getDescription() { return description; }
    public String getEndOfVotingTime() { return endOfVotingTime; }

}