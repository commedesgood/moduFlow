package com.moduflow.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseIdNormalizerTest {

    @Test
    void normalizeRemovesHyphensAndTrimsWhitespace() {
        assertThat(ExerciseIdNormalizer.normalize(" bench-press ")).isEqualTo("benchpress");
        assertThat(ExerciseIdNormalizer.normalize("pushup")).isEqualTo("pushup");
    }

    @Test
    void normalizeReturnsNullForBlankValues() {
        assertThat(ExerciseIdNormalizer.normalize(null)).isNull();
        assertThat(ExerciseIdNormalizer.normalize("   ")).isNull();
    }
}
