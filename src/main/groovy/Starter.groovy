import org.strangeway.disttest.Distro
import org.strangeway.disttest.Initramfs
import org.strangeway.disttest.KernelBinary
import org.strangeway.disttest.KernelSourcePool
import org.strangeway.disttest.Utils

import java.util.concurrent.ConcurrentHashMap

class Starter {
    public static void main(String[] args) {
        KernelSourcePool kernelSourcePool = new KernelSourcePool()
        Initramfs initramfs = new Initramfs("hello.sh")
        initramfs.getArtifact() //Hack to prevent races in other threads
        Map<String, Distro> distros = new HashMap<>()
        List<Thread> threads = new ArrayList<>()
        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>()
        for (version in ["linux-2.6.32.71", "linux-3.18.28"]) {
            KernelBinary kernelBinary = new KernelBinary(version, "acpi", kernelSourcePool)
            Distro distro = new Distro(kernelBinary, initramfs)
            distros[version] = distro
            String ver = version
            threads.add(Thread.start {
                results[ver] = distro.run()
            })
        }
        while (true) {
            boolean needBreak = true
            threads.each { t ->
                if (t.alive) {
                    needBreak = false
                }
            }
            Thread.sleep(100)
            distros.each { k, v ->
                if (results.containsKey(k)) {
                    println String.format("%-20s %-100s", k, results[k])
                    //ToDo: use more reliable method to clear terminal
                } else {
                    println String.format("%-20s %-100s", k, Utils.renderProgress(v))
                }
            }
            print String.format("%c[%dA", 0x1B, distros.size());

            if (needBreak) {
                break
            }
        }

        results.each { k, v ->
            println String.format("%-20s %-100s", k, v)
        }
    }
}

