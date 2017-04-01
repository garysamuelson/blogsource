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

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Set of ReST examples for interacting with the Camunda engine 
 * <br><br>
 * Focus is: Case
 * 
 * @author GaryS
 *
 */
@RequestScoped
@Path("case")
public class Case {
  
  /**
   * Logging 
   */
  private final Logger LOGGER = Logger.getLogger(Case.class.getName());
  
  
  /**
   * Access to the Camunda Process API
   */
  @Inject
  private RuntimeService runtimeService;
  
  /**
   * Access to the Camunda Case-service API
   */
  @Inject
  private CaseService caseService;
  
  
  
  /**
   * 
   * @param hello
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("caseechopost")
  public JsonNode caseEchoPost(JsonNode hello)  {
    
    // Testing out a new approach to receiving and returning
    // Jackson JsonNode.
    // Attempting to avoid all the in-code serialization overhead.
    // We'll see if this works out in the end.
    
    LOGGER.info("*** caseEchoPost - invoked");    
    
    // get case ID
    // NOTE: still not sure which id is used: case id or id ? 
    String caseID = hello.findValue("caseID").asText();
    
    LOGGER.info("*** caseEchoPost - caseID: " + caseID);  
    
    // get the case/process variables
    final JsonNode arrNode = hello.get("processVariables");
    Map<String, Object> variables = new HashMap<String, Object>();

    for (final JsonNode jsonNode : arrNode) {
      variables.put(jsonNode.findValue("name").asText(), jsonNode.findValue("value").asText());
    }
    
    // start the case
    /**
    ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
        .setVariables(variables)
        .executeWithVariablesInReturn();
    **/
    
    /**
    CaseInstance caseInstance = caseService
        .withCaseExecution(caseID)
        .setVariables(variables)
        .manualStart();
    **/
    
    CaseInstance caseInstance = caseService
        .createCaseInstanceByKey(caseID);
    
    String ciid = caseInstance.getCaseInstanceId();
    
    caseService.setVariables(ciid, variables);
    
    //caseService.m
    
    LOGGER.info("*** caseEchoPost - case started - ciid: " + ciid);
    
    LOGGER.info("*** caseEchoPost - case is active: " + caseInstance.isActive());  
    
    
  
    return hello;
  
  }
  

}
