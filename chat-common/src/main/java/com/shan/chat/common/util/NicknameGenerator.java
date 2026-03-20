package com.shan.chat.common.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 랜덤 닉네임 생성 유틸
 * 형용사 + 동물 명사 조합으로 생성한다.
 */
public final class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "행복한", "신나는", "활발한", "귀여운", "용감한",
            "재빠른", "조용한", "빛나는", "따뜻한", "멋진",
            "즐거운", "강인한", "섬세한", "유쾌한", "당당한"
    );

    private static final List<String> NOUNS = List.of(
            "고양이", "강아지", "토끼", "호랑이", "곰",
            "여우", "늑대", "판다", "사자", "펭귄",
            "독수리", "돌고래", "코끼리", "기린", "너구리"
    );

    private NicknameGenerator() {
    }

    public static String generate() {
        String adj = ADJECTIVES.get(ThreadLocalRandom.current().nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(ThreadLocalRandom.current().nextInt(NOUNS.size()));
        return adj + noun;
    }
}

