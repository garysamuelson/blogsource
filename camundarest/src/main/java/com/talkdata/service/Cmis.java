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

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ContextName;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import com.fasterxml.jackson.databind.JsonNode;


@RequestScoped
@Path("cmis")
public class Cmis {
  
  /**
   * Logging 
   */
  private final Logger LOGGER = Logger.getLogger(Cmis.class.getName());
  
  
  @Inject
  @ContextName("CamelRoute01")
  private CamelContext camelContext; // this is required as a workaround for a minor Weld annoyance.
  
  
  
  /**
   * NOTE:  Though camel is working fine... the CMIS adapter is a little tedious. 
   *  Not using the Camal CMIS adapter for this set of examples  (at least for now)
   * 
   * 
   * @return
   */
  @GET
  @Path("pingcamel/{hello}")
  @Produces(MediaType.APPLICATION_JSON)
  public String pingCamel(@PathParam("hello") String hello) {
    
    LOGGER.info("*** pingCamel invoked");
    
    ProducerTemplate producer = camelContext.createProducerTemplate();
    
    /**
    Endpoint endpoint = camelContext.getEndpoint("direct:start");
    template.setDefaultEndpoint(endpoint);
    template.sendBody("Test");
    **/
    
    
    //ConsumerTemplate consumer = camelContext.createConsumerTemplate()
    
    // producer.sendBody("direct:pingcamel","foo camel body");
    //String foo = producer.requestBody("direct:pingcamel","foo camel body",String.class);
    // producer.sendBody("direct:cmisq01","SELECT * FROM cmis:document WHERE cmis:name LIKE 'acm%'");
    producer.sendBody("direct:cmisq01","SELECT cmis:description FROM cmis:document WHERE cmis:name LIKE 'acm%'");
    // SELECT cmis:description FROM cmis:document WHERE cmis:name LIKE 'acm%'
    //LOGGER.info("*** pingCamel received: " + foo);
    
    String echoReply = "{\"echoback\": \"" + hello + "\"}";

    return echoReply;
    
  }
  
  
  /**
   * 
   * @return
   */
  @GET
  @Path("pingcmis")
  @Produces(MediaType.APPLICATION_JSON)
  public String pingCmis() {
    
    LOGGER.info("*** pingCmis invoked");
    
    // copy/paste from CMIS examples 
    //  https://chemistry.apache.org/docs/cmis-samples/samples/create-session/index.html
    
    // default factory implementation
    //SessionFactory factory = SessionFactoryImpl.newInstance();
    Map<String, String> parameters = new HashMap<String, String>();

    // user credentials
    parameters.put(SessionParameter.USER, "admin");
    parameters.put(SessionParameter.PASSWORD, "admin");

    // connection settings
    parameters.put(SessionParameter.ATOMPUB_URL, "http://centosw02esx:8080/alfresco/api/-default-/public/cmis/versions/1.1/atom");
    parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
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
    
    //parameters.put(SessionParameter.REPOSITORY_ID, "myRepository");

    // create session
    // Session session = factory.createSession(parameters);
    
    
    
    
    String echoReply = "{\"foo\": \"bar\"}";
    return echoReply; 
  }
  
  
  
  
  
  
  

}
