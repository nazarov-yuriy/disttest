package org.strangeway.disttest

import java.util.regex.Matcher

class KernelSource {
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

            def fileStream = srcArchive.newOutputStream()
            fileStream << new URL(url).openStream()
            fileStream.close()
        }

        File srcDir = new File("$basePath/$version")
        if(!srcDir.isDirectory()){
            Process process = new ProcessBuilder("tar", "-C", basePath, "-xf", srcArchive.path).start();
            process.waitFor()
            assert 0 == process.exitValue()
        }
        return srcDir.getPath()
    }

    String getHash() {
        return Utils.calcHash(version)
    }
}
