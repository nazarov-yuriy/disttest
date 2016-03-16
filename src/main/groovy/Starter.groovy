import org.strangeway.disttest.Distro
import org.strangeway.disttest.Initramfs
import org.strangeway.disttest.KernelBinary

class Starter {
    public static void main(String[] args) {
        KernelBinary kernelBinary = new KernelBinary("linux-4.4.4", "acpi")
        println kernelBinary.getArtifact()

        Initramfs initramfs = new Initramfs("hello.sh")
        println initramfs.getArtifact()

        Distro distro = new Distro(kernelBinary, initramfs)
        print distro.run()
    }
}

