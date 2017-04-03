package com.talkdata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionManager;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;



@RequestScoped
@Path("admin")
public class admin {
  
  /**
   * Logging 
   */
  private final Logger LOGGER = Logger.getLogger(admin.class.getName());
  
  @Inject
  private RepositoryService repositoryService;
  
  @Inject
  private RuntimeService runtimeService;
  
  
  @Inject
  private CaseService caseService;
  
  
  @Inject
  private HistoryService historyService;
  
  
  
  //@Inject
  //private CaseExecutionManager caseExecutionManager;
  
  
  @Inject
  private TaskService taskService;  
  
  /***
   * 
   * @return
   */
  @GET
  @Path("deleteallinstances")
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode deleteAllInstances() {

    LOGGER.info("*** deleteAllInstances invoked");
    
    /**
    List<CaseExecution> CaseExecutions = 
        caseService
          //.createCaseInstanceQuery()
          .createCaseExecutionQuery()
          //.active()
          .list();
      **/
    
    List<CaseInstance> caseInstances = 
        caseService
          .createCaseInstanceQuery()
          //.createCaseExecutionQuery()
          //.active()
          .list();
    
    List<String> caseInstanceIds = new ArrayList<String>();
    // for (CaseExecution caseExecution : CaseExecutions) {
    for (CaseInstance caseInstance : caseInstances) {
      caseInstanceIds.add(caseInstance.getId());
    }
    
    LOGGER.info("*** deleteAllInstances count: " + caseInstanceIds.size());
    
    // CaseExecutionManager caseExecutionManager = new  CaseExecutionManager();
    //ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    
    //caseService.terminateCaseExecution(caseExecutionId);
    
    //processEngine.getManagementService().get
    
    for (String caseInstanceId : caseInstanceIds) {
      try {
        caseService.terminateCaseExecution(caseInstanceId);
        //historyService.deleteHistoricCaseInstance(caseInstanceId);
      } catch (Exception e) {
        // do nothing
      }
    }
    
    // clean up history
    
    List<HistoricCaseInstance> historicCaseInstances = 
      historyService
        .createHistoricCaseInstanceQuery()
        .list();
    
    List<String> histCaseInstanceIds = new ArrayList<String>();
    for (HistoricCaseInstance historicCaseInstance : historicCaseInstances) {
      histCaseInstanceIds.add(historicCaseInstance.getId());
    }
        
    for (String histCaseInstanceId : histCaseInstanceIds) {
      try {
        //caseService.terminateCaseExecution(caseInstanceId);
        historyService.deleteHistoricCaseInstance(histCaseInstanceId);
      } catch (Exception e) {
        // do nothing
      }
    }

    // runtimeService.deleteProcessInstancesAsync(caseInstanceIds, null, "foo reason");
    //caseExecutionManager.del
    
    /**
    // get list of instances
    List<ProcessInstance> processInstances =
      runtimeService.createProcessInstanceQuery()
        .list();
    
    List<String> processInstanceIds = new ArrayList<String>();
    
    for (ProcessInstance processInstance : processInstances) {
      processInstanceIds.add(processInstance.getId());
    }
    **/

    // this time we build a proper JSON return value - using Jackson
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootObjectNode = mapper.createObjectNode();

    // create the JSON node to hold the BPM returned variables
    // ObjectNode processInstanceList = mapper.createObjectNode();
    ArrayNode caseInstanceList = mapper.createArrayNode();
    // attach to parent
    // rootObjectNode.set("processInstanceIds", processInstanceList);
    rootObjectNode.set("caseInstanceIds", caseInstanceList);
    for (String caseInstanceId : caseInstanceIds) {
      caseInstanceList.add(caseInstanceId);
    }
    
    // caseInstanceIds
    return rootObjectNode;
  }
  
  
  /**
   * 
   * @return
   */
  @GET
  @Path("deletealldeployments")
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode deleteAllDeployments() {
    
    List<Deployment> deployments = repositoryService
      .createDeploymentQuery()
      .list();
   
    List<String> deploymentIds = new ArrayList<String>();
    for (Deployment deployment : deployments) {
      deploymentIds.add(deployment.getId());
    } 
   
    LOGGER.info("*** deleteAllDeployments count: " + deploymentIds.size()); 
    
   
   for(String deploymentId :  deploymentIds) {
     repositoryService.deleteDeployment(deploymentId, true);
     
   }
   
    
    // this time we build a proper JSON return value - using Jackson
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootObjectNode = mapper.createObjectNode();

    // create the JSON node to hold the BPM returned variables
    // ObjectNode processInstanceList = mapper.createObjectNode();
    ArrayNode deploymentList = mapper.createArrayNode();
    // attach to parent
    // rootObjectNode.set("processInstanceIds", processInstanceList);
    rootObjectNode.set("deploymentIds", deploymentList);
    for (String deploymentId : deploymentIds) {
      deploymentList.add(deploymentId);
    }
    
    // caseInstanceIds
    return rootObjectNode; 
    
    
  }
  
  
  @GET
  @Path("activityquery")
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode activityQuery() {
    
    List<HistoricActivityInstance> historicActivityInstances = historyService
      .createHistoricActivityInstanceQuery()
      .list();
    
    LOGGER.info("*** activityQuery count: " + historicActivityInstances.size()); 
    
    List<CaseInstance> caseInstances = 
        caseService
          .createCaseInstanceQuery()
          .list();
    
    LOGGER.info("*** caseInstances count: " + caseInstances.size());
    
    CaseInstance caseInstance = caseInstances.get(0);
    
    String caseInstanceId = caseInstance.getCaseInstanceId();
    
    LOGGER.info("*** caseInstances activityId: " + caseInstanceId);
    
    caseService.terminateCaseExecution(caseInstanceId);
    caseService.closeCaseInstance(caseInstanceId);
    
    historyService.deleteHistoricCaseInstance(caseInstanceId);
    
    return null;
  }
  
  
  @GET
  @Path("cleanupcaseinstances")
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode cleanUpCaseInstances() {
    
    
    List<CaseInstance> caseInstances = 
        caseService
          .createCaseInstanceQuery()
          .list();
    
    LOGGER.info("*** cleanUpCaseInstances count: " + caseInstances.size());
    
    for(CaseInstance caseInstance :  caseInstances) {
      
      String caseInstanceId = caseInstance.getCaseInstanceId();
      
      LOGGER.info("*** Case caseInstanceId: " + caseInstanceId);
      
      // if active: terminate and close
      if (caseInstance.isActive()) {
        LOGGER.info("*** Case is active - terminating and closing: " + caseInstanceId);
        caseService.terminateCaseExecution(caseInstanceId);
        caseService.closeCaseInstance(caseInstanceId);
      }
      
      // clean up history
      historyService.deleteHistoricCaseInstance(caseInstanceId);
      
    }
    
    // create a list of case Ids for return
    List<String> caseIds = new ArrayList<String>();
    for(CaseInstance caseInstance :  caseInstances) {
      caseIds.add(caseInstance.getId());
    } 
    
    // this time we build a proper JSON return value - using Jackson
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootObjectNode = mapper.createObjectNode();

    // create the JSON node to hold the BPM returned variables
    // ObjectNode processInstanceList = mapper.createObjectNode();
    ArrayNode caseInstanceList = mapper.createArrayNode();
    // attach to parent
    rootObjectNode.set("case IDs", caseInstanceList);
    for (String caseId : caseIds) {
      caseInstanceList.add(caseId);
    }
    
    // caseInstanceIds
    return rootObjectNode;
    
    
  }
  

}
