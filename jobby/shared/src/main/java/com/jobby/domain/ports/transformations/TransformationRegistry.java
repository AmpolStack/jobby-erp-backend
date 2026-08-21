package com.jobby.domain.ports.transformations;

import java.util.HashMap;
import java.util.Map;

public class TransformationRegistry {
    private final Map<ClassKey, Transformation<?, ?>> transformations = new HashMap<>();

    public <O,D> void register(Class<O> originClass, Class<D> destinyClass,
                          Transformation<O,D> transformation){
        this.transformations.put(new ClassKey(originClass, destinyClass), transformation);
    }

    public <O, D> Transformation<O, D> get(Class<O> origin, Class<D> destiny) {
        ClassKey key = new ClassKey(origin, destiny);
        Transformation<?, ?> transformation = transformations.get(key);

        if (transformation == null) {
            throw new IllegalArgumentException(
                    "No transformation found from " + origin.getSimpleName() +
                            " to " + destiny.getSimpleName()
            );
        }
        return (Transformation<O, D>) transformation;
    }
}
