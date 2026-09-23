package com.example.nfe.domain;

/**
 * Item-level taxation, optional on {@link NfeItem}.
 * <p>
 * Every tax component is optional and purely descriptive; the domain applies
 * no calculations, validations or mandatory-field rules. {@code is}
 * (Imposto Seletivo) and {@code rtc} (IBSCBS) are independent RTC groups and
 * may both be null.
 */
public record ItemTaxation(
        IcmsTax icms,
        IpiTax ipi,
        PisCofinsTax pisCofins,
        ImportTax importTax,
        IsTax is,
        RtcTaxation rtc) {
}
