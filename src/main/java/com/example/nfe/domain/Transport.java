package com.example.nfe.domain;

/**
 * Provisional transport information, optional for any operation type.
 * The fields do not yet represent the official NF-e model.
 * <p>
 * {@code freightMode} structurally represents the freight modality
 * (modFrete) as plain data: nullable String, no enum and no code mapping
 * yet — the official representation will be handled by the XML layer.
 */
public record Transport(
        String freightMode,
        String carrierDocument,
        String carrierName,
        Address deliveryAddress) {
}
