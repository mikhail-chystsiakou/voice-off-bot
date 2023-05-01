package org.example.converter;

public interface Converter<Entity, DTO> {

    Entity fromDTO(DTO dto);

    DTO toDTO(Entity entity);
}
