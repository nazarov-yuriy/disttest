package org.strangeway.disttest

import java.util.regex.Matcher

class KernelConfig {
    static private final String basePath = "kernelConfigs"
    String version
    String name

    KernelConfig(String _version, String _name) {
        version = _version
        name = _name
    }

    public static String[] parseVersion(String ver) {
        Matcher m3 = ver =~ /linux-([34]|2\.6)\.(\d+)\.(\d+)$/
        if (m3.find()) {
            return [m3.group(1), m3.group(2), m3.group(3)]
        }
        Matcher m2 = ver =~ /linux-([34]|2\.6)\.(\d+)$/
        if (m2.find()) {
            return [m2.group(1), m2.group(2), 0]
        }
    }

    public static String renderVersion(String major, int minor, int patch) {
        if (patch) {
            return "linux-$major.$minor.$patch"
        } else {
            return "linux-$major.$minor"
        }
    }

    String getPath() {
        def (major, minor, patch) = parseVersion(version)
        for (int testMinor = Integer.parseInt(minor); testMinor >= 0; testMinor--) {
            for (int testPatch = Integer.parseInt(patch); testPatch >= 0; testPatch--) {//May be we should start with bigger number for prev minors
                String testVersion = renderVersion(major, testMinor, testPatch)
                String path = "$basePath/$testVersion/$name"
                if (new File(path).exists()) {
                    return path
                }
            }
        }
        assert 0
        return ""
    }

    String getHash() {
        return Utils.calcHash(new File(getPath()).bytes)
    }
}
