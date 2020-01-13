package org.yenbo.jetty.cxf;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.yenbo.jetty.api.DemoService;
import org.yenbo.jetty.api.SecretService;
import org.yenbo.jetty.exception.DemoExceptionMapper;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@Configuration
@ComponentScan("org.yenbo.jetty")
public class CxfConfiguration {

	private static final Logger log = LoggerFactory.getLogger(CxfConfiguration.class);
	
	@Autowired
	private JacksonJsonProvider jsonProvider;
	@Autowired
	private DemoExceptionMapper exceptionMapper;
	
	public static Server createServerFactory(Application application,
			List<Object> providers, List<Object> serviceBeans) {
		
		JAXRSServerFactoryBean factory = RuntimeDelegate.getInstance().createEndpoint(
				application, JAXRSServerFactoryBean.class);
        factory.setProviders(providers);
        factory.setServiceBeans(serviceBeans);
        
        Server server = factory.create();
        log.debug("Create server: {}", server.getEndpoint().getService().getName());
        return server;
	}
		
	@Bean(destroyMethod = "shutdown")
    public SpringBus cxf() {
        return new SpringBus();
    }
	
	@Bean
    public JacksonJsonProvider jsonProvider() {
    	return new JacksonJsonProvider();
    }
	
	@Bean
    public Server demoServer() {
		
        return createServerFactory(new DemoApplication(),
        		Arrays.<Object>asList(
        				jsonProvider,
        				exceptionMapper),
        		Arrays.<Object>asList(new DemoService()));
    }
	
	@Bean
    public Server secretServer() {
		
        return createServerFactory(new SecuredApplication(),
        		Arrays.<Object>asList(jsonProvider),
        		Arrays.<Object>asList(new SecretService()));
    }
	
	@Bean
	public DemoExceptionMapper exceptionMapper() {		
		return new DemoExceptionMapper();
	}
}
