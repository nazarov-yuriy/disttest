package org.strangeway.disttest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

class KernelBinary {
    KernelSource kernelSource
    KernelConfig kernelConfig

    KernelBinary(String version, String configName){
        kernelSource = KernelSource.findByVersion(version)
        kernelConfig = KernelConfig.findByname(configName)
        assert kernelSource
        assert kernelConfig
    }

    String getBinary() {
        Path src = Paths.get(kernelConfig.path)
        Path dst = Paths.get(kernelSource.path+"/.config")
        Files.copy(src, dst, REPLACE_EXISTING)

        Process process = new ProcessBuilder("make", "-j9").directory(new File(kernelSource.path)).start();
        process.waitFor()
        assert 0 == process.exitValue()
        return kernelSource.path+"/arch/x86_64/boot/bzImage"
    }
}
