package org.strangeway.disttest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

class KernelBinary implements Task {
    KernelSource kernelSource
    KernelConfig kernelConfig
    private volatile long percentage = 0
    private boolean artifactExists = false

    KernelBinary(String version, String configName) {
        kernelSource = new KernelSource(version)
        kernelConfig = new KernelConfig(version, configName)
        assert kernelSource
        assert kernelConfig
        artifactExists = new File("artifacts/" + getHash() + ".bzImage").exists()
    }

    File getArtifact() {
        percentage = 0
        File artifact = new File("artifacts/" + getHash() + ".bzImage")
        if (artifact.exists()) {
            percentage = 100
            return artifact;
        }

        Path src = Paths.get(kernelConfig.getPath())
        Path dst = Paths.get(kernelSource.getPath() + "/.config")
        Files.copy(src, dst, REPLACE_EXISTING)


        if (new File(kernelSource.getPath() + "/kernel/timeconst.pl").exists()) {
            String hash = Utils.calcHash(new File(kernelSource.getPath() + "/kernel/timeconst.pl").getBytes())
            if ("09899b0245ce50e1c82bc999db5a9e702318678a" == hash) {
                Process process = new ProcessBuilder("patch", "-p1", "--batch").directory(new File(kernelSource.getPath())).start();
                OutputStream stdin = process.getOutputStream();
                stdin << new File("kernelPatches/timeconst.pl.patch").getBytes()
                stdin.close()
                process.waitFor()
            }
        }

        Process process
        if ((new File(kernelSource.getPath() + "/include/linux/compiler-gcc4.h").exists()) &&
                (!new File(kernelSource.getPath() + "/include/linux/compiler-gcc5.h").exists())) {
            String gcc = "gcc-4.9"
            try {
                gcc.execute()
            } catch (all) {
                gcc = "gcc-4.8"
            }
            process = new ProcessBuilder("make", "-j9", "CC=$gcc").directory(new File(kernelSource.getPath())).start();
        } else {
            process = new ProcessBuilder("make", "-j9").directory(new File(kernelSource.getPath())).start();
        }

        def sout = new StringBuilder()
        def serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        assert 0 == process.exitValue()
        Files.copy(Paths.get(kernelSource.path + "/arch/x86_64/boot/bzImage"), Paths.get(artifact.path), REPLACE_EXISTING)
        percentage = 100
        return artifact
    }

    String getHash() {
        return Utils.calcHash(kernelSource.getHash() + kernelConfig.getHash())
    }

    @Override
    String getDescription() {
        return "Build Kernel"
    }

    @Override
    Task[] getSubTasks() {
        return artifactExists ? [] : [kernelSource]
    }

    @Override
    long getPercentage() {
        return percentage
    }
}
