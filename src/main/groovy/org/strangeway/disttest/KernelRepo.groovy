package org.strangeway.disttest

import groovy.transform.CompileStatic

import java.util.regex.Matcher

@CompileStatic
class KernelRepo {
    private static final String gitUrl = "git@server.home:/home/git/linux-stable.git"
    private static Comparator<String> byLastNumber = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            Matcher ma = o1 =~ /\d+$/
            Matcher mb = o2 =~ /\d+$/
            ma[0] as int <=> mb[0] as int
        }
    }

    static String getRepoPath() {
        File repoDir = new File("kernelRepo/linux-stable")
        if (repoDir.isDirectory()) {
            return repoDir.getPath()
        }
        Process process = new ProcessBuilder("git", "clone", "--no-checkout", gitUrl).directory(new File("kernelRepo")).start();
        process.consumeProcessOutput()
        process.waitFor()
        assert 0 == process.exitValue()
        return repoDir.getPath()
    }

    static String[] getVersionBetweenTags(String from, String to){
        Process process = new ProcessBuilder("git", "log", "--pretty=format:%H", "$from^..$to").directory(new File(getRepoPath())).start();
        StringBuilder sout = new StringBuilder()
        process.consumeProcessOutputStream(sout).join()
        process.waitFor()
        process.consumeProcessOutputStream(sout).join() //Todo: add proper fix
        assert 0 == process.exitValue()
        return sout.tokenize("\n").reverse() as String[]
    }



    static List<String> getSubLevels(String patchLevel){
        Process process = new ProcessBuilder("git", "tag").directory(new File(getRepoPath())).start();
        StringBuilder sout = new StringBuilder()
        StringBuilder serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        assert 0 == process.exitValue()
        List<String> subLevels = sout.tokenize("\n").findAll({it =~ /^$patchLevel.\d+$/})
        subLevels.sort(byLastNumber)
        subLevels
    }

    static List<String> getPatchLevels(){
        Process process = new ProcessBuilder("git", "tag").directory(new File(getRepoPath())).start();
        StringBuilder sout = new StringBuilder()
        StringBuilder serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        assert 0 == process.exitValue()
        List<String> v26 = sout.tokenize("\n").findAll({it =~ /^v2.6.3[2-9]+$/})
        v26.sort(byLastNumber)
        List<String> v3 = sout.tokenize("\n").findAll({it =~ /^v3.\d+$/})
        v3.sort(byLastNumber)
        List<String> v4 = sout.tokenize("\n").findAll({it =~ /^v4.\d+$/})
        v4.sort(byLastNumber)
        return [v26, v3, v4].flatten() as List<String>
    }

    static String[] getPrevNextTag(String commit){
        Process processNext = new ProcessBuilder("git", "describe", "--abbrev=0", commit).directory(new File(getRepoPath())).start();
        processNext.waitFor()
        String next = processNext.getText().replace("\n", "").replace("\r", "")
        assert 0 == processNext.exitValue()
        assert next != ""
        Process processPrev = new ProcessBuilder("git", "describe", "--abbrev=0", "$next^").directory(new File(getRepoPath())).start();
        processPrev.waitFor()
        String prev = processPrev.getText().replace("\n", "").replace("\r", "")
        assert 0 == processPrev.exitValue()
        assert prev != ""
        return [prev, next] as String[]
    }
}
