import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific instance of a class offering for a topic.
 * A topic may have multiple class instances (e.g. different tutorial groups).
 */
public class ClassInstance {

    private String topicCode;
    private String topicName;
    private Topic topic;
    private String attendanceMode;
    private String campus;
    private String semester;
    private int availabilityNumber;
    private String classFormat;       // e.g. "Lecture", "Tutorial", "Practical"
    private int instanceNumber;       // distinguishes multiple instances of the same format

    private List<Session> sessions;   // sessions belonging to this class instance

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ClassInstance() {
        this.sessions = new ArrayList<>();
    }

    public ClassInstance(String attendanceMode, String campus, String semester,
                         int availabilityNumber, String classFormat, int instanceNumber) {
        this.attendanceMode    = attendanceMode;
        this.campus            = campus;
        this.semester          = semester;
        this.availabilityNumber = availabilityNumber;
        this.classFormat       = classFormat;
        this.instanceNumber    = instanceNumber;
        this.sessions          = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getTopicCode() { return topicCode; }
    public void setTopicCode(String topicCode) { this.topicCode = topicCode; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getAttendanceMode() { return attendanceMode; }
    public void setAttendanceMode(String attendanceMode) { this.attendanceMode = attendanceMode; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) {
        this.topic = topic;
        if (topic != null) {
            this.topicCode = topic.getTopicCode();
            this.topicName = topic.getTopicName();
        }
    }

    public int getAvailabilityNumber() { return availabilityNumber; }
    public void setAvailabilityNumber(int availabilityNumber) { this.availabilityNumber = availabilityNumber; }

    public String getClassFormat() { return classFormat; }
    public void setClassFormat(String classFormat) { this.classFormat = classFormat; }

    public int getInstanceNumber() { return instanceNumber; }
    public void setInstanceNumber(int instanceNumber) { this.instanceNumber = instanceNumber; }

    public List<Session> getSessions() { return sessions; }
    public void setSessions(List<Session> sessions) { this.sessions = sessions; }

    public void addSession(Session session) { this.sessions.add(session); }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "ClassInstance{" +
                "topicCode='" + topicCode + '\'' +
                ", topicName='" + topicName + '\'' +
                ", attendanceMode='" + attendanceMode + '\'' +
                ", campus='" + campus + '\'' +
                ", semester='" + semester + '\'' +
                ", availabilityNumber=" + availabilityNumber +
                ", classFormat='" + classFormat + '\'' +
                ", instanceNumber=" + instanceNumber +
                ", sessions=" + sessions +
                '}';
    }
}