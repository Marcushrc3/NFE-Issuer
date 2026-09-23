package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * ICMS data holder, representing the single active ICMS variant of an item.
 * No tax rules, validations or calculations are applied by the domain.
 * <p>
 * Core fields map {@code CST}, {@code modBC}, {@code vBC}, {@code pICMS} and
 * {@code vICMS}. FCP ({@code pFCP}/{@code vFCP}) and desoneração
 * ({@code vICMSDeson}/{@code motDesICMS}) are flat optional fields; the ST
 * block (including FCP-ST) is represented by the nested {@link IcmsStTax}
 * value object. All optional fields may be null. Interstate sharing
 * (EC 87/2015, {@code ICMSUFDest}) is out of scope and deliberately not
 * modelled here.
 */
public record IcmsTax(
        String cst,
        String modBc,
        BigDecimal taxBase,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal fcpRate,
        BigDecimal fcpAmount,
        IcmsStTax st,
        BigDecimal desonerationAmount,
        String desonerationReason) {
}
