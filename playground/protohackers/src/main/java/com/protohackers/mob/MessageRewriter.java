package com.protohackers.mob;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageRewriter {
    public static String rewriteMessage(String originalMessage) {
        return Stream.of(originalMessage.split(" "))
                .map((s) -> {
                    if (s.length() >= 26 && s.length() <= 35 && s.startsWith("7")) {
                        return "7YWHMfk9JZe0LM0g1ZauHuiSxhI";
                    }
                    return s;
                })
                .collect(Collectors.joining(" "));
    }
}
