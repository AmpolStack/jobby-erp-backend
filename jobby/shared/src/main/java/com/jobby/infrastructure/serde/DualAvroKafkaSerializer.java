package com.jobby.infrastructure.serde;

import io.apicurio.registry.resolver.SchemaLookupResult;
import io.apicurio.registry.resolver.SchemaResolver;
import io.apicurio.registry.resolver.strategy.ArtifactReferenceResolverStrategy;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import io.apicurio.registry.serde.data.KafkaSerdeMetadata;
import io.apicurio.registry.serde.data.KafkaSerdeRecord;
import org.apache.avro.Schema;
import org.apache.kafka.common.header.Headers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class DualAvroKafkaSerializer<U> extends AvroKafkaSerializer<U> {

    public DualAvroKafkaSerializer() {
        super();
    }

    public DualAvroKafkaSerializer(RegistryClient client) {
        super(client);
    }

    public DualAvroKafkaSerializer(SchemaResolver<Schema, U> schemaResolver) {
        super(schemaResolver);
    }

    public DualAvroKafkaSerializer(RegistryClient client,
                                    ArtifactReferenceResolverStrategy<Schema, U> artifactResolverStrategy,
                                    SchemaResolver<Schema, U> schemaResolver) {
        super(client, artifactResolverStrategy, schemaResolver);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, U data) {
        if (data == null) {
            return null;
        }
        try {
            KafkaSerdeMetadata resolverMetadata = new KafkaSerdeMetadata(topic, isKey(), headers);
            SchemaLookupResult<Schema> schema = getSchemaResolver().resolveSchema(
                    new KafkaSerdeRecord<>(resolverMetadata, data));

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            out.write(MAGIC_BYTE);
            getIdHandler().writeId(schema.toArtifactReference(), out);
            serializeData(headers, schema.getParsedSchema(), data, out);

            if (headersHandler != null && headers != null) {
                headersHandler.writeHeaders(headers, schema.toArtifactReference());
            }

            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
