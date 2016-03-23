package org.strangeway.disttest

class KernelRepo {
    private static final gitUrl = "git@server.home:/home/git/linux-stable.git"

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
        StringBuilder serr = new StringBuilder()
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
        assert 0 == process.exitValue()
        return sout.tokenize("\n").reverse()
    }
}
