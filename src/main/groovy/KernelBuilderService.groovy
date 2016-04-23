import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.strangeway.disttest.KernelBinary
import org.strangeway.disttest.KernelSourcePool

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.regex.Matcher

@CompileStatic
class KernelBuilderService {
    static class Servlet extends HttpServlet {
        Map<Tuple2<String,String>, Thread> versions = [:] //ToDo: fix references leak

        void createTask(String config, String version, HttpServletResponse response) {
            if(versions.containsKey(new Tuple2<>(config, version))){
                assert !versions[new Tuple2<>(config, version)].alive, "Already created"
            }
            versions[new Tuple2<>(config, version)] = Thread.start {
                KernelSourcePool kernelSourcePool = new KernelSourcePool()
                KernelBinary kernelBinary = new KernelBinary(version.replace(/v/, "linux-"), version, config, kernelSourcePool)
                kernelBinary.getArtifact()
                kernelBinary.close()
            }
            response.contentType = 'application/json'
            response.getOutputStream() << JsonOutput.toJson([
                    result: "ok"
            ])
        }

        static void getTaskResult(String config, String version, HttpServletResponse response) {
            response.contentType = 'application/octet-stream'
            KernelSourcePool kernelSourcePool = new KernelSourcePool()
            KernelBinary kernelBinary = new KernelBinary(version.replace(/v/, "linux-"), version, config, kernelSourcePool)
            if (kernelBinary.isArtifactPresent()) {
                response.setHeader("Content-disposition", "attachment; filename=" + kernelBinary.getArtifact().name);
                response.getOutputStream() << kernelBinary.getArtifact().bytes
            } else {
                response.setStatus(404)
            }
        }

        void getTaskStatus(String config, String version, HttpServletResponse response) {
            response.contentType = 'application/json'
            KernelSourcePool kernelSourcePool = new KernelSourcePool()
            KernelBinary kernelBinary = new KernelBinary(version.replace(/v/, "linux-"), version, config, kernelSourcePool)
            if (kernelBinary.isArtifactPresent()) {
                response.getOutputStream() << JsonOutput.toJson([result: "done"])
            } else if (versions.containsKey(new Tuple2<>(config, version))) {
                response.getOutputStream() << JsonOutput.toJson([result: "in progress"])
            } else {
                response.setStatus(404)
            }
        }

        void service(HttpServletRequest request, HttpServletResponse response) {
            Matcher ma = request.requestURI =~ /\/v1\/kernel\/(.+)\/(.+)\/(.+)/
            assert ma
            String config = ma.group(1)
            String version = ma.group(2)
            String action = ma.group(3)
            if ("GET" == request.method && "status" == action) {
                getTaskStatus(config, version, response)
            } else if ("GET" == request.method && "binary" == action) {
                getTaskResult(config, version, response)
            } else if ("POST" == request.method && "create" == action) {
                createTask(config, version, response)
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server(8080)
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS)
        handler.addServlet(Servlet, "/*")
        server.handler = handler
        server.start()
    }
}