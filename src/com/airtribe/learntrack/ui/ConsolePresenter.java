package com.airtribe.learntrack.ui;

import java.util.List;

/**
 * Console rendering utility for tabular command-line output.
 */
public final class ConsolePresenter {
    private static final String NULL_CELL_VALUE = "N/A";

    private ConsolePresenter() {
        throw new AssertionError("ConsolePresenter cannot be instantiated");
    }

    /**
     * Prints a dynamically padded ASCII table to standard output.
     *
     * @param headers non-empty column headers
     * @param rows table body rows; null rows are ignored and null cells render as {@code N/A}
     * @throws IllegalArgumentException if headers are null, empty, contain null values,
     *         or if any row has more cells than there are headers
     */
    public static void printTable(String[] headers, List<String[]> rows) {
        validateHeaders(headers);

        int[] columnWidths = calculateColumnWidths(headers, rows);
        String separatorLine = buildSeparatorLine(columnWidths);

        System.out.println(separatorLine);
        printRow(headers, columnWidths);
        System.out.println(separatorLine);

        if (rows != null) {
            for (String[] row : rows) {
                if (row != null) {
                    printRow(row, columnWidths);
                }
            }
        }

        System.out.println(separatorLine);
    }

    private static int[] calculateColumnWidths(String[] headers, List<String[]> rows) {
        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        if (rows == null) {
            return columnWidths;
        }

        for (String[] row : rows) {
            if (row == null) {
                continue;
            }
            if (row.length > headers.length) {
                throw new IllegalArgumentException("row must not contain more cells than headers");
            }
            for (int i = 0; i < row.length; i++) {
                String value = normalizeCell(row[i]);
                if (value.length() > columnWidths[i]) {
                    columnWidths[i] = value.length();
                }
            }
        }
        return columnWidths;
    }

    private static String buildSeparatorLine(int[] columnWidths) {
        StringBuilder borderBuilder = new StringBuilder("+");
        for (int width : columnWidths) {
            borderBuilder.append("-".repeat(width + 2)).append("+");
        }
        return borderBuilder.toString();
    }

    private static void printRow(String[] cells, int[] columnWidths) {
        StringBuilder rowBuilder = new StringBuilder("|");
        for (int i = 0; i < columnWidths.length; i++) {
            String value = i < cells.length ? normalizeCell(cells[i]) : "";
            int padding = columnWidths[i] - value.length();
            rowBuilder.append(' ')
                    .append(value)
                    .append(" ".repeat(padding + 1))
                    .append('|');
        }
        System.out.println(rowBuilder);
    }

    private static String normalizeCell(String value) {
        return value == null ? NULL_CELL_VALUE : value;
    }

    private static void validateHeaders(String[] headers) {
        if (headers == null || headers.length == 0) {
            throw new IllegalArgumentException("headers must not be empty");
        }
        for (String header : headers) {
            if (header == null) {
                throw new IllegalArgumentException("header must not be null");
            }
        }
    }
}
