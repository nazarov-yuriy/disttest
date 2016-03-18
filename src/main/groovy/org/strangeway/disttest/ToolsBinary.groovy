package org.strangeway.disttest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

class ToolsBinary implements Task {
    private long percentage = 0
    ToolsSource toolsSource
    public static final basePath = "toolsConfigs"

    ToolsBinary(String version) {
        toolsSource = new ToolsSource(version)
    }

    String getBinary() {
        percentage = 0
        Path src = Paths.get(basePath+"/default")
        Path dst = Paths.get(toolsSource.getPath()+"/.config")
        Files.copy(src, dst, REPLACE_EXISTING)

        Process process = new ProcessBuilder("make", "-j9").directory(new File(toolsSource.getPath())).start();
        def sout = new StringBuilder()
        def serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        assert 0 == process.exitValue()
        percentage = 100
        return toolsSource.getPath()+"/busybox"
    }

    String getHash(){
        return Utils.calcHash(toolsSource.getHash())
    }

    @Override
    String getDescription() {
        return "Build Tools"
    }

    @Override
    Task[] getSubTasks() {
        return [toolsSource]
    }

    @Override
    long getPercentage() {
        return percentage
    }
}
