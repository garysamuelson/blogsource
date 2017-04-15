package com.talkdata.task;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;

@Named("printProcessVars")
public class PrintProcessVars {
	
	/**
	 * The logger
	 */
	private final Logger LOGGER = Logger.getLogger(PrintProcessVars.class.getName());
	
  @Inject
  private CaseService caseService;
	
	
	/**
	 * Print Camunda process variables
	 * 
	 * @param execution
	 * @throws Exception
	 */
	public void printvariables(DelegateExecution execution) throws Exception {

		LOGGER.info("*******************");
		LOGGER.info("*** Process ID: "+execution.getProcessDefinitionId());
		LOGGER.info("*** TASK ID: "+execution.getCurrentActivityId());

		Map<String, Object> processVars = execution.getVariables();
		Iterator<Map.Entry<String,Object>> entries = processVars.entrySet().iterator();

		if (processVars.isEmpty()) {
			LOGGER.info("*** Process Variables: EMPTY");
		} else {
			LOGGER.info("*** Process Variables: ");
		} 

		// Print out all the variables. 
		while (entries.hasNext()) {
			Map.Entry<String, Object> thisEntry = entries.next();
			Object object = thisEntry.getValue();
			if (object != null) {
				LOGGER.info(thisEntry.getKey().toString() + " : "+object.toString()); 
			} else {
				LOGGER.info(thisEntry.getKey().toString() + " : null");
			}
		}
		java.util.Date date = new java.util.Date();
		LOGGER.info(">> Date Stamp: "+date);
		//java.util.Date date = new java.util.Date();
		LOGGER.info("*******************");



	}
	
	
	/**
	 * This is now working - need to use "Expression" of listener type. 
	 * 
	 * NOT! - this isn't fetching the case variables correctly
	 * 
	 * @param caseExecutionEntity
	 * @throws Exception
	 */
  public void printCaseVariables(DelegateCaseExecution execution) throws Exception {

    LOGGER.info("*** caseTest");
    
    if(execution == null) {
      LOGGER.info("*** execution is null!!!");
      return;
    }
    
    
    
    // =========================================
    
    LOGGER.info("*******************");
    LOGGER.info("*** CaseDefinitionId ID: "+execution.getCaseDefinitionId()); // getProcessDefinitionId())
    LOGGER.info("*** CaseInstanceId ID: " + execution.getCaseInstanceId());
    
    LOGGER.info("*** ActivityId ID: "+execution.getActivityId());  // getCurrentActivityId());

    // LOGGER.info("*** Has variables: "+ execution.hasVariables());

    
    Map<String, Object> processVars = execution.getVariables();
    //Map<String, Object> processVars = caseService.getVariables(execution.);
    Iterator<Map.Entry<String,Object>> entries = processVars.entrySet().iterator();

    if (processVars.isEmpty()) {
      LOGGER.info("*** Process Variables: EMPTY");
    } else {
      LOGGER.info("*** Process Variables: ");
    } 

    // Print out all the variables. 
    while (entries.hasNext()) {
      Map.Entry<String, Object> thisEntry = entries.next();
      Object object = thisEntry.getValue();
      if (object != null) {
        LOGGER.info(thisEntry.getKey().toString() + " : "+object.toString()); 
      } else {
        LOGGER.info(thisEntry.getKey().toString() + " : null");
      }
    }
    java.util.Date date = new java.util.Date();
    LOGGER.info(">> Date Stamp: "+date);
    //java.util.Date date = new java.util.Date();
    LOGGER.info("*******************");
    
    

  }
	
	
	
	/**
	 * 
	 * 
	 * @throws Exception
	 */
	public void ping() throws Exception {
	  
	  LOGGER.info("*** ping");
	  
	  
	}
	
	
  public void methodOne(DelegateTask task) {
    LOGGER.info("*** methodOne");
    
  }

}
