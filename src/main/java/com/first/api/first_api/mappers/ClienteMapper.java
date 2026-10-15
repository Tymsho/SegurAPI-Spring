package com.first.api.first_api.mappers;

import com.first.api.first_api.models.Cliente;
import com.first.api.first_api.dtorequest.ClienteRequest;
import com.first.api.first_api.dtoresponse.ClienteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {
    
    @Mapping(source = "localidad.id", target = "localidadId")
    ClienteResponse toResponse(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "localidad", ignore = true)
    @Mapping(target = "productor", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Cliente toEntity(ClienteRequest dto);
}
