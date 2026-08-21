package com.jobby.infrastructure.adapter;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SafeResultValidatorAdapter - Unit Tests")
@ExtendWith(MockitoExtension.class)
class SafeResultValidatorAdapterTest {

    @Mock
    private Validator validator;

    @Mock
    private ConstraintViolation<Object> violation1;

    @Mock
    private ConstraintViolation<Object> violation2;

    private SafeResultValidatorAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SafeResultValidatorAdapter(validator);
    }

    @Test
    @DisplayName("validate: no violations returns success")
    void givenEntityWithoutViolations_whenValidate_returnsSuccess() {
        when(validator.validate(this)).thenReturn(Set.of());

        var result = adapter.validate(this);

        ResultAssertions.assertSuccess(result);
    }

    @Test
    @DisplayName("validate: single violation returns VALIDATION_ERROR with field")
    void givenEntityWithOneViolation_whenValidate_returnsValidationError() {
        var path = mock(jakarta.validation.Path.class);

        when(path.toString()).thenReturn("name");
        when(violation1.getPropertyPath()).thenReturn(path);
        when(violation1.getMessage()).thenReturn("must not be blank");

        doReturn(Set.of(violation1)).when(validator).validate(this);

        var result = adapter.validate(this);

        ResultAssertions.assertFailure(result, ErrorType.VALIDATION_ERROR);
        assertThat(result.error().getFields()).hasSize(1);
    }

    @Test
    @DisplayName("validate: multiple violations return all fields")
    void givenEntityWithMultipleViolations_whenValidate_returnsAllFields() {
        var path1 = mock(jakarta.validation.Path.class);
        var path2 = mock(jakarta.validation.Path.class);

        when(path1.toString()).thenReturn("name");
        when(path2.toString()).thenReturn("email");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("must not be null");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("must be valid");

        doReturn(Set.of(violation1, violation2)).when(validator).validate(this);

        var result = adapter.validate(this);

        ResultAssertions.assertFailure(result, ErrorType.VALIDATION_ERROR);
        assertThat(result.error().getFields()).hasSize(2);
    }

    @Test
    @DisplayName("validate: violation field uses property path toString as instance")
    void givenViolationWithPropertyPath_whenValidate_fieldInstanceMatchesProperty() {
        var path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("email");
        when(violation1.getPropertyPath()).thenReturn(path);
        when(violation1.getMessage()).thenReturn("must be a valid email address");
        doReturn(Set.of(violation1)).when(validator).validate(this);

        var result = adapter.validate(this);

        ResultAssertions.assertFailure(result);
        assertThat(result.error().getFields())
                .anySatisfy(field -> assertThat(field.getInstance()).isEqualTo("email"));
    }
}
