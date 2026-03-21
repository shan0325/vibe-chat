package com.shan.chat.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class NicknameGeneratorTest {

    @Test
    @DisplayName("생성된 닉네임은 null 또는 빈 값이 아니다")
    void generate_notBlank() {
        String nickname = NicknameGenerator.generate();

        assertThat(nickname).isNotNull().isNotBlank();
    }

    @RepeatedTest(10)
    @DisplayName("닉네임은 형용사+명사 형태로 공백 없이 생성된다")
    void generate_noWhitespace() {
        String nickname = NicknameGenerator.generate();

        assertThat(nickname).doesNotContain(" ");
    }

    @Test
    @DisplayName("반복 생성 시 다양한 조합이 만들어진다")
    void generate_producesDifferentResults() {
        Set<String> results = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            results.add(NicknameGenerator.generate());
        }

        // 15 * 15 = 225 조합 중 100번 시도에서 최소 10가지 이상의 서로 다른 닉네임
        assertThat(results.size()).isGreaterThan(10);
    }
}

