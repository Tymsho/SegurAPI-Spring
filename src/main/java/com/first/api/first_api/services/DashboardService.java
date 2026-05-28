package com.first.api.first_api.services;

import com.first.api.first_api.dtoresponse.DashboardStatsResponse;
import com.first.api.first_api.repositories.ClienteRepository;
import com.first.api.first_api.repositories.PolizaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PolizaRepository polizaRepository;

    public DashboardStatsResponse obtenerEstadisticasDashboard() {
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setCantClientesActivos(clienteRepository.countActiveClientesByProductorEmail(emailLogueado));
        response.setCantPolizasActivas(polizaRepository.countActivePolizasByProductorEmail(emailLogueado));
        response.setTotalPremios(polizaRepository.sumPremioByProductorEmail(emailLogueado));
        response.setTotalPrimas(polizaRepository.sumPrimaByProductorEmail(emailLogueado));
        response.setPolizasPorMes(polizaRepository.getPolizasPorMes(emailLogueado));
        response.setPolizasPorCompania(polizaRepository.getPolizasPorCompania(emailLogueado));
        response.setPolizasPorRamo(polizaRepository.getPolizasPorRamo(emailLogueado));
        response.setPolizasPorTipoPago(polizaRepository.getPolizasPorTipoPago(emailLogueado));

        return response;
    }
}
