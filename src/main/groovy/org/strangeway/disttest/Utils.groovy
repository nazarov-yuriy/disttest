package org.strangeway.disttest

import groovy.xml.MarkupBuilder
import java.security.MessageDigest

class Utils {
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    static String calcHash(byte[] bytes) {
        byte[] hash = MessageDigest
                .getInstance("SHA1")
                .digest(bytes)

        char[] chars = new char[hash.length * 2];
        for (int i = 0; i < hash.length; i++) {
            chars[i * 2] = HEX_DIGITS[(hash[i] >> 4) & 0xf];
            chars[i * 2 + 1] = HEX_DIGITS[hash[i] & 0xf];
        }
        return new String(chars);
    }

    static String calcHash(String str) {
        calcHash(str.getBytes())
    }

    public static void renderReport(List<List<String>> versions, Map<String, String> colors, String reportName) {
        final int columns = 14 //subLevels columns
        def writer = new StringWriter()
        def markupBuilder = new MarkupBuilder(writer)
        def height = 60 * (versions.collect({ Integer.max((it.size() - 1 + columns - 1) / columns as int, 1) }).sum() as int)
        def width = 130 * (columns + 1)
        writer << '<?xml version="1.0" encoding="UTF-8" ?>\n'
        markupBuilder.svg(width: width, height: height, version: "1.1") {
            int y = 10
            versions.eachWithIndex { version, versionIndex ->
                int x = 10 - 130
                if (versionIndex != 0) {
                    markupBuilder.line(x1: x + 50, y1: y - 30, x2: x + 50, y2: y, stroke: "black", "stroke-width": 1,)
                }
                version.eachWithIndex { patch, patchIndex ->
                    if (patchIndex > 1 && (patchIndex - 1) % columns == 0) {
                        x = 10 + 130
                        y += 60
                    } else {
                        x += 130
                    }
                    String color = "DarkGray"
                    if (colors.containsKey(patch)) {
                        color = colors[patch]
                    }
                    markupBuilder.rect(x: x, y: y, rx: 2, ry: 2, width: 100, height: 30, stroke: "black", "stroke-width": 1, fill: color)
                    markupBuilder.text(x: x + 5, y: y + 22, "font-size": 21, patch)
                }
                y += 60
            }
        }
        new File(reportName).withWriter {
            it << writer.toString()
        }
    }
}
