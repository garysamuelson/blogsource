package com.talkdata.service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Set of ReST examples for interacting with the Camunda engine 
 * @author GaryS
 *
 */
@RequestScoped
@Path("bpm")
@Consumes({ "application/xml", "application/json","application/text" })
@Produces({ "application/xml", "application/json","application/text" })
public class Bpm {
	
	/**
	 * Logging 
	 */
	private final Logger LOGGER = Logger.getLogger(Bpm.class.getName());
	
	
	/**
	 * Access to the Camunda Process API
	 */
	@Inject
	private RuntimeService runtimeService;
	
	
	/**
	 * Simple echo test of our ReST, JAX-RS service infrastructure
	 * <br><br>
	 * 
	 * @param hello
	 * @return the String echo - in JSON
	 */
	@GET
    @Path("echoget/{hello}")
	@Produces(MediaType.APPLICATION_JSON)
    public String echoget(@PathParam("hello") String hello){
		
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
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("echopost")
	public JsonNode echoPost(JsonNode hello) {
		
		// Testing out a new approach to receiving and returning 
		//	Jackson JsonNode. 
		// Attempting to avoid all the in-code serialization overhead.
		// We'll see if this works out in the end. 
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			// no formatting
			// mapper.writer().writeValueAsString(hello);
			// with formatting
			String logMessage = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(hello);
			LOGGER.info("*** echopost - hello: \n" + logMessage);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return hello;
	}
	
	
	
	/**
	 * 
	 * @param processID
	 * @param hello
	 * @return
	 */
	@GET
	@Path("bpmechoget/{processID}/{hello}")
	@Produces(MediaType.APPLICATION_JSON)
	public String bpmEchoGet(@PathParam("processID") String processID, 
		    				@PathParam("hello") String hello ){
		
		LOGGER.info("*** bpmEchoGet - processID: " + processID);
		LOGGER.info("*** bpmEchoGet - hello: " + hello);
		
		ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
				.setVariable("hello", hello)
				.executeWithVariablesInReturn();

		String piid = pVariablesInReturn.getProcessInstanceId();

		return "{\"processInstanceID\": \"" + piid + "\"}";
		

		
	}
	
	
	/**
	 * 
	 * 
	 * @param hello
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("bpmechopost")
	public String bpmEchoPost(JsonNode hello) {
		
		// Testing out a new approach to receiving and returning 
		//	Jackson JsonNode. 
		// Attempting to avoid all the in-code serialization overhead.
		// We'll see if this works out in the end. 
		
		// get processID
		String processID = hello.findValue("processID").asText();
		
		// get the process variables
		final JsonNode arrNode = hello.get("processVariables");
		Map<String,Object> variables = new HashMap<String,Object>();
		
		for(final JsonNode jsonNode : arrNode) {
			variables.put(jsonNode.findValue("name").asText(),jsonNode.findValue("value").asText());
		}
		
		// start the process
		ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
				.setVariables(variables)
				.executeWithVariablesInReturn();

		String piid = pVariablesInReturn.getProcessInstanceId();

		return "{\"processInstanceID\": \"" + piid + "\"}";
		
		
	}
	
	
	
	

}
