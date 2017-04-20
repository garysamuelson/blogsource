package com.talkdata.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.Tag;

import com.amazonaws.AmazonClientException;
import com.amazonaws.Request;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.talkdata.awsutils.Connection;
import com.talkdata.awsutils.PollingPolka;

import io.reactivex.Observable;
import io.reactivex.Single;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import io.reactivex.observables.ConnectableObservable;



/**
 * 
 * A collection of EC2 instance management utilities - available via ReST
 * 
 * 
 * @author Gary Samuelson
 *
 */
@Path("manageinstance")
@RequestScoped
public class ManageInstance {
  
  /**
   * The logger
   * NOTE: switching to static (not sure I wasn't using static prior). 
   */
  private final static Logger LOGGER = Logger.getLogger(ManageInstance.class.getName());
  
  
  
  /**
   * 
   * 
   * @param serviceRequest
   * @param response
   * @throws Exception
   */
  @POST
  @Path("startinstance")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void startInstance(final JsonNode serviceRequest, @Suspended final AsyncResponse response) throws Exception {
    
    LOGGER.info("*** startInstance invoked ");
        
    final AWSCredentials credentials = Connection.getCredentials(serviceRequest);
    final AmazonEC2AsyncClient eC2AsyncClient = Connection.getEC2AsyncClient(serviceRequest, credentials);
    
    String instanceId = serviceRequest.get("ec2StartInstance").get("instanceId").asText();
    
    StartInstancesRequest startInstancesRequest = 
        new StartInstancesRequest()
          .withInstanceIds(instanceId);
    
    // dry run request
    // Request<StartInstancesRequest> dryRunStartInstancesRequest = startInstancesRequest.getDryRunRequest();
    
    
    //Future<StartInstancesResult> startInstanceResultFuture = eC2AsyncClient.startInstancesAsync(startInstancesRequest);
    //StartInstancesResult startInstancesResult = null;

    //startInstanceResultFuture = eC2AsyncClient.startInstancesAsync(startInstancesRequest);
    
    //Single<List<InstanceStateChange>> startInstancesResult = 
    
    Observable.fromFuture(eC2AsyncClient.startInstancesAsync(startInstancesRequest))
      .flatMap(x -> Observable.fromArray(getInstances(x)))
      //.blockingForEach(x -> LOGGER.info("*** count: " + x.size()));
      .forEach(x -> LOGGER.info("*** startInstance - count: " + x.size()));
      
      
      
    LOGGER.info("*** startInstance done");  
    
    /**
    PollingPolka pollingPolka = new PollingPolka();
    //StartInstancesResult startInstancesResult = eC2AsyncClient.startInstances(startInstancesRequest);
    
    //Observable<JsonNode> jsonNodeResultsObs =
        pollingPolka.startEc2InstanceExecutor(eC2AsyncClient,startInstancesRequest)
          //.flatMap(x -> Observable.fromArray(getInstances(x)));
          .flatMap(x -> Observable.fromArray((Object)getInstances(x)));
        
          //.first()
          //.flatMap(instance -> Observable.just(describeInstancesResultToJsonNode(eC2AsyncClient, instance)));

    **/    
    response.resume(serviceRequest); 
 
  }
  
  
  
  @POST
  @Path("stopinstance")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void stopInstance(JsonNode serviceRequest, @Suspended final AsyncResponse response) throws Exception {
    
    LOGGER.info("*** linuxrhel invoked ");
        

  }
  
  
  /**
   * 
   * @param startInstancesResult
   * @return
   */
  protected List<InstanceStateChange> getInstances(final StartInstancesResult startInstancesResult) {
  
    
    LOGGER.info("*** getInstances invoked");
    
    List<InstanceStateChange> instanceListObs = startInstancesResult.getStartingInstances();

    return instanceListObs;
    //return Observable.from(instanceListObs);
    //return Observable.just(instanceListObs);
  }
  
  
  
  
  
  /**
   * 
   * @param eC2AsyncClient
   * @param instance
   * @return
   */
  protected JsonNode describeInstanceForJson(final AmazonEC2AsyncClient eC2AsyncClient,
      final Instance instance) {
    
    LOGGER.info("*** describeInstanceForJson invoked");
    
    DescribeInstancesRequest request = new DescribeInstancesRequest();
      request.withInstanceIds(instance.getInstanceId());
    
      ObjectMapper mapper = new ObjectMapper();
      DescribeInstancesResult result = null;
      JsonNode jsonNode= null;
      // String jsonString=null;
      try {
        LOGGER.info("*** describeInstanceForJson - ec2async.describeInstance Id: " + instance.getInstanceId());
        result  = eC2AsyncClient.describeInstances(request);
        jsonNode = mapper.valueToTree(result); 
      } catch (AmazonEC2Exception ec2Exception) {
        ec2Exception.printStackTrace();
        //String errorMessage = "Doh! Bad instance ID?";
        // NOTE: will need to do something with this error. 
      }   
      
      
    return jsonNode;
    
   
  }
  
  
  /**
   * 
   * @param eC2AsyncClient
   * @param startInstancesRequest
   * @return
   */
  /**
  protected Observable<StartInstancesResult> startEc2InstanceExecutor(final AmazonEC2AsyncClient eC2AsyncClient,
      final StartInstancesRequest startInstancesRequest) {

    return Observable.create((ObservableOnSubscribe<StartInstancesResult>) subscriber -> {
      Runnable r = () -> {
        LOGGER.info("*** startEc2InstanceExecutor runable invoked");

        
        subscriber.onNext(startEc2Instance(eC2AsyncClient, startInstancesRequest));
        subscriber.onComplete();

      };
      executor.execute(r);
    });
        
    
  }
  **/
  
  

}