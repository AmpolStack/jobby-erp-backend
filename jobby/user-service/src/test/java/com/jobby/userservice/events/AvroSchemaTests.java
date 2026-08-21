package com.jobby.userservice.events;

import org.apache.avro.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AvroSchemaTests {

    @Nested
    @DisplayName("UserCreatedSchema")
    class UserCreatedSchemaTests {

        @Test
        @DisplayName("builder creates record with all fields")
        void builderCreatesRecordWithAllFields() {
            var userId = 100L;
            var name = ByteBuffer.wrap("John Doe".getBytes(StandardCharsets.UTF_8));
            var email = ByteBuffer.wrap("john@example.com".getBytes(StandardCharsets.UTF_8));
            var createdAt = Instant.now();

            var record = UserCreatedSchema.newBuilder()
                    .setUserId(userId)
                    .setName(name)
                    .setEmail(email)
                    .setCreatedAt(createdAt)
                    .build();

            assertEquals(userId, record.getUserId());
            assertEquals(name, record.getName());
            assertEquals(email, record.getEmail());
            assertEquals(createdAt.truncatedTo(java.time.temporal.ChronoUnit.MILLIS), record.getCreatedAt());
        }

        @Test
        @DisplayName("serialization and deserialization roundtrip preserves data")
        void serializationRoundtripPreservesData() throws Exception {
            var userId = 200L;
            var name = ByteBuffer.wrap("Jane Doe".getBytes(StandardCharsets.UTF_8));
            var email = ByteBuffer.wrap("jane@example.com".getBytes(StandardCharsets.UTF_8));
            var createdAt = Instant.now();

            var original = UserCreatedSchema.newBuilder()
                    .setUserId(userId)
                    .setName(name)
                    .setEmail(email)
                    .setCreatedAt(createdAt)
                    .build();

            var bytes = original.toByteBuffer();
            var restored = UserCreatedSchema.fromByteBuffer(bytes);

            assertEquals(original.getUserId(), restored.getUserId());
            assertArrayEquals(original.getName().array(), restored.getName().array());
            assertArrayEquals(original.getEmail().array(), restored.getEmail().array());
            assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        }

        @Test
        @DisplayName("has valid Avro schema")
        void hasValidAvroSchema() {
            var schema = UserCreatedSchema.getClassSchema();
            assertNotNull(schema);
            assertEquals("UserCreatedSchema", schema.getName());
            assertEquals("com.jobby.userservice.events", schema.getNamespace());
            assertEquals(Schema.Type.RECORD, schema.getType());
            assertEquals(4, schema.getFields().size());
        }

        @Test
        @DisplayName("SpecificRecord get/put by index works")
        void specificRecordGetPutByIndexWorks() {
            var userId = 300L;
            var name = ByteBuffer.wrap("Alice".getBytes(StandardCharsets.UTF_8));
            var email = ByteBuffer.wrap("alice@example.com".getBytes(StandardCharsets.UTF_8));
            var createdAt = Instant.ofEpochMilli(123456789L);

            var record = new UserCreatedSchema(userId, name, email, createdAt);

            assertEquals(userId, record.get(0));
            assertEquals(name, record.get(1));
            assertEquals(email, record.get(2));
            assertEquals(createdAt.truncatedTo(java.time.temporal.ChronoUnit.MILLIS), record.get(3));
        }
    }

    @Nested
    @DisplayName("EmailChangeRequest")
    class EmailChangeRequestTests {

        @Test
        @DisplayName("builder creates record with all fields")
        void builderCreatesRecordWithAllFields() {
            var email = ByteBuffer.wrap("user@example.com".getBytes(StandardCharsets.UTF_8));
            var code = "ABC123";

            var record = EmailChangeRequestSchema.newBuilder()
                    .setEmail(email)
                    .setCode(code)
                    .build();

            assertEquals(email, record.getEmail());
            assertEquals(code, record.getCode());
        }

        @Test
        @DisplayName("serialization and deserialization roundtrip preserves data")
        void serializationRoundtripPreservesData() throws Exception {
            var email = ByteBuffer.wrap("test@example.com".getBytes(StandardCharsets.UTF_8));
            var code = "XYZ789";

            var original = EmailChangeRequestSchema.newBuilder()
                    .setEmail(email)
                    .setCode(code)
                    .build();

            var bytes = original.toByteBuffer();
            var restored = EmailChangeRequestSchema.fromByteBuffer(bytes);

            assertArrayEquals(original.getEmail().array(), restored.getEmail().array());
            assertEquals(original.getCode(), restored.getCode());
        }

        @Test
        @DisplayName("has valid Avro schema")
        void hasValidAvroSchema() {
            var schema = EmailChangeRequestSchema.getClassSchema();
            assertNotNull(schema);
            assertEquals("EmailChangeRequestSchema", schema.getName());
            assertEquals("com.jobby.userservice.events", schema.getNamespace());
            assertEquals(Schema.Type.RECORD, schema.getType());
            assertEquals(2, schema.getFields().size());
        }

        @Test
        @DisplayName("code field is java.lang.String")
        void codeFieldIsJavaString() {
            var record = new EmailChangeRequestSchema(
                    ByteBuffer.wrap("a@b.com".getBytes(StandardCharsets.UTF_8)),
                    "CODE123"
            );
            assertInstanceOf(String.class, record.getCode());
        }
    }
}
