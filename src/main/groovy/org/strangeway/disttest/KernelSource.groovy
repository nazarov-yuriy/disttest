package org.strangeway.disttest

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Matcher

@CompileStatic
class KernelSource {
    private static final String basePath = "kernelSources"
    String version
    String commit
    int slot
    String tmpMountPoint
    String aufsMountPoint
    KernelRepo kernelRepo

    KernelSource(String _version, String _commit, int _slot) {
        version = _version
        commit = _commit
        slot = _slot
        kernelRepo = new KernelRepo()
    }

    String getSeedPath(String version) {
        File srcArchive = new File("downloads/" + version + ".tar.xz")
        if (!srcArchive.exists()) {
            for(i in 1..150){
                if (new File("downloads/${version}.${i}.tar.xz").exists()) {
                    version = "$version.$i"
                    srcArchive = new File("downloads/" + version + ".tar.xz")
                    break
                }
            }
        }
        if (!srcArchive.exists()) {
            String url = null
            Matcher v4 = version =~ /linux-4\..*/
            if (v4) {
                url = "https://cdn.kernel.org/pub/linux/kernel/v4.x/" + version + ".tar.xz"
            }
            Matcher v3 = version =~ /linux-3\..*/
            if (v3) {
                url = "https://cdn.kernel.org/pub/linux/kernel/v3.x/" + version + ".tar.xz"
            }
            Matcher v26 = version =~ /linux-2\..*/
            if (v26) {
                url = "https://cdn.kernel.org/pub/linux/kernel/v2.6/longterm/v2.6.32/" + version + ".tar.xz"
            }
            assert url

            long totalLen = new URL(url).openConnection().getContentLength();
            long downloadedLen = 0
            BufferedOutputStream fileStream = srcArchive.newOutputStream()
            InputStream urlStream = new URL(url).openStream();
            byte[] buf = new byte[65536]
            int len
            while ((len = urlStream.read(buf)) != -1) {
                fileStream.write(buf, 0, len)
                downloadedLen += len
            }
            fileStream.close()
            urlStream.close()
        }

        File srcDir = new File("$basePath/$version")
        if (!srcDir.isDirectory()) {
            Process process = new ProcessBuilder("tar", "-C", basePath, "-xf", srcArchive.path).start();
            process.waitFor()
            assert 0 == process.exitValue(), process.text+process.err
        }
        return srcDir.getPath()
    }

    String getTmpPath() {
        File tmpDir = new File("mounts/tmpMP$slot")
        if (!tmpDir.isDirectory()) {
            assert tmpDir.mkdir() || (new File("mounts").mkdir() && tmpDir.mkdir())
        }
        Process process = new ProcessBuilder("mount", tmpDir.getPath()).start(); //Should be present in /etc/fstab
        process.waitFor()
        assert 0 == process.exitValue()
        return tmpMountPoint = tmpDir.getPath()
    }

    String getPath() {
        if (aufsMountPoint != null) {
            return aufsMountPoint
        }
        String seed = getSeedPath(version)
        String repo = kernelRepo.getRepoPath()
        String tmp = getTmpPath()

        Files.deleteIfExists(Paths.get("mounts/repo$slot"))
        Files.createSymbolicLink(Paths.get("mounts/repo$slot"), Paths.get(new File(repo).getCanonicalPath()))

        Files.deleteIfExists(Paths.get("mounts/seed$slot"))
        Files.createSymbolicLink(Paths.get("mounts/seed$slot"), Paths.get(new File(seed).getCanonicalPath()))

        Files.deleteIfExists(Paths.get("mounts/tmp$slot"))
        Files.createSymbolicLink(Paths.get("mounts/tmp$slot"), Paths.get(new File(tmp).getCanonicalPath()))

        File aufsDir = new File("mounts/aufs$slot")
        if (!aufsDir.isDirectory()) {
            assert aufsDir.mkdir()
        }
        Process process = new ProcessBuilder("mount", "-i", aufsDir.getPath()).start(); //Should be present in /etc/fstab
        process.waitFor()
        assert 0 == process.exitValue()
        aufsMountPoint = aufsDir.getPath()

        Process gitResetMixed = new ProcessBuilder("git", "reset", "--mixed", commit).directory(aufsDir).start();
        gitResetMixed.consumeProcessOutput()
        gitResetMixed.waitFor()
        assert 0 == gitResetMixed.exitValue()

        Process gitResetHard = new ProcessBuilder("git", "reset", "--hard", commit).directory(aufsDir).start();
        gitResetHard.consumeProcessOutput()
        gitResetHard.waitFor()
        assert 0 == gitResetHard.exitValue()

        Process gitClean = new ProcessBuilder("git", "clean", "-d", "-x", "-f").directory(aufsDir).start();
        gitClean.consumeProcessOutput()
        gitClean.waitFor()
        assert 0 == gitClean.exitValue()

        return aufsMountPoint
    }

    void close() {
        if (aufsMountPoint != null) {
            Process aufsProcess = new ProcessBuilder("umount", "-i", aufsMountPoint).start();
            aufsProcess.waitFor()
            assert 0 == aufsProcess.exitValue()
        }
        if (tmpMountPoint) {
            Process tmpProcess = new ProcessBuilder("umount", tmpMountPoint).start();
            tmpProcess.waitFor()
            assert 0 == tmpProcess.exitValue()
        }
    }

    String getHash() {
        return Utils.calcHash(version+commit)
    }
}
