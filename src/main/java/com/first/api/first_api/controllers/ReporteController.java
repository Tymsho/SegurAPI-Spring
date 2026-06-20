package com.first.api.first_api.controllers;

import com.first.api.first_api.services.ReporteExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteExcelService reporteExcelService;

    @GetMapping("/polizas/excel")
    public ResponseEntity<byte[]> descargarExcelPolizas() {
        byte[] excelContent = reporteExcelService.generarExcelPolizasPorCliente();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "polizas_por_cliente.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }
}
