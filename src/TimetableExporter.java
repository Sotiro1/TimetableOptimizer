import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Exports a Timetable to a CSV file.
 *
 * <p>No external libraries required — uses only the Java standard library.
 *
 * <p>Output columns:
 * Topic Code, Topic Name, Attendance Mode, Campus, Semester, Availability Number,
 * Class Format, Instance Number, First Class Date, Last Class Date,
 * Day of Week, Start Time, End Time, Building, Room
 */
public class TimetableExporter {

    private static final String HEADER =
            "Topic Code,Topic Name,Attendance Mode,Campus,Semester,Availability Number," +
                    "Class Format,Instance Number,First Class Date,Last Class Date," +
                    "Day of Week,Start Time,End Time,Building,Room";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Exports the given timetable to a CSV file.
     * If the file path does not end in .csv, the extension is appended automatically.
     *
     * @param timetable the timetable to export
     * @param filePath  destination file path
     */
    public void export(Timetable timetable, String filePath) {
        if (timetable == null) {
            System.err.println("[TimetableExporter] Cannot export a null timetable.");
            return;
        }
        if (filePath == null || filePath.isBlank()) {
            System.err.println("[TimetableExporter] File path must not be empty.");
            return;
        }

        // Normalise extension
        if (!filePath.toLowerCase().endsWith(".csv")) {
            filePath += ".csv";
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            writer.write(HEADER);
            writer.newLine();

            if (timetable.getClassInstances() == null
                    || timetable.getClassInstances().isEmpty()) {
                System.out.println("[TimetableExporter] Timetable '"
                        + timetable.getName()
                        + "' has no class instances. Empty file written to: " + filePath);
                return;
            }

            int rows = 0;

            for (ClassInstance ci : timetable.getClassInstances()) {
                if (ci.getSessions() == null || ci.getSessions().isEmpty()) continue;

                for (Session session : ci.getSessions()) {
                    writer.write(buildRow(ci, session));
                    writer.newLine();
                    rows++;
                }
            }

            System.out.println("[TimetableExporter] Timetable '" + timetable.getName()
                    + "' exported to: " + filePath
                    + " (" + rows + " row(s) written).");

        } catch (IOException e) {
            System.err.println("[TimetableExporter] Failed to write file '"
                    + filePath + "': " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildRow(ClassInstance ci, Session session) {
        return String.join(",",
                escape(safe(ci.getTopicCode())),
                escape(safe(ci.getTopicName())),
                escape(safe(ci.getAttendanceMode())),
                escape(safe(ci.getCampus())),
                escape(safe(ci.getSemester())),
                escape(String.valueOf(ci.getAvailabilityNumber())),
                escape(safe(ci.getClassFormat())),
                escape(String.valueOf(ci.getInstanceNumber())),
                escape(session.getFirstClassDate() != null
                        ? session.getFirstClassDate().format(DATE_FMT) : ""),
                escape(session.getLastClassDate() != null
                        ? session.getLastClassDate().format(DATE_FMT) : ""),
                escape(safe(session.getDayOfWeek())),
                escape(session.getStartTime() != null
                        ? session.getStartTime().format(TIME_FMT) : ""),
                escape(session.getEndTime() != null
                        ? session.getEndTime().format(TIME_FMT) : ""),
                escape(safe(session.getBuilding())),
                escape(safe(session.getRoom()))
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}