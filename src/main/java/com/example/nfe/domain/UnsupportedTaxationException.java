package com.example.nfe.domain;

/**
 * Signals that the request carries a taxation configuration this version of
 * the NF-e engine does not support (e.g. an ICMS CST outside the implemented
 * variants, or a field combination the mapper cannot represent).
 * <p>
 * This is a deterministic limitation of the supported request surface — a
 * client error — never an unexpected internal failure. The message must
 * identify the taxation and the received CST, and state that the request
 * cannot be processed in this version.
 */
public class UnsupportedTaxationException extends RuntimeException {

    public UnsupportedTaxationException(String message) {
        super(message);
    }
}
