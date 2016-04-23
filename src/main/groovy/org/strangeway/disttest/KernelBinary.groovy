package org.strangeway.disttest

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

@CompileStatic
class KernelBinary {
    KernelSource kernelSource
    KernelConfig kernelConfig
    KernelSourcePool kernelSourcePool
    private boolean artifactExists = false

    KernelBinary(String version, String commit, String configName, KernelSourcePool _kernelSourcePool) {
        kernelSourcePool = _kernelSourcePool
        kernelSource = kernelSourcePool.getKernelSource(version, commit)
        kernelConfig = new KernelConfig(version, configName)
        assert kernelSource
        assert kernelConfig
        artifactExists = new File("artifacts/" + getHash() + ".bzImage").exists()
    }

    boolean isArtifactPresent(){
        File artifact = new File("artifacts/" + getHash() + ".bzImage")
        return artifact.exists()
    }

    File getArtifact() {
        File artifact = new File("artifacts/" + getHash() + ".bzImage")
        if (artifact.exists()) {
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
        if(new File(kernelSource.getPath() + "/arch/x86/vdso/Makefile").exists()) {
            if (new File(kernelSource.getPath() + "/arch/x86/vdso/Makefile").text.contains("-m elf_x86_64")) {
                Process process = new ProcessBuilder("patch", "-p1", "--batch").directory(new File(kernelSource.getPath())).start();
                OutputStream stdin = process.getOutputStream();
                stdin << new File("kernelPatches/m_elf_x86_64.patch").getBytes()
                stdin.close()
                process.waitFor()
            }
        }

        Process processOldConfig = new ProcessBuilder("make", "oldconfig").directory(new File(kernelSource.getPath())).start();
        processOldConfig.outputStream.close()
        processOldConfig.consumeProcessOutput(new StringBuilder(), new StringBuilder())
        processOldConfig.waitFor()
        assert 0 == processOldConfig.exitValue()

        Process process
        if ((new File(kernelSource.getPath() + "/include/linux/compiler-gcc4.h").exists()) &&
                (!new File(kernelSource.getPath() + "/include/linux/compiler-gcc5.h").exists())) {
            String gcc = "gcc-4.9"
            try {
                gcc.execute()
            } catch (ignored) {
                gcc = "gcc-4.8"
            }
            process = new ProcessBuilder("make", "-j9", "CC=$gcc").directory(new File(kernelSource.getPath())).start();
        } else {
            process = new ProcessBuilder("make", "-j9").directory(new File(kernelSource.getPath())).start();
        }

        StringBuilder sout = new StringBuilder()
        StringBuilder serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        process.consumeProcessOutput(sout, serr)
        assert 0 == process.exitValue(), sout+serr
        Files.copy(Paths.get(kernelSource.path + "/arch/x86_64/boot/bzImage"), Paths.get(artifact.path), REPLACE_EXISTING)
        return artifact
    }

    void close() {
        kernelSourcePool.putKernelSource(kernelSource)
    }

    String getHash() {
        return Utils.calcHash(kernelSource.getHash() + kernelConfig.getHash())
    }
}
