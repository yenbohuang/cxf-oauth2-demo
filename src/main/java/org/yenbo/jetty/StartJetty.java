package org.yenbo.jetty;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartJetty {

	private static final Logger log = LoggerFactory.getLogger(StartJetty.class);
	
	public static void main(String[] args) {
		
		Server server = new Server(8080);
		server.setStopAtShutdown(true);
        
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setResourceBase(
        		StartJetty.class.getProtectionDomain().getCodeSource().getLocation().toString());
        log.debug("Resource base: {}", webapp.getResourceBase());
        webapp.setConfigurations(new Configuration[] {
        		new WebInfConfiguration(),
        		new AnnotationConfiguration()
        		});
        webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
            ".*/[^/]*\\.jar$|.*/target/classes/.*");
        server.insertHandler(webapp);
        
        try {
        	server.start();
        	server.dumpStdErr();
        	server.join();
        } catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
