package org.jbpm.spring.boot;

import java.io.FileInputStream;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.process.workitem.email.EmailWorkItemHandler;
import org.jbpm.services.api.DeploymentService;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ComponentScan
@EnableAutoConfiguration(exclude=HibernateJpaAutoConfiguration.class)
@ImportResource(value= {"classpath:jee-tx-context.xml",
		"classpath:jpa-context.xml", "classpath:jbpm-context.xml", "classpath:security-context.xml",})
public class Application {

	public static void main(String[] args) {
		args = new String[3];
		args[0] = "org.mastertheboss.kieserver";
		args[1] = "hello-kie-server";
		args[2] = "1.0.0-SNAPSHOT";
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
		if (args.length > 1) {

			try {
				System.out.println("Params available trying to deploy " + args);
				DeploymentService deploymentService = (DeploymentService) ctx.getBean("deploymentService");

				KModuleDeploymentUnit unit = new KModuleDeploymentUnit(args[0], args[1], args[2]);
				deploymentService.deploy(unit);
				KieSession ksession = readKnowledgeBase();
				EmailWorkItemHandler emailHandler = new EmailWorkItemHandler();
				emailHandler.setConnection("smtp.gmail.com", "587", "nayak.swadhin@gmail.com", "mypassword");
				emailHandler.getConnection().setStartTls(true);
				ksession.getWorkItemManager().registerWorkItemHandler("Email", emailHandler);
				ksession.startProcess("com.sample.bpmn.hello");
				System.out.println("Process started ...");
			} catch (Throwable e) {
				e.printStackTrace();
				System.out.println("Error when deploying = " + e.getMessage());
			}
		}
	}

	
	private static KieSession readKnowledgeBase() throws Exception {
		KieServices ks = KieServices.Factory.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		FileInputStream fis = new FileInputStream( "src/main/resources/sample.bpmn" );
		kfs.write( "src/main/resources/sample.bpmn",
		            ks.getResources().newInputStreamResource( fis ) );
		KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();
		Results results = kieBuilder.getResults();
		if( results.hasMessages( Message.Level.ERROR ) ){
		     System.out.println( results.getMessages() );
		     throw new IllegalStateException( "### errors ###" );
		}
		KieContainer kieContainer =
		     ks.newKieContainer( ks.getRepository().getDefaultReleaseId() );

		KieBaseConfiguration config = ks.newKieBaseConfiguration();
		config.setOption( EventProcessingOption.STREAM );
		KieBase kieBase = kieContainer.newKieBase( config );
		KieSession kieSession = kieBase.newKieSession();
		return kieSession;
	}
}
