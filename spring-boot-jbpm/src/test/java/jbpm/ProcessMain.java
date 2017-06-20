package jbpm;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;
import org.jbpm.process.workitem.email.EmailWorkItemHandler;


/**
 * This is a sample file to launch a process.
 */
public class ProcessMain {

	public static final void main(String[] args) throws Exception {
		KnowledgeBase kbase = readKnowledgeBase();
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();


		EmailWorkItemHandler emailHandler = new EmailWorkItemHandler();
		emailHandler.setConnection("smtp.gmail.com", "587", "nayak.swadhin@gmail.com", "mypassword");
		ksession.getWorkItemManager().registerWorkItemHandler("Email", (WorkItemHandler) emailHandler);

		// start a new process instance
		ksession.startProcess("com.sample.bpmn.hello");

	}


	private static KnowledgeBase readKnowledgeBase() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("sample.bpmn"), ResourceType.DRF);
		return kbuilder.newKnowledgeBase();
	}

	// load up the knowledge base
}