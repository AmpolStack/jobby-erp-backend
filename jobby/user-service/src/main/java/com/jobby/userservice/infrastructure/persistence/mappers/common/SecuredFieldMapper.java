package com.jobby.userservice.infrastructure.mappers.common;

import com.jobby.infrastructure.security.fields.IndexedField;
import com.jobby.infrastructure.security.fields.ProtectedField;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SecuredFieldMapper {

    @Named("fromIndexedField")
    default String fromIndexedField(IndexedField indexedField){
        if(indexedField == null){
            return null;
        }
        return indexedField.getPayload();
    }

    @Named("fromProtectedField")
    default String fromProtectedField(ProtectedField protectedField){
        if(protectedField == null){
            return null;
        }
        return protectedField.getPayload();
    }

    @Named("toProtectedField")
    default ProtectedField toProtectedField(String source){
        if(source == null) return null;
        return new ProtectedField(source, null);
    }

    @Named("toIndexedField")
    default IndexedField toIndexedField(String source){
        if(source == null) return null;
        return new IndexedField(source, null,null);
    }
}
