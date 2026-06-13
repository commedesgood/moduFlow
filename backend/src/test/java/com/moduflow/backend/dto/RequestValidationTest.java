package com.moduflow.backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void workoutItemRejectsBlankNameAndNegativeNumbers() {
        WorkoutItemDto request = new WorkoutItemDto(
                null,
                "squat",
                " ",
                null,
                -1,
                -2,
                -10.0
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "sets", "reps", "weight");
    }

    @Test
    void routineItemRejectsTooLongName() {
        RoutineItemDto request = new RoutineItemDto(
                null,
                "a".repeat(101),
                3,
                10,
                20.0,
                "pushup"
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void routineScheduleRejectsInvalidRestDays() {
        RoutineScheduleDto request = new RoutineScheduleDto(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of("thu", "holiday")
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("restDays[1].<list element>");
    }

    @Test
    void routineScheduleAllowsMissingRestDays() {
        RoutineScheduleDto request = new RoutineScheduleDto(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void workoutCountRejectsLargeDelta() {
        WorkoutCountRequest request = new WorkoutCountRequest(101);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("delta");
    }
}
