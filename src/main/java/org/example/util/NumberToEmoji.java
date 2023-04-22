package org.example.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NumberToEmoji {

    private Map<Integer, String> numbersMap = new HashMap<>();

    public NumberToEmoji() {
        numbersMap.put(0, "0️⃣");
        numbersMap.put(1, "1️⃣");
        numbersMap.put(2, "2️⃣");
        numbersMap.put(3, "3️⃣");
        numbersMap.put(4, "4️⃣");
        numbersMap.put(5, "5️⃣");
        numbersMap.put(6, "6️⃣");
        numbersMap.put(7, "7️⃣");
        numbersMap.put(8, "8️⃣");
        numbersMap.put(9, "9️⃣");
    }

    public String toEmoji(long number) {
        StringBuilder emojiString = new StringBuilder();
        while (number > 0) {
            int digit = (int) (number % 10);
            number /= 10;
            String nextNumber = numbersMap.get(digit);
            emojiString.insert(0, nextNumber);
        }
        return emojiString.toString();
    }
}
