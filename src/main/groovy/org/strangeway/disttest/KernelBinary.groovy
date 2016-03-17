package org.strangeway.disttest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

class KernelBinary {
    KernelSource kernelSource
    KernelConfig kernelConfig

    KernelBinary(String version, String configName){
        kernelSource = new KernelSource(version)
        kernelConfig = new KernelConfig(version, configName)
        assert kernelSource
        assert kernelConfig
    }

    File getArtifact() {
        File artifact = new File("artifacts/"+getHash()+".bzImage")
        if(artifact.exists()){
            return artifact;
        }

        Path src = Paths.get(kernelConfig.getPath())
        Path dst = Paths.get(kernelSource.getPath()+"/.config")
        Files.copy(src, dst, REPLACE_EXISTING)

        //ToDo: implement status reporting
        Process process = new ProcessBuilder("make", "-j9").directory(new File(kernelSource.getPath())).start();
        def sout = new StringBuilder()
        def serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        assert 0 == process.exitValue()
        Files.copy(Paths.get(kernelSource.path+"/arch/x86_64/boot/bzImage"), Paths.get(artifact.path), REPLACE_EXISTING)
        return artifact
    }

    String getHash(){
        return Utils.calcHash(kernelSource.getHash()+kernelConfig.getHash())
    }
}
