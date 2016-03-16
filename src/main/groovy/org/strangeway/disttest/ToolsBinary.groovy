package org.strangeway.disttest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

class ToolsBinary {
    ToolsSource toolsSource
    public static final basePath = "toolsConfigs"

    ToolsBinary(String version) {
        toolsSource = ToolsSource.findByVersion(version)
        assert toolsSource
    }

    String getBinary() {
        Path src = Paths.get(basePath+"/default")
        Path dst = Paths.get(toolsSource.path+"/.config")
        Files.copy(src, dst, REPLACE_EXISTING)

        Process process = new ProcessBuilder("make", "-j9").directory(new File(toolsSource.path)).start();
        process.waitFor()
        assert 0 == process.exitValue()
        return toolsSource.path+"/busybox"
    }

    String getHash(){
        return Utils.calcHash(toolsSource.getHash())
    }
}
