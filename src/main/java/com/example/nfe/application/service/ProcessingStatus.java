package com.example.nfe.application.service;

/**
 * Processing result status of an NF-e emission request.
 * <ul>
 *   <li>{@link #XML_GENERATED} — the XML was generated and XSD-validated
 *       (XML_ONLY mode).</li>
 *   <li>{@link #READY_FOR_EMISSION} — the XML was generated, validated and
 *       signed, but has NOT yet been transmitted to SEFAZ.</li>
 *   <li>{@link #AUTHORIZED} — SEFAZ authorized the NF-e (cStat 100).</li>
 *   <li>{@link #REJECTED} — SEFAZ rejected the NF-e or the batch.</li>
 *   <li>{@link #PROCESSING} — the batch was accepted and is being
 *       processed; no authorization protocol exists yet.</li>
 * </ul>
 */
public enum ProcessingStatus {
    XML_GENERATED,
    READY_FOR_EMISSION,
    AUTHORIZED,
    REJECTED,
    PROCESSING
}
