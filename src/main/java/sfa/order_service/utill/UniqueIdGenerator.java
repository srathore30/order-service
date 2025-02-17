package sfa.order_service.utill;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UniqueIdGenerator {

    public static String generateUniqueId() {
        UUID uuid = UUID.randomUUID();
        StringBuilder uniqueId = new StringBuilder(Long.toString(uuid.getMostSignificantBits() & Long.MAX_VALUE, 36)
                .toUpperCase());
        while (uniqueId.length() < 10) {
            uniqueId.insert(0, "0");
        }
        return uniqueId.substring(0, 10);
    }
}
