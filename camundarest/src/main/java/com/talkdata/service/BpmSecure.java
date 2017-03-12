package com.talkdata.service;

import java.util.logging.Logger;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.task.Task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;



/**
 * Set of secure ReST examples for interacting with the Camunda engine 
 * 
 * @author GaryS
 *
 */
@RequestScoped
@Path("bpmsecure")
@DeclareRoles({"myusergroup" }) 
public class BpmSecure {

	/**
	 * Logging 
	 */
	private final Logger LOGGER = Logger.getLogger(BpmSecure.class.getName());
	
	
	/**
	 * Access to the Camunda Process API
	 */
	@Inject
	private RuntimeService runtimeService;
	
	@Inject
	private TaskService taskService;
	
	
	
	/**
	 * 
	 * 
	 * 
	 * @param hello
	 * @return
	 */
	@GET
	@Path("echoget/{hello}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public String echoGet(@PathParam("hello") String hello) {

		LOGGER.info("*** echoget - hello: " + hello);

		// assemble a basic JSON reply and return
		String echoReply = "{\"echoback\": \"" + hello + "\"}";

		return echoReply;
	}
	
	
	/**
	 * 
	 * @param hello
	 * @return
	 */
	@GET
	@Path("secureechoget/{hello}")
	@RolesAllowed("myusergroup") 
	@Produces(MediaType.APPLICATION_JSON)
	public String secureEchoGet(@PathParam("hello") String hello) {

		LOGGER.info("*** echoget - hello: " + hello);
		
		
		//LoginContext loginContext = new LoginContext
		
		

		// assemble a basic JSON reply and return
		String echoReply = "{\"echoback\": \"" + hello + "\"}";

		return echoReply;
	}
	
	
	
	/**
	 * 
	 * @param processID
	 * @param hello
	 * @param security
	 * @return
	 */
	@GET
	@Path("reviewcontext/{processID}/{hello}")
	@RolesAllowed("myusergroup") 
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode reviewContext(@PathParam("processID") String processID,
								@PathParam("hello") String hello,
								@Context SecurityContext security) {

		LOGGER.info("*** reviewContext - variable hello: " + hello);
		
		String loginName = security.getUserPrincipal().getName();
		LOGGER.info("*** reviewContext - getName: " + loginName);
					
		
		ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
				.setVariable("hello", hello)
				.executeWithVariablesInReturn();
		
		// Though I want to simply assign the entire process to a single owner..
		//	It looks like only tasks are assigned an owner (I reviewed various Camunda tests)
		// So, I'll get my waiting task and assign ownership via "claim" - noting that I assume the task is ready. 

 		runtimeService
 			.getActiveActivityIds(pVariablesInReturn.getProcessInstanceId())
 			.forEach((activityId) -> {
 				LOGGER.info("*** Activity ID: "+ activityId);
 			} );
 						
 		// query task
 		Task task = taskService
	 			.createTaskQuery()
	 			.processInstanceId(pVariablesInReturn.getProcessInstanceId())
	 			.active()
	 			.singleResult();
 		
 		LOGGER.info("*** task getName: "+ task.getName());
 		LOGGER.info("*** task getId: "+ task.getId());
 		// before claiming
 		LOGGER.info("*** task getAssignee before claim: "+ task.getAssignee());
 		
 		// claim task for logged in user
 	 	taskService.claim(task.getId(), loginName);
 	 	
 	 	// query task after claim
 		Task taskAfterClaim = taskService
	 			.createTaskQuery()
	 			.processInstanceId(pVariablesInReturn.getProcessInstanceId())
	 			.active()
	 			.singleResult();
 		
 		// after claiming
 		LOGGER.info("*** task getAssignee after claim: "+ taskAfterClaim.getAssignee());
 		
 		// assemble a basic JSON reply and return
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode returnJsonNode = mapper.createObjectNode();
		returnJsonNode
			.put("logged in user name", loginName)
			.put("processID", processID)
			.put("processInstanceID", pVariablesInReturn.getProcessInstanceId())
			.put("claimed task name", taskAfterClaim.getName())
			.put("claimed task instance ID", taskAfterClaim.getId())
			.put("task assignee", taskAfterClaim.getAssignee());

		return returnJsonNode; 
		
	}
	
}
