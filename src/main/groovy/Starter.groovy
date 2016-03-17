import org.strangeway.disttest.Distro
import org.strangeway.disttest.Initramfs
import org.strangeway.disttest.KernelBinary

class Starter {
    public static void main(String[] args) {
        Initramfs initramfs = new Initramfs("hello.sh")
        for(version in ["linux-2.6.32.71", "linux-3.18.28", "linux-4.4.4", "linux-4.5"]){
            KernelBinary kernelBinary = new KernelBinary(version, "acpi")
            Distro distro = new Distro(kernelBinary, initramfs)
            print(distro.run())
        }
    }
}

