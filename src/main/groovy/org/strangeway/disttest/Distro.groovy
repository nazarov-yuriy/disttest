package org.strangeway.disttest

class Distro implements Task{
    volatile long startedAt = 0
    volatile boolean running = false

    KernelBinary kernelBinary
    Initramfs initramfs

    Distro(KernelBinary _kernelBinary, Initramfs _initramfs){
        kernelBinary = _kernelBinary
        initramfs = _initramfs
    }

    String run() {
        Process process = new ProcessBuilder("qemu-system-x86_64",
                "-machine", "accel=kvm",
                "-m", "64",
                "-kernel", kernelBinary.getArtifact().path,
                "-initrd", initramfs.getArtifact().path,
                "-chardev", "stdio,id=charserial0",
                "-device", "isa-serial,chardev=charserial0,id=serial0",
                "-nographic", "-nodefconfig", "-nodefaults"
        ).start()
        running = true
        startedAt = System.currentTimeMillis()
        process.waitForOrKill(5000) //ToDo: make value at least configurable
        assert 0 == process.exitValue()
        running = false
        try {
            return process.getText().replace("\n", "").replace("\r", "");
        } catch (all) {
            return "<KILLED>"
        }
    }

    @Override
    String getDescription() {
        return "Running"
    }

    @Override
    Task[] getSubTasks() {
        return [kernelBinary, initramfs]
    }

    @Override
    long getPercentage() {
        if(running){
            return 100.0*(System.currentTimeMillis() - startedAt)/5000.0
        }else{
            if(startedAt) {
                return 100
            }else{
                return 0
            }
        }
    }
}
