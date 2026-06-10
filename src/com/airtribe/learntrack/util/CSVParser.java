package com.airtribe.learntrack.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Zero-dependency CSV parser and writer using a deterministic state machine.
 *
 * <p>The parser supports RFC 4180-style quoted fields, escaped double quotes,
 * commas inside quotes, and newlines inside quoted fields.</p>
 */
public final class CSVParser {
    private enum ParserState {
        NORMAL,
        QUOTED,
        QUOTE_ESCAPED
    }

    private CSVParser() {
        throw new AssertionError("CSVParser cannot be instantiated");
    }

    /**
     * Parses a CSV file into rows.
     *
     * <p>Missing files return an empty result. Empty logical rows and comment
     * rows beginning with {@code #} are skipped after parsing.</p>
     *
     * @param filePath path to the CSV file
     * @return parsed rows, each represented as a string array
     * @throws IOException if the file exists but cannot be read
     * @throws IllegalArgumentException if {@code filePath} is null or blank
     */
    public static List<String[]> parseCSV(String filePath) throws IOException {
        String resolvedPath = requireNotBlank(filePath, "filePath");
        List<String[]> records = new ArrayList<>();
        File file = new File(resolvedPath);
        if (!file.exists()) {
            return records;
        }

        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            char[] buffer = new char[4096];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                contentBuilder.append(buffer, 0, charsRead);
            }
        }

        return parseRawCSVString(contentBuilder.toString());
    }

    /**
     * Writes rows to a CSV file.
     *
     * <p>Fields containing commas, quotes, carriage returns, or newlines are
     * quoted. Double quotes inside fields are escaped by doubling them.</p>
     *
     * @param filePath destination CSV file path
     * @param data rows to write
     * @throws IOException if the file cannot be written
     * @throws IllegalArgumentException if {@code filePath} is blank, {@code data} is null,
     *         any row is null, or any field is null
     */
    public static void writeCSV(String filePath, List<String[]> data) throws IOException {
        String resolvedPath = requireNotBlank(filePath, "filePath");
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resolvedPath))) {
            for (String[] row : data) {
                if (row == null) {
                    throw new IllegalArgumentException("row must not be null");
                }
                writer.write(formatRow(row));
                writer.newLine();
            }
        }
    }

    private static List<String[]> parseRawCSVString(String text) {
        List<String[]> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder fieldBuffer = new StringBuilder();
        ParserState state = ParserState.NORMAL;
        boolean hasPendingData = false;
        boolean firstFieldQuoted = false;

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char currentChar = chars[i];

            switch (state) {
                case NORMAL:
                    if (currentChar == '"') {
                        if (fieldBuffer.length() > 0) {
                            if (!fieldBuffer.toString().trim().isEmpty()) {
                                throw new IllegalArgumentException("Unexpected quote inside unquoted CSV field");
                            }
                            fieldBuffer.setLength(0);
                        }
                        if (currentRow.isEmpty() && fieldBuffer.length() == 0) {
                            firstFieldQuoted = true;
                        }
                        state = ParserState.QUOTED;
                        hasPendingData = true;
                    } else if (currentChar == ',') {
                        currentRow.add(fieldBuffer.toString().trim());
                        fieldBuffer.setLength(0);
                        hasPendingData = true;
                    } else if (isLineBreak(currentChar)) {
                        if (isCarriageReturnBeforeLineFeed(currentChar, chars, i)) {
                            i++;
                        }
                        addCompletedRow(rows, currentRow, fieldBuffer, firstFieldQuoted);
                        currentRow = new ArrayList<>();
                        fieldBuffer.setLength(0);
                        hasPendingData = false;
                        firstFieldQuoted = false;
                    } else {
                        fieldBuffer.append(currentChar);
                        hasPendingData = true;
                    }
                    break;
                case QUOTED:
                    if (currentChar == '"') {
                        state = ParserState.QUOTE_ESCAPED;
                    } else {
                        fieldBuffer.append(currentChar);
                        hasPendingData = true;
                    }
                    break;
                case QUOTE_ESCAPED:
                    if (currentChar == '"') {
                        fieldBuffer.append('"');
                        state = ParserState.QUOTED;
                        hasPendingData = true;
                    } else if (currentChar == ',') {
                        currentRow.add(fieldBuffer.toString());
                        fieldBuffer.setLength(0);
                        state = ParserState.NORMAL;
                        hasPendingData = true;
                    } else if (isLineBreak(currentChar)) {
                        if (isCarriageReturnBeforeLineFeed(currentChar, chars, i)) {
                            i++;
                        }
                        addCompletedQuotedRow(rows, currentRow, fieldBuffer, firstFieldQuoted);
                        currentRow = new ArrayList<>();
                        fieldBuffer.setLength(0);
                        state = ParserState.NORMAL;
                        hasPendingData = false;
                        firstFieldQuoted = false;
                    } else if (Character.isWhitespace(currentChar)) {
                        state = ParserState.NORMAL;
                    } else {
                        throw new IllegalArgumentException("Invalid character after closing quote: " + currentChar);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unsupported parser state: " + state);
            }
        }

        if (state == ParserState.QUOTED) {
            throw new IllegalArgumentException("Unclosed quoted CSV field");
        }

        if (state == ParserState.QUOTE_ESCAPED || hasPendingData || !currentRow.isEmpty() || fieldBuffer.length() > 0) {
            boolean quotedField = state == ParserState.QUOTE_ESCAPED;
            if (quotedField) {
                addCompletedQuotedRow(rows, currentRow, fieldBuffer, firstFieldQuoted);
            } else {
                addCompletedRow(rows, currentRow, fieldBuffer, firstFieldQuoted);
            }
        }

        return rows;
    }

    private static void addCompletedRow(
            List<String[]> rows,
            List<String> currentRow,
            StringBuilder fieldBuffer,
            boolean firstFieldQuoted
    ) {
        currentRow.add(fieldBuffer.toString().trim());
        addRowIfNotBlank(rows, currentRow, firstFieldQuoted);
    }

    private static void addCompletedQuotedRow(
            List<String[]> rows,
            List<String> currentRow,
            StringBuilder fieldBuffer,
            boolean firstFieldQuoted
    ) {
        currentRow.add(fieldBuffer.toString());
        addRowIfNotBlank(rows, currentRow, firstFieldQuoted);
    }

    private static void addRowIfNotBlank(List<String[]> rows, List<String> currentRow, boolean firstFieldQuoted) {
        if (!firstFieldQuoted && currentRow.size() == 1 && currentRow.get(0).trim().isEmpty()) {
            return;
        }
        if (!firstFieldQuoted && !currentRow.isEmpty() && currentRow.get(0).trim().startsWith("#")) {
            return;
        }
        rows.add(currentRow.toArray(new String[0]));
    }

    private static String formatRow(String[] row) {
        StringBuilder rowBuilder = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (row[i] == null) {
                throw new IllegalArgumentException("field must not be null");
            }

            rowBuilder.append(escapeField(row[i], i == 0));
            if (i < row.length - 1) {
                rowBuilder.append(',');
            }
        }
        return rowBuilder.toString();
    }

    private static String escapeField(String field, boolean firstField) {
        if (requiresQuoting(field, firstField)) {
            return '"' + field.replace("\"", "\"\"") + '"';
        }
        return field;
    }

    private static boolean requiresQuoting(String field, boolean firstField) {
        return field.indexOf(',') >= 0
                || field.indexOf('"') >= 0
                || field.indexOf('\n') >= 0
                || field.indexOf('\r') >= 0
                || (firstField && field.trim().startsWith("#"));
    }

    private static boolean isLineBreak(char currentChar) {
        return currentChar == '\n' || currentChar == '\r';
    }

    private static boolean isCarriageReturnBeforeLineFeed(char currentChar, char[] chars, int index) {
        return currentChar == '\r' && index + 1 < chars.length && chars[index + 1] == '\n';
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmedValue;
    }
}
