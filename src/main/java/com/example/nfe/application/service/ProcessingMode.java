package com.example.nfe.application.service;

/**
 * Application-level processing mode for an NF-e emission request.
 * <p>
 * This is an API/application processing concern — deliberately NOT part of
 * the fiscal domain model.
 * <ul>
 *   <li>{@link #XML_ONLY} — generate, XSD-validate and return the unsigned
 *       XML. No signing, no credentials, no SEFAZ.</li>
 *   <li>{@link #EMIT} — generate, XSD-validate and sign the XML, then
 *       transmit the signed document to SEFAZ through the configured
 *       {@code SefazTransmitter} and return the structured result.</li>
 * </ul>
 */
public enum ProcessingMode {
    XML_ONLY,
    EMIT
}
