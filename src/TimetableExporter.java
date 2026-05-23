import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class TimetableExporter {

    public void export(Timetable timetable, String filePath) {
        if (!filePath.endsWith(".xlsx")) {
            filePath += ".xlsx";
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(filePath)) {

            XSSFSheet sheet = workbook.createSheet("Timetable");

            // --- Header style ---
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)33, (byte)84, (byte)157}, null)); // dark blue
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setFontName("Arial");
            headerStyle.setFont(headerFont);

            // --- Alternating row styles ---
            XSSFCellStyle rowStyle1 = workbook.createCellStyle();
            rowStyle1.setFillForegroundColor(new XSSFColor(new byte[]{(byte)235, (byte)241, (byte)255}, null));
            rowStyle1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            rowStyle1.setAlignment(HorizontalAlignment.CENTER);
            XSSFFont rowFont = workbook.createFont();
            rowFont.setFontName("Arial");
            rowFont.setFontHeightInPoints((short) 10);
            rowStyle1.setFont(rowFont);

            XSSFCellStyle rowStyle2 = workbook.createCellStyle();
            rowStyle2.setFillForegroundColor(new XSSFColor(new byte[]{(byte)255, (byte)255, (byte)255}, null));
            rowStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            rowStyle2.setAlignment(HorizontalAlignment.CENTER);
            rowStyle2.setFont(rowFont);

            // --- Headers ---
            String[] headers = {"Topic Code", "Topic Name", "Day of Week", "Start Time", "End Time", "Building", "Room", "Campus", "Class Format"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- Data rows ---
            List<ClassInstance> instances = timetable.getClassInstances();
            int rowNum = 1;
            for (ClassInstance ci : instances) {
                for (Session session : ci.getSessions()) {
                    Row row = sheet.createRow(rowNum);
                    XSSFCellStyle style = (rowNum % 2 == 0) ? rowStyle2 : rowStyle1;

                    String[] values = {
                            ci.getTopicCode() != null ? ci.getTopicCode() : "",
                            ci.getTopicName() != null ? ci.getTopicName() : "",
                            session.getDayOfWeek(),
                            session.getStartTime() != null ? session.getStartTime().toString() : "",
                            session.getEndTime()   != null ? session.getEndTime().toString()   : "",
                            session.getBuilding(),
                            session.getRoom(),
                            ci.getCampus(),
                            ci.getClassFormat()
                    };

                    for (int i = 0; i < values.length; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(values[i]);
                        cell.setCellStyle(style);
                    }
                    rowNum++;
                }
            }

            // --- Auto-size columns ---
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos);
            System.out.println("[TimetableExporter] Timetable '" + timetable.getName() + "' exported successfully to: " + filePath);

        } catch (IOException e) {
            System.out.println("[TimetableExporter] ERROR -- failed to export timetable: " + e.getMessage());
        }
    }
}