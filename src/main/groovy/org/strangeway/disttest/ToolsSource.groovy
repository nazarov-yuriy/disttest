package org.strangeway.disttest

class ToolsSource implements Task {
    private static final basePath = "toolsSources"
    String version
    long percentage = 0

    ToolsSource(String _version){
        version = _version
    }

    String getPath(){
        percentage = 1
        File srcArchive = new File("downloads/${version}.tar.bz2")
        if(!srcArchive.exists()){
            def fileStream = srcArchive.newOutputStream()
            fileStream << new URL("https://busybox.net/downloads/${version}.tar.bz2").openStream()
            fileStream.close()
        }
        File srcDir = new File("$basePath/$version")
        if(!srcDir.isDirectory()){
            Process process = new ProcessBuilder("tar", "-C", basePath, "-xf", srcArchive.path).start();
            process.waitFor()
            assert 0 == process.exitValue()
        }
        percentage = 100
        return srcDir.getPath()
    }

    String getHash(){
        return Utils.calcHash(version)
    }

    @Override
    String getDescription() {
        return "Download Tools"
    }

    @Override
    Task[] getSubTasks() {
        return new Task[0]
    }

    @Override
    long getPercentage() {
        return percentage
    }
}
