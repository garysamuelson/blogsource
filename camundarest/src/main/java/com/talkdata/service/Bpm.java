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
import org.camunda.bpm.engine.variable.VariableMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Set of ReST examples for interacting with the Camunda engine 
 * 
 * @author GaryS
 *
 */
@RequestScoped
@Path("bpm")
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
	 * Simple echo GET test of our ReST, JAX-RS service infrastructure <br>
	 * <br>
	 * 
	 * @param hello
	 * @return the String echo - in JSON
	 */
	@GET
	@Path("echoget/{hello}")
	@Produces(MediaType.APPLICATION_JSON)
	public String echoget(@PathParam("hello") String hello) {

		LOGGER.info("*** echoget - hello: " + hello);

		// assemble a basic JSON reply and return
		String echoReply = "{\"echoback\": \"" + hello + "\"}";

		return echoReply;
	}
	
	
	/**
	 * Simple echo POST test of our ReST, JAX-RS service infrastructure
	 * 
	 * @param hello
	 * @return
	 * @throws JsonProcessingException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("echopost")
	public JsonNode echoPost(JsonNode hello) throws JsonProcessingException {

		// Testing out a new approach to receiving and returning
		// Jackson JsonNode.
		// Attempting to avoid all the in-code serialization overhead.
		// We'll see if this works out in the end.
		ObjectMapper mapper = new ObjectMapper();

		// no formatting
		// mapper.writer().writeValueAsString(hello);
		// with formatting
		String logMessage = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(hello);
		LOGGER.info("*** echopost - hello: \n" + logMessage);

		return hello;
	}
	
	
	
	/**
	 * Basic start process example via ReST GET
	 * 
	 * @param processID
	 * @param hello
	 * @return
	 */
	@GET
	@Path("bpmechoget/{processID}/{hello}")
	@Produces(MediaType.APPLICATION_JSON)
	public String bpmEchoGet(@PathParam("processID") String processID, @PathParam("hello") String hello) {

		LOGGER.info("*** bpmEchoGet - processID: " + processID);
		LOGGER.info("*** bpmEchoGet - hello: " + hello);

		ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
				.setVariable("hello", hello)
				.executeWithVariablesInReturn();

		String piid = pVariablesInReturn.getProcessInstanceId();

		return "{\"processInstanceID\": \"" + piid + "\"}";

	}
	
	
	/**
	 * More advanced start process example via ReST POST
	 * 
	 * NOTE: Camunda includes its own ReST API. This example demonstrates how to 
	 * 			build your own interfaces. The goal is a facade for use with 
	 * 			external users who need a reduced, or simplified, set of features. 
	 * 
	 * sample input:
	 * 
		{
			"processID": "simple_process_example_pid",
			"processVariables": [
				{
					"name": "hello",
					"value": "greetings BPM"
				},
				{
					"name": "myName",
					"value": "Joe Smith"
				}
			]
		}
	 * 
	 * @param hello
	 * @return
	 * @throws JsonProcessingException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bpmechopost")
	public JsonNode bpmEchoPost(JsonNode hello)  {

		// Testing out a new approach to receiving and returning
		// Jackson JsonNode.
		// Attempting to avoid all the in-code serialization overhead.
		// We'll see if this works out in the end.

		// get processID
		String processID = hello.findValue("processID").asText();

		// get the process variables
		final JsonNode arrNode = hello.get("processVariables");
		Map<String, Object> variables = new HashMap<String, Object>();

		for (final JsonNode jsonNode : arrNode) {
			variables.put(jsonNode.findValue("name").asText(), jsonNode.findValue("value").asText());
		}

		// start the process
		ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
				.setVariables(variables)
				.executeWithVariablesInReturn();

		String piid = pVariablesInReturn.getProcessInstanceId();
		boolean isEnded = pVariablesInReturn.isEnded();
		VariableMap variableMap = pVariablesInReturn.getVariables();

		// this time we build a proper JSON return value - using Jackson
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootObjectNode = mapper.createObjectNode();

		// Extract completed process results or output
		// set the process processID, InstanceID, and isEnded 
		// variables as child JSON nodes
		rootObjectNode
			.put("processID", processID)
			.put("processInstanceID", piid)
			.put("isEnded", isEnded);

		// create the JSON node to hold the BPM returned variables
		ObjectNode processVariablesNode = mapper.createObjectNode();
		// attach to parent
		rootObjectNode.set("processVariables", processVariablesNode);

		// print returned process variables and append to return JSON object
		variableMap.forEach((processVariableName, processVariableValue) -> {
			// log values
			LOGGER.info(processVariableName.toString() + " : " + processVariableValue.toString());

			// build JSON return
			processVariablesNode.put(processVariableName.toString(), processVariableValue.toString());
		});

		// create writer and return pretty output
		// 	NOTE: Converting to String isn't required since we're now returning a JsonNode
		/**
		String results = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootObjectNode);
		return results;
		**/
		
		return rootObjectNode;

	}
	
	
	
	

}
