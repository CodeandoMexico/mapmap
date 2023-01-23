package org.codeandomexico.mapmap.server.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
public class EncodedPolylineBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String points;

    private String levels;

    private int length;

    public EncodedPolylineBean(String points, String levels, int length) {
        this.points = points;
        this.levels = levels;
        this.length = length;
    }

    public String getLevels(int defaultLevel) {
        if (levels == null) {
            StringBuilder b = new StringBuilder();
            String l = encodeNumber(defaultLevel);
            b.append(l.repeat(Math.max(0, length)));
            return b.toString();
        }
        return levels;
    }

    private static String encodeNumber(int num) {
        StringBuilder encodeString = new StringBuilder();
        while (num >= 0x20) {
            int nextValue = (0x20 | (num & 0x1f)) + 63;
            encodeString.append((char) (nextValue));
            num >>= 5;
        }
        num += 63;
        encodeString.append((char) (num));
        return encodeString.toString();
    }
}
