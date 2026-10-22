package com.first.api.first_api.importacion;

import com.first.api.first_api.models.Sexo;
import com.first.api.first_api.models.TipoIva;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utilidades para normalizar datos reales de carteras de aseguradoras argentinas:
 * nombres, sufijos societarios, CUIT/DNI, provincias y localidades.
 */
public final class ArgentineDataNormalizer {

    private static final Pattern COMPANY_SUFFIXES = Pattern.compile(
        "\\b(SOCIEDAD ANONIMA|SOCIEDAD COLECTIVA|SOC COL|S R L|SRL|S C A|SCA|S A|SA|S C|SC|S H|SH)\\b"
    );

    private static final Pattern CORPORATE_INDICATORS = Pattern.compile(
        "\\b(S\\.?A\\.?|S\\.?R\\.?L\\.?|S\\.?C\\.?|SOC\\.?\\s*COL\\.?|COOP\\.?|ASOC\\.?|CLUB|MUNICIPALIDAD|CONSORCIO|FIDEICOMISO|CIRCULO|CENTRO DE DIA|PARROQUIA|SANATORIO|FARMACIA|INSTITUTO|CABANA|ESTANCIAS)\\b"
    );

    private ArgentineDataNormalizer() {
    }

    public static String stripAccentsAndClean(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input.toUpperCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "")
                         .replaceAll("\\s+", " ")
                         .trim();
    }

    public static String normalizeCompanyOrPersonKey(String rawName) {
        if (rawName == null) {
            return "";
        }
        String cleaned = stripAccentsAndClean(rawName);
        cleaned = cleaned.replaceAll("[.,$\\-_*]", " ");
        cleaned = COMPANY_SUFFIXES.matcher(cleaned).replaceAll(" ");
        return cleaned.replaceAll("\\s+", " ").trim();
    }

    public static boolean isPersonaJuridica(String rawNombre) {
        if (rawNombre == null || rawNombre.isBlank()) {
            return false;
        }
        String clean = stripAccentsAndClean(rawNombre);
        return CORPORATE_INDICATORS.matcher(clean).find();
    }

    public static record DocExtractionResult(
        String dniLimpio,
        boolean esPersonaJuridica,
        Sexo sexoInferido,
        TipoIva ivaInferido
    ) {}

    public static DocExtractionResult extractDocumento(String rawDoc, String rawNombre) {
        String digits = "";
        if (rawDoc != null && !rawDoc.isBlank()) {
            String doc = rawDoc.trim();
            int commaIdx = doc.indexOf(',');
            if (commaIdx != -1) {
                doc = doc.substring(0, commaIdx).trim();
            }
            if (doc.endsWith(".0")) {
                doc = doc.substring(0, doc.length() - 2).trim();
            }
            digits = doc.replaceAll("\\D", "");
        }

        boolean corporate = isPersonaJuridica(rawNombre);

        if (digits.length() == 11) {
            String prefix = digits.substring(0, 2);
            if (prefix.equals("30") || prefix.equals("33") || prefix.equals("34")) {
                return new DocExtractionResult(digits, true, Sexo.OTRO, TipoIva.RESPONSABLE_INSCRIPTO);
            } else if (prefix.equals("20") || prefix.equals("23") || prefix.equals("24") || prefix.equals("27")) {
                String dni = digits.substring(2, 10).replaceFirst("^0+", "");
                if (dni.isEmpty()) {
                    dni = digits.substring(2, 10);
                }
                Sexo sexo = switch (prefix) {
                    case "20" -> Sexo.MASCULINO;
                    case "27" -> Sexo.FEMENINO;
                    default -> Sexo.OTRO;
                };
                return new DocExtractionResult(dni, false, sexo, TipoIva.CONSUMIDOR_FINAL);
            } else {
                if (corporate) {
                    return new DocExtractionResult(digits, true, Sexo.OTRO, TipoIva.RESPONSABLE_INSCRIPTO);
                } else {
                    String dni = digits.substring(2, 10).replaceFirst("^0+", "");
                    if (dni.isEmpty()) {
                        dni = digits.substring(2, 10);
                    }
                    return new DocExtractionResult(dni, false, Sexo.OTRO, TipoIva.CONSUMIDOR_FINAL);
                }
            }
        } else if (digits.length() >= 6 && digits.length() <= 9) {
            String dni = digits.replaceFirst("^0+", "");
            if (dni.isEmpty()) {
                dni = digits;
            }
            if (corporate) {
                return new DocExtractionResult(dni, true, Sexo.OTRO, TipoIva.RESPONSABLE_INSCRIPTO);
            } else {
                return new DocExtractionResult(dni, false, Sexo.OTRO, TipoIva.CONSUMIDOR_FINAL);
            }
        } else if (digits.isEmpty()) {
            return new DocExtractionResult(
                null,
                corporate,
                Sexo.OTRO,
                corporate ? TipoIva.RESPONSABLE_INSCRIPTO : TipoIva.CONSUMIDOR_FINAL
            );
        } else {
            String dni = digits.replaceFirst("^0+", "");
            if (dni.isEmpty()) {
                dni = digits;
            }
            return new DocExtractionResult(
                dni,
                corporate,
                Sexo.OTRO,
                corporate ? TipoIva.RESPONSABLE_INSCRIPTO : TipoIva.CONSUMIDOR_FINAL
            );
        }
    }

    public static String normalizeProvinciaName(String rawProv, String rawLoc) {
        String p = stripAccentsAndClean(rawProv);
        String l = stripAccentsAndClean(rawLoc);

        if (p.equals("CAPITAL") || p.equals("CAPITAL FEDERAL") || p.equals("CABA") || p.equals("CIUDAD AUTONOMA DE BUENOS AIRES")
            || l.equals("CAPITAL") || l.equals("CAPITAL FEDERAL") || l.equals("CABA") || l.equals("CIUDAD AUTONOMA DE BUENOS AIRES")) {
            return "CIUDAD AUTONOMA DE BUENOS AIRES";
        }

        if (p.isBlank()) {
            return "BUENOS AIRES";
        }

        if (p.equals("BS AS") || p.equals("BS. AS.") || p.equals("BSAS") || p.equals("BUENOS AIRES")) {
            return "BUENOS AIRES";
        }
        if (p.equals("CBA") || p.equals("CORDOBA")) {
            return "CORDOBA";
        }
        if (p.equals("STA FE") || p.equals("STA. FE") || p.equals("SANTA FE")) {
            return "SANTA FE";
        }
        if (p.equals("MZA") || p.equals("MENDOZA")) {
            return "MENDOZA";
        }
        if (p.equals("ER") || p.equals("ENTRE RIOS")) {
            return "ENTRE RIOS";
        }

        return p;
    }

    public static String normalizeLocalidadName(String rawLoc) {
        if (rawLoc == null) {
            return "";
        }
        String cleaned = stripAccentsAndClean(rawLoc);
        cleaned = cleaned.replaceAll("\\([^)]*\\)", "").replaceAll("[()]", "").trim();

        cleaned = cleaned.replaceFirst("^(?i)CNEL\\.?\\s*", "CORONEL ")
                         .replaceFirst("^(?i)GRAL\\.?\\s*", "GENERAL ")
                         .replaceFirst("^(?i)ING\\.?\\s*", "INGENIERO ")
                         .replaceFirst("^(?i)PTE\\.?\\s*", "PRESIDENTE ")
                         .replaceFirst("^(?i)DR\\.?\\s*", "DOCTOR ")
                         .replaceFirst("^(?i)SAN\\s+", "SAN ");

        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        if (cleaned.equals("CABA") || cleaned.equals("CAPITAL")) {
            return "CAPITAL FEDERAL";
        }

        return cleaned;
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}
