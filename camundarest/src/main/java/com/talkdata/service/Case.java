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
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.model.cmmn.instance.Task;
import org.camunda.spin.json.SpinJsonNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

// SPIN imports - this will get confusing with plain-old jackson
//import static org.camunda.spin.Spin.*;
//import static org.camunda.spin.DataFormats.*;
import static org.camunda.spin.DataFormats.*;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
// org.camunda.bpm.engine.variable.Variables.

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
  
  @Inject
  private TaskService taskService;  
  
  
  
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
        .withCaseDefinitionByKey(caseID)
        .setVariables(variables)
        .create();
        //.createCaseInstanceByKey(caseID);
    
    String ciid = caseInstance.getCaseInstanceId();
    
    //caseService.setVariables(ciid, variables);
    
    
    
    /**
    Task task = taskService
        .createTaskQuery()
        .processInstanceId(ciid)
        .active()
        .singleResult();
    **/
    
    
    //caseService.m
    
    LOGGER.info("*** caseEchoPost - case started - ciid: " + ciid);
    
    LOGGER.info("*** caseEchoPost - case is active: " + caseInstance.isActive());  
    
    
  
    return hello;
  
  }
  
  
  /**
   * 
   * Sample payload on the post
    {
      "caseID": "case_simple_01_cid",
      "processVariables": [
        {
          "name": "hello",
          "value": "greetings BPM"
        },
        {
          "name": "myName",
          "value": "Joe Smith"
        },
        {
          "name": "customer",
          "value": "Bob"
        }
      ]
    }
   *  
   * @param hello
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("casebasicstart")
  public JsonNode caseBasicStart(JsonNode postpayload)  {
    
    // Testing out a new approach to receiving and returning
    // Jackson JsonNode.
    
    LOGGER.info("******************************");
    LOGGER.info("*** caseBasicStart - invoked");    
    LOGGER.info("******************************");   
    
    // get case ID
    // NOTE: Using the "General -> Case Id" field value from the CMMN case model
    String caseID = postpayload.findValue("caseID").asText();
    
    LOGGER.info("*** caseBasicStart - caseID: " + caseID);  
    
    // get the case/process variables
    final JsonNode arrNode = postpayload.get("processVariables");
    Map<String, Object> variables = new HashMap<String, Object>();

    for (final JsonNode jsonNode : arrNode) {
      variables.put(jsonNode.findValue("name").asText(), jsonNode.findValue("value").asText());
    }
    
    // start the case
    // NOTE: It appears that Case instance execution takes this thread - since, we
    //  do not see any logger output (below) until the case completes.
    //  Though we are not blocked and waiting for human tasks - the non-human tasks may
    //    be causing this thread to block. 
    //  At this point, the non-human tasks use the "sync" setting in CMMN diagram. 
    CaseInstance caseInstance = caseService
        .withCaseDefinitionByKey(caseID) // NOTE: this is the field value from General -> Case Id field
        .setVariables(variables)
        .create();
        
    String ciid = caseInstance.getCaseInstanceId();
    
    // Case is started - and stuff is now running
    //  Quickly claim the case to save ourselves an extra click on the "claim" link
    if (postpayload.hasNonNull("claim")) {
      String claimId = postpayload.findValue("claim").asText();
      LOGGER.info("*** caseBasicStart - found user to claim: " + claimId);  
      /**
      CaseInstance caseInstances = 
          caseService
            .createCaseInstanceQuery()
            .active()
            .singleResult();
      
      caseService.
      **/
      
      // org.camunda.bpm.engine.task.Task task = taskService
      
      // if case is open, we claim it
      if (caseInstance.isActive()) {
        
        org.camunda.bpm.engine.task.Task task 
          = taskService
            .createTaskQuery()
            .caseInstanceId(ciid)
            //.taskId("task_startNewCase_id")
            .active()
            .singleResult();
        
        taskService.claim(task.getId(), claimId);
      }
            
    }

    
    
    LOGGER.info("*** caseBasicStart - case started - ciid: " + ciid);
    
    LOGGER.info("*** caseBasicStart - case is active: " + caseInstance.isActive());  
    
    
    // Simply returning back, or echoing, the received payload. 
    // NOT yet reflecting actual case-execution results
    return postpayload;
  
  }
  
  
  
  /**
   * 
   * 
   * @param postpayload
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("casebasicstartspin")
  public JsonNode caseBasicStartSpin(JsonNode postpayload)  throws Exception {
    
    // Testing out a new approach to receiving and returning
    // Jackson JsonNode.
    
    LOGGER.info("********************************");
    LOGGER.info("*** caseBasicStartSpin - invoked");    
    LOGGER.info("********************************");   
    
    // get case ID
    // NOTE: Using the "General -> Case Id" field value from the CMMN case model
    String caseID = postpayload.findValue("caseID").asText();
    
    LOGGER.info("*** caseBasicStart - caseID: " + caseID);  
    
    // get the case/process variables
    final JsonNode arrNode = postpayload.get("processVariables");
    Map<String, Object> variables = new HashMap<String, Object>();

    ObjectMapper mapper = new ObjectMapper();
    for (final JsonNode jsonNode : arrNode) {
      // check for variable with children - which we'll assume are of type JSON
      LOGGER.info("*** jsonNode - name: " + jsonNode.get("name").asText() + " , size: " + jsonNode.get("value").size());
      if (jsonNode.get("value").size() > 0) {
        variables.put(jsonNode.get("name").asText(), Spin.JSON(mapper.writer().writeValueAsString(jsonNode.get("value"))));
      } else {
        variables.put(jsonNode.get("name").asText(), jsonNode.get("value").asText());
      }
    }
    
    //-----------------------
    // Spin Test
    // ----------------------
    // and now we add a special Camunda type called SPIN JSON
    //  this is to get the lists available in our in-line Camunda forms. 
    SpinJsonNode spinJsonCustomer = Spin.JSON("{\"customer\": \"Kermit\"}");
    variables.put("spinJsonCustomerManualAdd",spinJsonCustomer); // <<< this works
    
    VariableMap variableMap2 = Variables.createVariables();
    
    // VariableMap variableMap = (VariableMap) new HashMap<String, Object>(); // <<< this fails
    // VariableMap variableMap = Variables.create();
    //VariableMap variableMap = Variables.create().
        
    // Spin customer doesn't resolve well. 
    for (final JsonNode jsonNode : arrNode) {
      variableMap2.put(jsonNode.findValue("name").asText(), jsonNode.findValue("value").asText());
    }
    
    SpinJsonNode spinJsonCustomer2 = Spin.JSON("{\"customer\": \"Kermit\"}");
    variableMap2.put("spinJsonCustomer2",spinJsonCustomer2); // <<< this works
    
    
    // start the case
    // NOTE: It appears that Case instance execution takes this thread - since, we
    //  do not see any logger output (below) until the case completes.
    //  Though we are not blocked and waiting for human tasks - the non-human tasks may
    //    be causing this thread to block. 
    //  At this point, the non-human tasks use the "sync" setting in CMMN diagram. 
    CaseInstance caseInstance = caseService
        .withCaseDefinitionByKey(caseID) // NOTE: this is the field value from General -> Case Id field
        .setVariables(variables)
        .create();
        
    
    
    String ciid = caseInstance.getCaseInstanceId();
    
    // Case is started - and stuff is now running
    //  Quickly claim the case to save ourselves an extra click on the "claim" link
    if (postpayload.hasNonNull("claim")) {
      String claimId = postpayload.findValue("claim").asText();
      LOGGER.info("*** caseBasicStart - found user to claim: " + claimId);  
      /**
      CaseInstance caseInstances = 
          caseService
            .createCaseInstanceQuery()
            .active()
            .singleResult();
      
      caseService.
      **/
      
      // org.camunda.bpm.engine.task.Task task = taskService
      
      // if case is open, we claim it
      // TEMP COMMENT OUT: we'll fix this in a minute
      /**
      if (caseInstance.isActive()) {
        
        org.camunda.bpm.engine.task.Task task 
          = taskService
            .createTaskQuery()
            .caseInstanceId(ciid)
            //.taskId("task_startNewCase_id")
            .active()
            .singleResult();
        
        taskService.claim(task.getId(), claimId);
      }
      **/
            
    }

    
    
    LOGGER.info("*** caseBasicStart - case started - ciid: " + ciid);
    
    LOGGER.info("*** caseBasicStart - case is active: " + caseInstance.isActive());  
    
    
    // Simply returning back, or echoing, the received payload. 
    // NOT yet reflecting actual case-execution results
    return postpayload;
  
  }
  
  
  
  

}
