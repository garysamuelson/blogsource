package com.talkdata.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;

/**
 * Setup relationship with CMIS 
 *  - this saves us a mouse-click for task assignment... 
 * 
 * 
 * @author teamw
 *
 */
@Named("startCmisCase")
public class StartCmisCase {
  
  /**
   * The logger
   */
  private final Logger LOGGER = Logger.getLogger(StartCmisCase.class.getName());
  
  
  /**
   * 
   * @param execution
   * @throws Exception
   */
  public void setupCmisFolder(DelegateCaseExecution execution) throws Exception {
    
    LOGGER.info("*** setupCmisFolder invoked");
    
    // using static credentials on this first pass. Later adding in SSO
    Map<String, String> parameters = new HashMap<String, String>();

    // user credentials
    parameters.put(SessionParameter.USER, "admin");
    parameters.put(SessionParameter.PASSWORD, "admin");
    
    // connection settings
    parameters.put(SessionParameter.BROWSER_URL, "http://centosw02esx:8080/alfresco/api/-default-/public/cmis/versions/1.1/browser");
    parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
    
    SessionFactory factory = SessionFactoryImpl.newInstance();    
    List<Repository> repositories = factory.getRepositories(parameters);
    
    // Assuming we have a "-default-" in the repo. 
    // NOTE: yes... Java 8, streams, and lambdas are cool.  
    Repository cmisRepository =
      repositories
        .stream()
        .filter((x) -> x.getId().equalsIgnoreCase("-default-"))
        .findFirst()
        .orElse(null);
        
    if (cmisRepository == null) {
      LOGGER.info("*** pingCmis - did not find -default- repository!!!" );
      // then we just let stuff throw errors - this is not production code... 
    }
    
    LOGGER.info("*** pingCmis set repository: " + cmisRepository.getId());
    parameters.put(SessionParameter.REPOSITORY_ID, cmisRepository.getId());
    Session cmisSession = factory.createSession(parameters);
    
    Folder rootFolder = cmisSession.getRootFolder();
    
    OperationContext folderOpCtx = cmisSession.createOperationContext();
    folderOpCtx.setFilterString("cmis:objectId,cmis:objectTypeId,cmis:name,cmis:path");
    
    ItemIterable<CmisObject> cmisObjects = rootFolder.getChildren(folderOpCtx);
    // try using Streams now
    CmisObject cmisSharedFolderObject = 
        StreamSupport.stream( cmisObjects.spliterator(), false)
          .filter((x) -> x.getName().equalsIgnoreCase("Shared"))
          .findFirst()
          .orElse(null);
    
    if (cmisSharedFolderObject == null) {
      LOGGER.info("*** cmisSharedFolderObject not found!!!");
    }
    
    CmisObject cmisObject = cmisSession.getObject(cmisSharedFolderObject.getId());
    Folder sharedFolder = null;
    if (cmisObject instanceof Folder) {
      sharedFolder = (Folder) cmisObject;
    } else {
      LOGGER.info("*** pingCmis shared folder: is not a folder");
    }
    
    LOGGER.info("*** pingCmis found shared folder - Id: " + cmisSharedFolderObject.getId());
    
    execution.setVariable("cmisRepositoryId", cmisRepository.getId());
    execution.setVariable("cmisSharedFolderId", cmisSharedFolderObject.getId());
    execution.setVariable("cmisSharedFolderName", cmisSharedFolderObject.getName());
    execution.setVariable("cmisSharedFolderPath", sharedFolder.getPath());
    
  }
  

  
  
  
  

}
