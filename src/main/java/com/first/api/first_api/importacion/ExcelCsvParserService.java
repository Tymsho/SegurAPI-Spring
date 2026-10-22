package com.first.api.first_api.importacion;

import com.first.api.first_api.models.TipoPago;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Motor para lectura y parseo avanzado de planillas de aseguradoras en formato .xls, .xlsx y .csv.
 * Incluye detección inteligente de encabezados, formateo limpio de celdas, normalización de pagos
 * y resolución de colapsos de endosos y anulaciones.
 */
@Service
public class ExcelCsvParserService {

    private static final Set<String> HEADER_KEYWORDS = Set.of(
        "POLIZA", "TOMADOR", "ASEGURADO", "RAMO", "SECCION", "VIGENCIA",
        "VIG", "PREMIO", "ENDOSO", "DOCUMENTO", "DOC"
    );

    @Data
    public static class RawSheetData {
        private int headerRowIndex;
        private List<String> headers;
        private List<Map<String, String>> dataRows;
        private int totalRawRows;

        public RawSheetData() {}

        public RawSheetData(int headerRowIndex, List<String> headers,
                            List<Map<String, String>> dataRows, int totalRawRows) {
            this.headerRowIndex = headerRowIndex;
            this.headers = headers;
            this.dataRows = dataRows;
            this.totalRawRows = totalRawRows;
        }

        public static RawSheetDataBuilder builder() {
            return new RawSheetDataBuilder();
        }

        public static class RawSheetDataBuilder {
            private int headerRowIndex;
            private List<String> headers;
            private List<Map<String, String>> dataRows;
            private int totalRawRows;

            RawSheetDataBuilder() {}

            public RawSheetDataBuilder headerRowIndex(int headerRowIndex) {
                this.headerRowIndex = headerRowIndex;
                return this;
            }

            public RawSheetDataBuilder headers(List<String> headers) {
                this.headers = headers;
                return this;
            }

            public RawSheetDataBuilder dataRows(List<Map<String, String>> dataRows) {
                this.dataRows = dataRows;
                return this;
            }

            public RawSheetDataBuilder totalRawRows(int totalRawRows) {
                this.totalRawRows = totalRawRows;
                return this;
            }

            public RawSheetData build() {
                return new RawSheetData(headerRowIndex, headers, dataRows, totalRawRows);
            }
        }
    }

    @Data
    public static class ParsedPolizaItem {
        private String nroPza;
        private Integer nroEndoso;
        private String tomadorNombre;
        private String aseguradoNombre;
        private String rawDocumento;
        private String domicilio;
        private String rawLocalidad;
        private String rawProvincia;
        private String telefono;
        private String rawFechaNacimiento;
        private LocalDate inicioVigencia;
        private LocalDate finVigencia;
        private LocalDate fechaEmision;
        private String rawRamoPrincipal;
        private String rawRamoSecundarioPlan;
        private String bienAsegurado;
        private String tipoFacturacion;
        private TipoPago tipoPago;
        private Double premio;
        private Double sumaAsegurada;
        private boolean activo;

        public ParsedPolizaItem() {}

        public ParsedPolizaItem(String nroPza, Integer nroEndoso, String tomadorNombre, String aseguradoNombre,
                                String rawDocumento, String domicilio, String rawLocalidad, String rawProvincia,
                                String telefono, String rawFechaNacimiento, LocalDate inicioVigencia,
                                LocalDate finVigencia, LocalDate fechaEmision, String rawRamoPrincipal, String rawRamoSecundarioPlan,
                                String bienAsegurado, String tipoFacturacion, TipoPago tipoPago,
                                Double premio, Double sumaAsegurada, boolean activo) {
            this.nroPza = nroPza;
            this.nroEndoso = nroEndoso;
            this.tomadorNombre = tomadorNombre;
            this.aseguradoNombre = aseguradoNombre;
            this.rawDocumento = rawDocumento;
            this.domicilio = domicilio;
            this.rawLocalidad = rawLocalidad;
            this.rawProvincia = rawProvincia;
            this.telefono = telefono;
            this.rawFechaNacimiento = rawFechaNacimiento;
            this.inicioVigencia = inicioVigencia;
            this.finVigencia = finVigencia;
            this.fechaEmision = fechaEmision;
            this.rawRamoPrincipal = rawRamoPrincipal;
            this.rawRamoSecundarioPlan = rawRamoSecundarioPlan;
            this.bienAsegurado = bienAsegurado;
            this.tipoFacturacion = tipoFacturacion;
            this.tipoPago = tipoPago;
            this.premio = premio;
            this.sumaAsegurada = sumaAsegurada;
            this.activo = activo;
        }

        public static ParsedPolizaItemBuilder builder() {
            return new ParsedPolizaItemBuilder();
        }

        public static class ParsedPolizaItemBuilder {
            private String nroPza;
            private Integer nroEndoso;
            private String tomadorNombre;
            private String aseguradoNombre;
            private String rawDocumento;
            private String domicilio;
            private String rawLocalidad;
            private String rawProvincia;
            private String telefono;
            private String rawFechaNacimiento;
            private LocalDate inicioVigencia;
            private LocalDate finVigencia;
            private LocalDate fechaEmision;
            private String rawRamoPrincipal;
            private String rawRamoSecundarioPlan;
            private String bienAsegurado;
            private String tipoFacturacion;
            private TipoPago tipoPago;
            private Double premio;
            private Double sumaAsegurada;
            private boolean activo;

            ParsedPolizaItemBuilder() {}

            public ParsedPolizaItemBuilder nroPza(String nroPza) {
                this.nroPza = nroPza;
                return this;
            }

            public ParsedPolizaItemBuilder nroEndoso(Integer nroEndoso) {
                this.nroEndoso = nroEndoso;
                return this;
            }

            public ParsedPolizaItemBuilder tomadorNombre(String tomadorNombre) {
                this.tomadorNombre = tomadorNombre;
                return this;
            }

            public ParsedPolizaItemBuilder aseguradoNombre(String aseguradoNombre) {
                this.aseguradoNombre = aseguradoNombre;
                return this;
            }

            public ParsedPolizaItemBuilder rawDocumento(String rawDocumento) {
                this.rawDocumento = rawDocumento;
                return this;
            }

            public ParsedPolizaItemBuilder domicilio(String domicilio) {
                this.domicilio = domicilio;
                return this;
            }

            public ParsedPolizaItemBuilder rawLocalidad(String rawLocalidad) {
                this.rawLocalidad = rawLocalidad;
                return this;
            }

            public ParsedPolizaItemBuilder rawProvincia(String rawProvincia) {
                this.rawProvincia = rawProvincia;
                return this;
            }

            public ParsedPolizaItemBuilder telefono(String telefono) {
                this.telefono = telefono;
                return this;
            }

            public ParsedPolizaItemBuilder rawFechaNacimiento(String rawFechaNacimiento) {
                this.rawFechaNacimiento = rawFechaNacimiento;
                return this;
            }

            public ParsedPolizaItemBuilder inicioVigencia(LocalDate inicioVigencia) {
                this.inicioVigencia = inicioVigencia;
                return this;
            }

            public ParsedPolizaItemBuilder finVigencia(LocalDate finVigencia) {
                this.finVigencia = finVigencia;
                return this;
            }

            public ParsedPolizaItemBuilder fechaEmision(LocalDate fechaEmision) {
                this.fechaEmision = fechaEmision;
                return this;
            }

            public ParsedPolizaItemBuilder rawRamoPrincipal(String rawRamoPrincipal) {
                this.rawRamoPrincipal = rawRamoPrincipal;
                return this;
            }

            public ParsedPolizaItemBuilder rawRamoSecundarioPlan(String rawRamoSecundarioPlan) {
                this.rawRamoSecundarioPlan = rawRamoSecundarioPlan;
                return this;
            }

            public ParsedPolizaItemBuilder bienAsegurado(String bienAsegurado) {
                this.bienAsegurado = bienAsegurado;
                return this;
            }

            public ParsedPolizaItemBuilder tipoFacturacion(String tipoFacturacion) {
                this.tipoFacturacion = tipoFacturacion;
                return this;
            }

            public ParsedPolizaItemBuilder tipoPago(TipoPago tipoPago) {
                this.tipoPago = tipoPago;
                return this;
            }

            public ParsedPolizaItemBuilder premio(Double premio) {
                this.premio = premio;
                return this;
            }

            public ParsedPolizaItemBuilder sumaAsegurada(Double sumaAsegurada) {
                this.sumaAsegurada = sumaAsegurada;
                return this;
            }

            public ParsedPolizaItemBuilder activo(boolean activo) {
                this.activo = activo;
                return this;
            }

            public ParsedPolizaItem build() {
                return new ParsedPolizaItem(nroPza, nroEndoso, tomadorNombre, aseguradoNombre,
                        rawDocumento, domicilio, rawLocalidad, rawProvincia, telefono, rawFechaNacimiento,
                        inicioVigencia, finVigencia, fechaEmision, rawRamoPrincipal, rawRamoSecundarioPlan,
                        bienAsegurado, tipoFacturacion, tipoPago, premio, sumaAsegurada, activo);
            }
        }
    }

    public record NombreApellido(String nombre, String apellido) {}

    /**
     * Parsea un archivo (.xls, .xlsx, .csv) extrayendo la fila de encabezados y las filas de datos.
     */
    public RawSheetData parseFileToRawData(byte[] fileBytes, String filename) throws IOException {
        if (filename != null && (filename.toLowerCase().endsWith(".csv") || filename.toLowerCase().endsWith(".txt"))) {
            return parseCsv(fileBytes);
        }
        return parseExcel(fileBytes);
    }

    private RawSheetData parseExcel(byte[] fileBytes) throws IOException {
        byte[] repairedBytes = Ole2RepairUtil.repairIfCorruptedOle2(fileBytes);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(repairedBytes))) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("El archivo Excel no contiene hojas de cálculo");
            }

            Sheet sheet = workbook.getSheetAt(0);
            List<List<String>> allRows = new ArrayList<>();

            int lastRowNum = sheet.getLastRowNum();
            for (int r = 0; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    allRows.add(Collections.emptyList());
                    continue;
                }
                int lastCellNum = row.getLastCellNum();
                List<String> rowCells = new ArrayList<>();
                for (int c = 0; c < lastCellNum; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    rowCells.add(readCellAsString(cell));
                }
                allRows.add(rowCells);
            }

            int headerRowIndex = detectHeaderRowIndex(allRows);
            List<String> headers = extractHeaders(allRows, headerRowIndex);
            List<Map<String, String>> dataRows = buildDataRows(allRows, headerRowIndex, headers);

            return RawSheetData.builder()
                .headerRowIndex(headerRowIndex)
                .headers(headers)
                .dataRows(dataRows)
                .totalRawRows(dataRows.size())
                .build();
        }
    }

    private RawSheetData parseCsv(byte[] fileBytes) throws IOException {
        String content = new String(fileBytes, StandardCharsets.UTF_8);
        if (content.contains("\uFFFD")) {
            content = new String(fileBytes, StandardCharsets.ISO_8859_1);
        }

        List<String> lines = content.lines().toList();
        char delimiter = detectCsvDelimiter(lines);

        List<List<String>> allRows = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank()) continue;
            allRows.add(parseCsvLine(line, delimiter));
        }

        int headerRowIndex = detectHeaderRowIndex(allRows);
        List<String> headers = extractHeaders(allRows, headerRowIndex);
        List<Map<String, String>> dataRows = buildDataRows(allRows, headerRowIndex, headers);

        return RawSheetData.builder()
            .headerRowIndex(headerRowIndex)
            .headers(headers)
            .dataRows(dataRows)
            .totalRawRows(dataRows.size())
            .build();
    }

    private char detectCsvDelimiter(List<String> lines) {
        int commas = 0;
        int semicolons = 0;
        int checkLimit = Math.min(5, lines.size());
        for (int i = 0; i < checkLimit; i++) {
            String l = lines.get(i);
            commas += countOccurrences(l, ',');
            semicolons += countOccurrences(l, ';');
        }
        return semicolons >= commas ? ';' : ',';
    }

    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }

    private List<String> parseCsvLine(String line, char delimiter) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    public int detectHeaderRowIndex(List<List<String>> rows) {
        int maxScore = -1;
        int bestRowIndex = 0;
        int limit = Math.min(15, rows.size());

        for (int r = 0; r < limit; r++) {
            List<String> row = rows.get(r);
            int nonEmptyCount = 0;
            int score = 0;
            for (String cell : row) {
                if (cell != null && !cell.isBlank()) {
                    nonEmptyCount++;
                    String clean = ArgentineDataNormalizer.stripAccentsAndClean(cell).replaceAll("[^A-Z]", " ");
                    for (String word : clean.split("\\s+")) {
                        if (HEADER_KEYWORDS.contains(word)) {
                            score++;
                        }
                    }
                }
            }
            if (nonEmptyCount >= 4 && score > maxScore) {
                maxScore = score;
                bestRowIndex = r;
            }
        }
        return bestRowIndex;
    }

    private List<String> extractHeaders(List<List<String>> allRows, int headerRowIndex) {
        if (headerRowIndex >= allRows.size()) {
            return Collections.emptyList();
        }
        List<String> rawHeaders = allRows.get(headerRowIndex);
        List<String> cleanHeaders = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (int i = 0; i < rawHeaders.size(); i++) {
            String h = rawHeaders.get(i);
            String headerName = (h != null && !h.isBlank()) ? h.trim() : "COL_" + (i + 1);
            String uniqueName = headerName;
            int counter = 2;
            while (seen.contains(uniqueName)) {
                uniqueName = headerName + "_" + counter++;
            }
            seen.add(uniqueName);
            cleanHeaders.add(uniqueName);
        }
        return cleanHeaders;
    }

    private List<Map<String, String>> buildDataRows(List<List<String>> allRows, int headerRowIndex, List<String> headers) {
        List<Map<String, String>> dataRows = new ArrayList<>();
        for (int r = headerRowIndex + 1; r < allRows.size(); r++) {
            List<String> row = allRows.get(r);
            boolean allEmpty = true;
            Map<String, String> map = new LinkedHashMap<>();

            for (int c = 0; c < headers.size(); c++) {
                String val = (c < row.size()) ? row.get(c) : "";
                if (val != null && !val.isBlank()) {
                    allEmpty = false;
                }
                map.put(headers.get(c), val != null ? val.trim() : "");
            }

            if (!allEmpty) {
                dataRows.add(map);
            }
        }
        return dataRows;
    }

    public String readCellAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDateTime dt = cell.getLocalDateTimeCellValue();
                    if (dt != null) {
                        return dt.toLocalDate().toString();
                    }
                    Date d = cell.getDateCellValue();
                    return d != null ? DateTimeFormatter.ISO_LOCAL_DATE.format(d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()) : "";
                }
                double val = cell.getNumericCellValue();
                return BigDecimal.valueOf(val).stripTrailingZeros().toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        double fVal = cell.getNumericCellValue();
                        return BigDecimal.valueOf(fVal).stripTrailingZeros().toPlainString();
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    public LocalDate parseFlexibleDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return null;
        String s = rawDate.trim();

        // Si viene con timestamp con hora ("2026-07-12 12:00:00" o "06/05/1945 12:00:00 a.m.")
        if (s.contains(" ")) {
            s = s.split("\\s+")[0].trim();
        }

        // Si es número serial de Excel (ej. 45120)
        if (s.matches("^\\d{4,6}(\\.0+)?$")) {
            try {
                double serial = Double.parseDouble(s);
                Date d = DateUtil.getJavaDate(serial);
                if (d != null) {
                    return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                }
            } catch (Exception ignored) {}
        }

        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );

        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(s, fmt);
            } catch (DateTimeParseException ignored) {}
        }

        return null;
    }

    public Double parseDoubleValue(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim();
        boolean negative = false;
        if (s.startsWith("-") || s.endsWith("-") || (s.startsWith("(") && s.endsWith(")"))) {
            negative = true;
        }

        s = s.replaceAll("[^0-9,.-]", "");
        if (s.isEmpty()) return null;

        if (s.contains(".") && s.contains(",")) {
            int lastDot = s.lastIndexOf('.');
            int lastComma = s.lastIndexOf(',');
            if (lastComma > lastDot) {
                // Formato argentino: 74.164,14 -> quitar puntos de miles y reemplazar coma por punto
                s = s.replace(".", "").replace(",", ".");
            } else {
                // Formato anglosajón: 74,164.14 -> quitar comas de miles
                s = s.replace(",", "");
            }
        } else if (s.contains(",")) {
            // Solo contiene coma decimal: 74164,14 -> reemplazar coma por punto
            s = s.replace(",", ".");
        } else if (s.contains(".")) {
            // Si contiene puntos y hay más de uno (ej. 1.234.567 sin decimales), quitar puntos de miles
            if (s.indexOf('.') != s.lastIndexOf('.')) {
                s = s.replace(".", "");
            }
        }

        try {
            double val = Double.parseDouble(s);
            double result = negative ? -Math.abs(val) : Math.abs(val);
            return Math.round(result * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double parseMonetaryAmount(String raw) {
        return parseDoubleValue(raw);
    }

    public TipoPago parseTipoPago(String rawTipoPago) {
        if (rawTipoPago == null || rawTipoPago.isBlank()) {
            return TipoPago.NO_ESPECIFICADO;
        }
        String clean = ArgentineDataNormalizer.stripAccentsAndClean(rawTipoPago);
        if (clean.equals("EFECTIVO") || clean.equals("AG") || clean.equals("GC") || clean.equals("CR")) {
            return TipoPago.EFECTIVO;
        }
        if (clean.contains("TC") || clean.contains("TARJETA") || clean.contains("MASTERCARD")
                || clean.contains("VISA") || clean.contains("AMEX") || clean.contains("CREDITO")) {
            return TipoPago.TARJETA_CREDITO;
        }
        if (clean.contains("TD") || (clean.contains("DEBITO") && !clean.contains("CBU") && !clean.contains("AUTOMATICO"))) {
            return TipoPago.TARJETA_DEBITO;
        }
        if (clean.contains("DD") || clean.contains("CBU") || clean.contains("DEBITO AUTOMATICO")) {
            return TipoPago.DEBITO_CBU;
        }
        return TipoPago.NO_ESPECIFICADO;
    }

    public NombreApellido splitNombreApellido(String rawNombreCompleto, boolean esPersonaJuridica) {
        if (rawNombreCompleto == null || rawNombreCompleto.isBlank()) {
            return new NombreApellido("-", "-");
        }
        String clean = ArgentineDataNormalizer.toTitleCase(rawNombreCompleto.trim());
        if (esPersonaJuridica) {
            return new NombreApellido("-", clean);
        }
        if (clean.contains(",")) {
            String[] parts = clean.split(",", 2);
            String apellido = parts[0].trim();
            String nombre = parts.length > 1 ? parts[1].trim() : "-";
            if (nombre.isEmpty()) nombre = "-";
            return new NombreApellido(nombre, apellido);
        }
        String[] words = clean.split("\\s+");
        if (words.length == 1) {
            return new NombreApellido("-", words[0]);
        }
        String apellido = words[0];
        StringBuilder nombreSb = new StringBuilder();
        for (int i = 1; i < words.length; i++) {
            if (i > 1) nombreSb.append(" ");
            nombreSb.append(words[i]);
        }
        return new NombreApellido(nombreSb.toString(), apellido);
    }

    /**
     * Mapea una fila cruda a un item de póliza según la configuración de mapeo.
     */
    public ParsedPolizaItem mapRowToPolizaItem(Map<String, String> row, ImportMappingConfigDTO config) {
        Map<String, String> cm = config.getColumnMapping();

        String nroPza = getVal(row, cm.get("nroPoliza"));
        if (nroPza == null || nroPza.isBlank() || nroPza.toUpperCase().contains("TOTAL")) {
            return null;
        }

        String rawEndoso = getVal(row, cm.get("nroEndoso"));
        Integer nroEndoso = 0;
        if (rawEndoso != null && !rawEndoso.isBlank()) {
            try {
                nroEndoso = Integer.parseInt(rawEndoso.replaceAll("\\D", ""));
            } catch (Exception ignored) {}
        }

        String tomador = getVal(row, cm.get("tomadorNombre"));
        String asegurado = getVal(row, cm.get("aseguradoNombre"));
        String doc = getVal(row, cm.get("documento"));
        String domicilio = getVal(row, cm.get("domicilio"));
        String localidad = getVal(row, cm.get("localidad"));
        String provincia = getVal(row, cm.get("provincia"));
        String telefono = getVal(row, cm.get("telefono"));
        String fechaNac = getVal(row, cm.get("fechaNacimiento"));

        LocalDate inicio = parseFlexibleDate(getVal(row, cm.get("inicioVigencia")));
        LocalDate fin = parseFlexibleDate(getVal(row, cm.get("finVigencia")));
        LocalDate fechaEmision = parseFlexibleDate(getVal(row, cm.get("fechaEmision")));

        // Mapfre: "ANUALIDAD" y "FECHA VIG." (finVigencia)
        Integer anualidad = null;
        String rawAnualidad = getVal(row, cm.get("anualidad"));
        if (rawAnualidad == null || rawAnualidad.isBlank()) {
            rawAnualidad = getVal(row, "ANUALIDAD");
        }
        if (rawAnualidad != null && !rawAnualidad.isBlank()) {
            try {
                anualidad = Integer.parseInt(rawAnualidad.replaceAll("\\D", ""));
            } catch (Exception ignored) {}
        }

        if (anualidad != null && fin != null) {
            inicio = fin.minusYears(1);
            fechaEmision = fin.minusYears(Math.max(1, anualidad));
        } else {
            // Inferencia bidireccional de vigencia
            if (fin != null && inicio == null) {
                inicio = fin.minusYears(1);
            } else if (inicio != null && fin == null) {
                fin = inicio.plusYears(1);
            }
        }

        if (fechaEmision == null) {
            fechaEmision = inicio;
        }

        String ramoPrin = getVal(row, cm.get("ramoPrincipal"));
        String ramoSec = getVal(row, cm.get("ramoSecundarioPlan"));
        String bienAseg = getVal(row, cm.get("bienAsegurado"));
        String tipoFact = getVal(row, cm.get("tipoFacturacion"));
        TipoPago tipoPago = parseTipoPago(getVal(row, cm.get("tipoPago")));

        Double premio = parseDoubleValue(getVal(row, cm.get("premio")));
        Double fallbackCentavos = parseDoubleValue(getVal(row, cm.get("premioFallbackCentavos")));
        if ((premio == null || Math.abs(premio) < 0.0001) && fallbackCentavos != null && Math.abs(fallbackCentavos) > 0.0001) {
            premio = Math.round((fallbackCentavos / 100.0) * 100.0) / 100.0;
        } else if (premio != null) {
            premio = Math.round(premio * 100.0) / 100.0;
        }

        Double sumaAsegurada = parseDoubleValue(getVal(row, cm.get("sumaAsegurada")));

        return ParsedPolizaItem.builder()
            .nroPza(nroPza)
            .nroEndoso(nroEndoso)
            .tomadorNombre(tomador)
            .aseguradoNombre(asegurado)
            .rawDocumento(doc)
            .domicilio(domicilio)
            .rawLocalidad(localidad)
            .rawProvincia(provincia)
            .telefono(telefono)
            .rawFechaNacimiento(fechaNac)
            .inicioVigencia(inicio)
            .finVigencia(fin)
            .fechaEmision(fechaEmision)
            .rawRamoPrincipal(ramoPrin)
            .rawRamoSecundarioPlan(ramoSec)
            .bienAsegurado(bienAseg)
            .tipoFacturacion(tipoFact)
            .tipoPago(tipoPago)
            .premio(premio)
            .sumaAsegurada(sumaAsegurada)
            .activo(true)
            .build();
    }

    public String getVal(Map<String, String> row, String colName) {
        if (colName == null || colName.isBlank() || row == null) return null;
        String val = row.get(colName);
        if (val != null) return val;

        String colTrimmed = colName.trim();
        val = row.get(colTrimmed);
        if (val != null) return val;

        String targetClean = ArgentineDataNormalizer.stripAccentsAndClean(colName);
        for (Map.Entry<String, String> entry : row.entrySet()) {
            if (entry.getKey() != null && ArgentineDataNormalizer.stripAccentsAndClean(entry.getKey()).equals(targetClean)) {
                return entry.getValue();
            }
        }

        // Fallback ignorando signos de puntuación (. - _ /)
        String targetNoPunct = targetClean.replaceAll("[.\\-_/]", "").replaceAll("\\s+", " ").trim();
        for (Map.Entry<String, String> entry : row.entrySet()) {
            if (entry.getKey() != null) {
                String keyNoPunct = ArgentineDataNormalizer.stripAccentsAndClean(entry.getKey()).replaceAll("[.\\-_/]", "").replaceAll("\\s+", " ").trim();
                if (keyNoPunct.equals(targetNoPunct)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Agrupa y colapsa las filas por número de póliza, calculando endosos y anulaciones.
     */
    public List<ParsedPolizaItem> collapseRowsByPoliza(List<ParsedPolizaItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();

        Map<String, List<ParsedPolizaItem>> grouped = new LinkedHashMap<>();
        for (ParsedPolizaItem item : items) {
            if (item == null || item.getNroPza() == null || item.getNroPza().isBlank()) continue;
            grouped.computeIfAbsent(item.getNroPza(), k -> new ArrayList<>()).add(item);
        }

        List<ParsedPolizaItem> result = new ArrayList<>();

        for (List<ParsedPolizaItem> group : grouped.values()) {
            if (group.size() == 1) {
                ParsedPolizaItem single = group.get(0);
                Double p = single.getPremio();
                Double s = single.getSumaAsegurada();
                if ((p != null && p < 0) || (s != null && s < 0)) {
                    single.setActivo(false);
                    single.setPremio(Math.abs(p != null ? p : 0.0));
                } else {
                    single.setActivo(true);
                }
                result.add(single);
            } else {
                // Ordenar por endoso ascendente
                group.sort(Comparator.comparing(item -> item.getNroEndoso() != null ? item.getNroEndoso() : 0));

                double sumaPremios = 0.0;
                double maxPremioPositivo = 0.0;
                int nonZeroMovements = 0;
                Double lastValidPremioPositivo = null;

                for (ParsedPolizaItem row : group) {
                    Double pr = row.getPremio();
                    if (pr != null && Math.abs(pr) > 0.001) {
                        sumaPremios += pr;
                        nonZeroMovements++;
                        if (pr > 0) {
                            if (pr > maxPremioPositivo) maxPremioPositivo = pr;
                            lastValidPremioPositivo = pr;
                        }
                    }
                }

                ParsedPolizaItem latest = group.get(group.size() - 1);
                LocalDate earliestEmision = group.stream()
                    .map(ParsedPolizaItem::getFechaEmision)
                    .filter(java.util.Objects::nonNull)
                    .min(java.util.Comparator.naturalOrder())
                    .orElse(latest.getFechaEmision() != null ? latest.getFechaEmision() : latest.getInicioVigencia());

                ParsedPolizaItem collapsed = ParsedPolizaItem.builder()
                    .nroPza(latest.getNroPza())
                    .nroEndoso(latest.getNroEndoso())
                    .tomadorNombre(latest.getTomadorNombre())
                    .aseguradoNombre(latest.getAseguradoNombre())
                    .rawDocumento(latest.getRawDocumento())
                    .domicilio(latest.getDomicilio())
                    .rawLocalidad(latest.getRawLocalidad())
                    .rawProvincia(latest.getRawProvincia())
                    .telefono(latest.getTelefono())
                    .rawFechaNacimiento(latest.getRawFechaNacimiento())
                    .inicioVigencia(latest.getInicioVigencia())
                    .finVigencia(latest.getFinVigencia())
                    .fechaEmision(earliestEmision)
                    .rawRamoPrincipal(latest.getRawRamoPrincipal())
                    .rawRamoSecundarioPlan(latest.getRawRamoSecundarioPlan())
                    .bienAsegurado(latest.getBienAsegurado())
                    .tipoFacturacion(latest.getTipoFacturacion())
                    .tipoPago(latest.getTipoPago())
                    .sumaAsegurada(latest.getSumaAsegurada())
                    .build();

                // Anulación por contra-endoso (ej. +$63.762 y -$63.762) o última suma negativa
                boolean esAnulada = (nonZeroMovements > 1 && Math.abs(sumaPremios) < 1.0)
                        || (latest.getSumaAsegurada() != null && latest.getSumaAsegurada() < 0);

                if (esAnulada) {
                    collapsed.setActivo(false);
                    collapsed.setPremio(maxPremioPositivo > 0 ? maxPremioPositivo : Math.abs(sumaPremios));
                } else {
                    collapsed.setActivo(true);
                    if (latest.getPremio() != null && latest.getPremio() > 0) {
                        collapsed.setPremio(latest.getPremio());
                    } else if (lastValidPremioPositivo != null) {
                        collapsed.setPremio(lastValidPremioPositivo);
                    } else {
                        collapsed.setPremio(latest.getPremio() != null ? Math.abs(latest.getPremio()) : 0.0);
                    }
                }

                // Si aseguradoNombre != tomadorNombre (ej. colectivos Noble con empleados), almacenar en bienAsegurado
                if (collapsed.getAseguradoNombre() != null && !collapsed.getAseguradoNombre().isBlank()
                        && !collapsed.getAseguradoNombre().equalsIgnoreCase(collapsed.getTomadorNombre())) {
                    if (collapsed.getBienAsegurado() == null || collapsed.getBienAsegurado().isBlank()) {
                        collapsed.setBienAsegurado(collapsed.getAseguradoNombre());
                    }
                }

                result.add(collapsed);
            }
        }

        return result;
    }
}
