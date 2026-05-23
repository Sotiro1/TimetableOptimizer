/**
 * Represents an academic topic (subject/unit) available for enrolment.
 */
public class Topic {

    private String topicCode;
    private String topicName;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Topic() {}

    public Topic(String topicCode, String topicName) {
        this.topicCode = topicCode;
        this.topicName = topicName;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getTopicCode() { return topicCode; }
    public void setTopicCode(String topicCode) { this.topicCode = topicCode; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Topic{topicCode='" + topicCode + "', topicName='" + topicName + "'}";
    }
}
