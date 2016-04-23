import groovy.transform.CompileStatic
import org.strangeway.disttest.Distro
import org.strangeway.disttest.Initramfs
import org.strangeway.disttest.KernelBinary
import org.strangeway.disttest.KernelRepo
import org.strangeway.disttest.KernelSourcePool
import org.strangeway.disttest.Utils

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
class Starter {
    public static void threadedDemo() {
        KernelSourcePool kernelSourcePool = new KernelSourcePool()
        Initramfs initramfs = new Initramfs("hello.sh")
        initramfs.getArtifact() //Hack to prevent races in other threads
        Map<String, Distro> distros = new HashMap<>()
        List<Thread> threads = new ArrayList<>()
        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>()
        for (version in ["linux-2.6.32.71", "linux-3.18.28"]) {
            KernelBinary kernelBinary = new KernelBinary(version, version.replace(/linux-/, "v"), "acpi", kernelSourcePool)
            Distro distro = new Distro(kernelBinary, initramfs)
            distros[version] = distro
            String ver = version
            threads.add(Thread.start {
                results[ver] = distro.run()
                distro.close()
            })
        }
        while (true) {
            boolean needBreak = true
            threads.each { Thread t ->
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
                    println String.format("%-20s %-100s", k, "In progress")
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

    public static void bisectDemo() {
        KernelSourcePool kernelSourcePool = new KernelSourcePool()
        Initramfs initramfs = new Initramfs("hello.sh")
        initramfs.getArtifact()

        String[] commits = KernelRepo.getVersionBetweenTags("v3.18", "v3.18.28")
        int broken = 0
        int working = commits.length - 1
        while (broken + 1 != working) {
            int testIndex = ((broken + working) / 2) as int
            KernelBinary kernelBinary = new KernelBinary("linux-3.18", commits[testIndex], "acpi", kernelSourcePool)
            println "Testing commit #$testIndex($broken..$working)" + commits[testIndex]
            Distro distro = new Distro(kernelBinary, initramfs)
            String res = distro.run()
            distro.close()
            println "Res: $res\n"
            if (res == "<KILLED>") {
                broken = testIndex
            } else {
                working = testIndex
            }
        }
    }

    public
    static boolean semtexOneVersionVulnerable(String version, String commit, Initramfs initramfs, KernelSourcePool kernelSourcePool) {
        KernelBinary kernelBinary = new KernelBinary(version.replace(/v/, "linux-"), commit, "acpi_perf_sec", kernelSourcePool)

        Distro distro = new Distro(kernelBinary, initramfs)
        String res = distro.run()
        distro.close()
        println "Res: $res\n"
        return res == "uid=0(root) gid=0(root) groups=1(user)"
    }

    public static void bisectSemtexDemo() {
        Map<String, String> results = new HashMap<>()
        KernelSourcePool kernelSourcePool = new KernelSourcePool()
        Initramfs initramfs = new Initramfs("cve-2013-2094.sh")
        initramfs.getArtifact()

        List<List<String>> versions = KernelRepo.getPatchLevels().collect({ String it ->
            [it, KernelRepo.getSubLevels(it)].flatten() as List<String>
        })
        def toTest = ["v2.6.37": 0, "v2.6.38": 0, "v2.6.39": 0, "v3.0": 0, "v3.1": 0, "v3.2": 0, "v3.3": 0, "v3.4": 0, "v3.5": 0, "v3.6": 0, "v3.7": 0, "v3.8": 0]
        //def toTest = ["v3.10": 0]

        //versions.each {
        String[] it = KernelRepo.getPatchLevels()
        println(it)
        String from_version = it[5]
        String to_version = it[-1]
        if (toTest.containsKey(it[0]) || true) {
            String[] commits = KernelRepo.getVersionBetweenTags(from_version, to_version)
            println "There is " + commits.length + " commits between $from_version and $to_version"
            int broken = 5
            int working = commits.length - 1
            println "Testing $from_version"
            if (semtexOneVersionVulnerable(from_version, from_version, initramfs, kernelSourcePool)) {
                results[from_version] = "FireBrick"
            } else {
                results[from_version] = "LimeGreen"
            }
            println "Testing $to_version"
            if (semtexOneVersionVulnerable(from_version, to_version, initramfs, kernelSourcePool)) {
                results[to_version] = "FireBrick"
            } else {
                results[to_version] = "LimeGreen"
            }
            if (results[from_version] == "FireBrick" && results[to_version] == "LimeGreen") {
                while (broken + 1 != working) {
                    int testIndex = ((broken + working) / 2) as int
                    println "Testing commit #$testIndex($broken..$working)" + commits[testIndex]
                    String[] prevNext = KernelRepo.getPrevNextTag(commits[testIndex])
                    if (semtexOneVersionVulnerable(from_version, commits[testIndex], initramfs, kernelSourcePool)) {
                        results[prevNext[0]] = "FireBrick"
                        broken = testIndex
                    } else {
                        results[prevNext[1]] = "LimeGreen"
                        working = testIndex
                    }
                }
            }
        }
        Utils.renderReport(versions, results, "cve-2013-2094.svg")
    }

    public static void buildDemo() {
        def versions = KernelRepo.getPatchLevels().collect({ String it ->
            [it, KernelRepo.getSubLevels(it)].flatten()
        })
        KernelSourcePool kernelSourcePool = new KernelSourcePool()
        versions.each {
            String version = it[0]
            println "Building $version"
            try {
                KernelBinary kernelBinary = new KernelBinary(version.replace(/v/, "linux-"), version, "acpi", kernelSourcePool)
                println kernelBinary.getArtifact()
                kernelBinary.close()
            }
            catch (e) {
                println e
            }
        }
    }

    public static void main(String[] args) {
        //bisectSemtexDemo()
        KernelSourcePool kernelSourcePool = new KernelSourcePool()
        def version = "v2.6.32"
        KernelBinary kernelBinary = new KernelBinary(version.replace(/v/, "linux-"), version, "acpi", kernelSourcePool)
        println kernelBinary.getArtifact()
    }
}

