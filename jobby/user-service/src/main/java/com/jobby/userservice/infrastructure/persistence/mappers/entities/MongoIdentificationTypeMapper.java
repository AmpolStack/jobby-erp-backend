package com.jobby.userservice.infrastructure.mappers.entities;
import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.infrastructure.persistence.entities.MongoIdentificationTypeEntity;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MongoIdentificationTypeMapper {

    default IdentificationType toDomain(MongoIdentificationTypeEntity entity){
        if(entity == null) return null;
        return IdentificationType.reconstruct(entity.getId(),
                entity.getDianCode(),
                entity.getName(),
                entity.getMinLength(),
                entity.getMaxLength(),
                entity.getExpression(),
                entity.getAbbreviation(),
                entity.getAllowCharacters());
    }

    List<IdentificationType> toDomain(List<MongoIdentificationTypeEntity> entity);

    MongoIdentificationTypeEntity toEntity(IdentificationType domain);
}
