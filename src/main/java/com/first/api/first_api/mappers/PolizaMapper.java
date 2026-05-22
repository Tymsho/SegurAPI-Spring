package com.first.api.first_api.mappers;

import com.first.api.first_api.models.Poliza;
import com.first.api.first_api.dto.PolizaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PolizaMapper {

    @Mapping(source = "tomador.id", target = "clienteId")
    @Mapping(target = "nombreCliente", expression = "java(poliza.getTomador() != null ? poliza.getTomador().getNombre() + \" \" + poliza.getTomador().getApellido() : null)")
    @Mapping(source = "compania.id", target = "companiaId")
    @Mapping(source = "compania.nombre", target = "nombreCompania")
    @Mapping(source = "ramo.id", target = "ramoId")
    @Mapping(source = "ramo.nombre", target = "nombreRamo")
    PolizaDTO toDTO(Poliza poliza);

    @Mapping(target = "tomador", ignore = true)
    @Mapping(target = "compania", ignore = true)
    @Mapping(target = "ramo", ignore = true)
    @Mapping(target = "productor", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Poliza toEntity(PolizaDTO dto);
}
