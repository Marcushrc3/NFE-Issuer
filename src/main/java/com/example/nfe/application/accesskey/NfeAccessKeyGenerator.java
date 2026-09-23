package com.example.nfe.application.accesskey;

import com.example.nfe.domain.NfeEmission;

import java.time.format.DateTimeFormatter;

/**
 * Builds the official 44-digit NF-e access key (chave de acesso) from
 * explicit domain values only. Pure business logic: no XML, no Spring, no
 * SEFAZ dependencies.
 * <p>
 * Composition: cUF(2) + AAMM(4) + CNPJ(14) + mod(2) + serie(3) + nNF(9) +
 * tpEmis(1) + cNF(8) + cDV(1). The check digit uses the official modulo-11
 * algorithm with weights 2..9 cycling from right to left (dv = 11 - sum%11;
 * dv &gt;= 10 becomes 0).
 * <p>
 * Nothing is invented: when any required component is missing, the
 * generator returns {@code null} and the caller decides how to proceed.
 */
public final class NfeAccessKeyGenerator {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyMM");
    private static final java.util.Set<String> VALID_UFS = java.util.Set.of(
            "11", "12", "13", "14", "15", "16", "17", "21", "22", "23", "24", "25",
            "26", "27", "28", "29", "31", "32", "33", "35", "41", "42", "43", "50",
            "51", "52", "53");

    private NfeAccessKeyGenerator() {
    }

    /**
     * Generates the 44-digit access key for the given emission, or
     * {@code null} when any required component is absent.
     */
    public static String generate(NfeEmission emission) {
        if (emission.stateCode() == null
                || emission.emissionDate() == null
                || emission.issuer() == null || emission.issuer().document() == null
                || emission.model() == null
                || emission.series() == null
                || emission.number() == null
                || emission.emissionType() == null
                || emission.randomCode() == null
                || emission.checkDigit() == null) {
            return null;
        }
        String first43 = emission.stateCode()
                + YEAR_MONTH.format(emission.emissionDate())
                + emission.issuer().document()
                + emission.model()
                + String.format("%03d", emission.series())
                + String.format("%09d", emission.number())
                + emission.emissionType()
                + emission.randomCode();
        String computed = checkDigit(first43);
        if (emission.checkDigit() != null && VALID_UFS.contains(emission.stateCode())
                && !emission.checkDigit().equals(computed)) {
            throw new IllegalArgumentException("checkDigit does not match the NF-e access key calculation");
        }
        return first43 + computed;
    }

    /**
     * Official NF-e check digit over the first 43 key digits.
     */
    public static String checkDigit(String first43) {
        int sum = 0;
        int weight = 2;
        for (int i = first43.length() - 1; i >= 0; i--) {
            sum += Character.digit(first43.charAt(i), 10) * weight;
            weight = weight == 9 ? 2 : weight + 1;
        }
        int normalized = (sum + 1) % 11;
        int dv = 11 - normalized;
        if (dv >= 10) {
            dv = 0;
        }
        return String.valueOf(dv);
    }
}
