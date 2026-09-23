package com.example.nfe.api.dto;

import com.example.nfe.application.service.ProcessingMode;
import com.example.nfe.domain.OperationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public record NfeEmissionRequest(
        @NotBlank(message = "externalReference must not be blank") String externalReference,
        @NotNull(message = "operationType must not be null") OperationType operationType,
        String model,
        Integer series,
        Long number,
        OffsetDateTime emissionDate,
        String operationDescription,
        String fiscalEstablishmentCity,
        String stateCode,
        String randomCode,
        String operationDirection,
        String destinationType,
        String printFormat,
        String emissionType,
        String checkDigit,
        String environment,
        String purpose,
        String finalConsumer,
        String presenceIndicator,
        String processType,
        String processVersion,
        @NotNull(message = "issuer must not be null") @Valid IssuerDto issuer,
        @NotNull(message = "recipient must not be null") @Valid RecipientDto recipient,
        @NotEmpty(message = "items must contain at least one item") @Valid List<ItemDto> items,
        @Valid ImportDetailsDto importDetails,
        @Valid ExportDetailsDto exportDetails,
        @Valid PaymentDto payment,
        @NotNull(message = "processingMode must not be null") ProcessingMode processingMode) {
}
