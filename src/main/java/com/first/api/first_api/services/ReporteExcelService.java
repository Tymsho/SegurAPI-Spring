package com.first.api.first_api.services;

import com.first.api.first_api.models.Cliente;
import com.first.api.first_api.models.Poliza;
import com.first.api.first_api.repositories.ClienteRepository;
import com.first.api.first_api.repositories.PolizaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReporteExcelService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PolizaRepository polizaRepository;

    public byte[] generarExcelPolizasPorCliente() {
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        
        List<Cliente> clientes = clienteRepository.findByActivoTrueAndProductorEmail(emailLogueado, Pageable.unpaged()).getContent();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (Cliente cliente : clientes) {
                String sheetName = (cliente.getNombre() + " " + cliente.getApellido() + " " + cliente.getId()).replaceAll("[^a-zA-Z0-9 ]", " ").trim();
                if (sheetName.length() > 30) sheetName = sheetName.substring(0, 30);
                
                Sheet sheet = workbook.createSheet(sheetName);

                Row headerRow = sheet.createRow(0);
                String[] columns = {"Nro Póliza", "Ramo", "Compañía", "Inicio Vigencia", "Fin Vigencia", "Prima", "Premio", "Estado"};
                
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                List<Poliza> polizas = polizaRepository.findMisPolizasFiltradas(emailLogueado, cliente.getId(), null, null, Pageable.unpaged()).getContent();
                
                int rowIdx = 1;
                for (Poliza poliza : polizas) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(poliza.getNroPza());
                    row.createCell(1).setCellValue(poliza.getRamo() != null ? poliza.getRamo().getNombre() : "");
                    row.createCell(2).setCellValue(poliza.getCompania() != null ? poliza.getCompania().getNombre() : "");
                    row.createCell(3).setCellValue(poliza.getInicioVigencia() != null ? poliza.getInicioVigencia().toString() : "");
                    row.createCell(4).setCellValue(poliza.getFinVigencia() != null ? poliza.getFinVigencia().toString() : "");
                    row.createCell(5).setCellValue(poliza.getPrima() != null ? poliza.getPrima() : 0.0);
                    row.createCell(6).setCellValue(poliza.getPremio() != null ? poliza.getPremio() : 0.0);
                    row.createCell(7).setCellValue(poliza.isActivo() ? "Activa" : "Anulada");
                }

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            if (clientes.isEmpty()) {
                workbook.createSheet("Sin Clientes");
            }

            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
    }
}
