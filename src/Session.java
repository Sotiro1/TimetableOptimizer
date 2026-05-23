import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a recurring weekly session within a ClassInstance.
 * Captures the date range, day, time slot, and physical location.
 */
public class Session {

    private LocalDate firstClassDate;
    private LocalDate lastClassDate;
    private String dayOfWeek;      // e.g. "Monday", "Tuesday"
    private LocalTime startTime;
    private LocalTime endTime;
    private String building;
    private String room;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Session() {}

    public Session(LocalDate firstClassDate, LocalDate lastClassDate, String dayOfWeek,
                   LocalTime startTime, LocalTime endTime, String building, String room) {
        this.firstClassDate = firstClassDate;
        this.lastClassDate  = lastClassDate;
        this.dayOfWeek      = dayOfWeek;
        this.startTime      = startTime;
        this.endTime        = endTime;
        this.building       = building;
        this.room           = room;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public LocalDate getFirstClassDate() { return firstClassDate; }
    public void setFirstClassDate(LocalDate firstClassDate) { this.firstClassDate = firstClassDate; }

    public LocalDate getLastClassDate() { return lastClassDate; }
    public void setLastClassDate(LocalDate lastClassDate) { this.lastClassDate = lastClassDate; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Session{" +
                "firstClassDate=" + firstClassDate +
                ", lastClassDate=" + lastClassDate +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", building='" + building + '\'' +
                ", room='" + room + '\'' +
                '}';
    }
}
