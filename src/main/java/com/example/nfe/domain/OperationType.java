package com.example.nfe.domain;

/**
 * Supported NF-e operation types and their operation-specific sections.
 */
public enum OperationType {

    /** Requires {@link ImportDetails} and forbids {@link ExportDetails}. */
    IMPORT,

    /** Forbids both {@link ImportDetails} and {@link ExportDetails}. */
    TRANSFER,

    /** Requires {@link ExportDetails} and forbids {@link ImportDetails}. */
    EXPORT
}
