package com.jobby.domain.ports.transformations;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import lombok.Getter;
import java.lang.reflect.ParameterizedType;

public abstract class Transformation<O,D> {

    @Getter
    private final Class<O> originClass;
    @Getter
    private final Class<D> destinyClass;
    private final TransformationRegistry registry;

    protected Transformation(TransformationRegistry registry) {
        this.registry = registry;
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.originClass = (Class<O>) genericSuperclass.getActualTypeArguments()[0];
        this.destinyClass = (Class<D>) genericSuperclass.getActualTypeArguments()[1];
        register();
    }

    private void register() {
        this.registry.register(originClass, destinyClass, this);
    }

    public abstract Result<D, Error> transform(O origin);
}
