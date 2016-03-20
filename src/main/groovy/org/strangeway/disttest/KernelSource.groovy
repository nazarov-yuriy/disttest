package org.strangeway.disttest

import java.util.regex.Matcher

class KernelSource implements Task {
    private percentage = 0
    private static final basePath = "kernelSources"
    String version

    KernelSource(String _version){
        version = _version
    }

    String getPath() {
        File srcArchive = new File("downloads/"+version+".tar.xz")
        if(!srcArchive.exists()){
            String url = null
            Matcher v4 = version =~ /linux-4\..*/
            if(v4){
                url = "https://cdn.kernel.org/pub/linux/kernel/v4.x/"+version+".tar.xz"
            }
            Matcher v3 = version =~ /linux-3\..*/
            if(v3){
                url = "https://cdn.kernel.org/pub/linux/kernel/v3.x/"+version+".tar.xz"
            }
            Matcher v26 = version =~ /linux-2\..*/
            if(v26){
                url = "https://cdn.kernel.org/pub/linux/kernel/v2.6/longterm/v2.6.32/"+version+".tar.xz"
            }
            assert url

            long totalLen = new URL(url).openConnection().getContentLength();
            long downloadedLen = 0
            BufferedOutputStream fileStream = srcArchive.newOutputStream()
            InputStream urlStream = new URL(url).openStream();
            byte[] buf = new byte[65536]
            int len = 0
            while( (len = urlStream.read(buf)) != -1 ){
                fileStream.write(buf, 0, len)
                downloadedLen += len
                percentage = 100L * downloadedLen / totalLen
            }
            fileStream.close()
            urlStream.close()
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

    String getHash() {
        return Utils.calcHash(version)
    }

    @Override
    String getDescription() {
        return "Download Kernel"
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
