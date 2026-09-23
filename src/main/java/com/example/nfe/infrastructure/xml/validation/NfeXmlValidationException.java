package com.example.nfe.infrastructure.xml.validation;

/**
 * Thrown when generated NF-e XML does not conform to the official PL_010f
 * XSD schema.
 * <p>
 * Carries the first validation problem found, including line and column
 * information when the underlying parser reports it.
 */
public class NfeXmlValidationException extends RuntimeException {

    private final int lineNumber;
    private final int columnNumber;

    public NfeXmlValidationException(String message) {
        this(message, -1, -1, null);
    }

    public NfeXmlValidationException(String message, Throwable cause) {
        this(message, -1, -1, cause);
    }

    public NfeXmlValidationException(String message, int lineNumber, int columnNumber, Throwable cause) {
        super(buildMessage(message, lineNumber, columnNumber), cause);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    private static String buildMessage(String message, int lineNumber, int columnNumber) {
        if (lineNumber > 0) {
            if (columnNumber > 0) {
                return message + " (line " + lineNumber + ", column " + columnNumber + ")";
            }
            return message + " (line " + lineNumber + ")";
        }
        return message;
    }
}
