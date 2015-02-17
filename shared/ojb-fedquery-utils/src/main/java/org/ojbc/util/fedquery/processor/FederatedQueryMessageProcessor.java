package org.ojbc.util.fedquery.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ojbc.util.camel.helper.OJBUtils;
import org.ojbc.util.model.query.FederatedQueryProfile;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FederatedQueryMessageProcessor {
	
	private static final Log log = LogFactory.getLog( FederatedQueryMessageProcessor.class );
	private Map<String, List<FederatedQueryProfile>> federatedQueryManager;	
	private Map<String, String> federatedQueryEndpointMap;
	
	/**
	 * This method accepts a NodeList of federated query endpoints that are to be called.  The NodeList is generated
	 * in the Camel route by an XPath which retrieves the system names from the XML message.
	 * 
	 * Each system is looked up in the federatedQueryEndpointMap which contains the actual endpoints that will be called.
	 * A federated query profile array is also set up so we can correlate requests with responses and check that all endpoints responded.
	 * 
	 * @param exchange
	 * @param federatedQueryEndpoints
	 * @throws Exception
	 */
	
	public void processSystemName(Exchange exchange, @Header("federatedQueryEndpointsNodeList") NodeList federatedQueryEndpoints) throws Exception
	{
		ArrayList<FederatedQueryProfile> queryProfileArray = new ArrayList<FederatedQueryProfile>();
		
		StringBuffer federatedQueryRecipientListHeader = new StringBuffer();
	
		//Iterate through the NodeList to see what systems to call
		for(int i=0; i<federatedQueryEndpoints.getLength(); i++){
			Node childNode = federatedQueryEndpoints.item(i);
			  
			String systemName = childNode.getTextContent();
			
			log.debug("System to call for federated query: " + systemName);
		
			//Create a new federated query profile
			FederatedQueryProfile systemProfile = new FederatedQueryProfile(systemName, false);
			
			//Add to array which is in the 'Map' of all queries, the map is keyed by WS-Address Message ID
			queryProfileArray.add(systemProfile);
			
			//Look in Federated Query Map to get endpoint name from System name in message
			String endpointID = federatedQueryEndpointMap.get(systemName);
			
			//Add to String which will make Camel Recipient List header
			federatedQueryRecipientListHeader.append(endpointID);
			federatedQueryRecipientListHeader.append(",");
			
		}

		log.debug("Federated Query Recipient List Header: " + federatedQueryRecipientListHeader.toString());
		
		//Set recipient list header referred to in Camel Route
		exchange.getIn().setHeader("federatedQueryRecipientListHeader", federatedQueryRecipientListHeader.toString());
		
		//Add the federated query profile array to query manager map
		String requestID = (String)exchange.getIn().getHeader("federatedQueryRequestGUID");
		federatedQueryManager.put(requestID, queryProfileArray);
		
	}
	
	/**
	 * The reply handlers will all call this method.  This method retrieves the WS-Addressing Message ID, looks in 
	 * the federated query manager to retrieve the query profile array, marks the response that was received and 
	 * updates two important headers for the aggregator: 
	 * federatedQueryRequestGUID (the correlation expression) 
	 * federatedQueryNumberOfEndpointsRequested (the number of expected responses) 
	 * 
	 * There is also a 'timeout' in the aggregator which is configurable in the properties file.  The aggregator
	 * either waits for the expected number of responses or the timeout.
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	public void processFederatedResponse(Exchange exchange) throws Exception
	{

		//Retrieve the WS-Addressing Message ID
		HashMap<String, String> wsAddressingHeadersMap = OJBUtils.returnWSAddressingHeadersFromCamelSoapHeaders(exchange);
		
		String requestID = wsAddressingHeadersMap.get("MessageID");	
		
		if (StringUtils.isBlank(requestID))
		{	
			throw new Exception("Unable to find unique ID in Soap Header.  Was the message ID set in the Soap WS Addressing header?");
		}	
		
		//Get the Operation that was invoked
		//String contentType = (String)exchange.getIn().getHeader(Exchange.CONTENT_TYPE);
	
		//String operation = StringUtils.substringBetween(contentType, "action=\"", "\"");
		//log.debug("\n\nProcessing response for action: " + operation);
		
		//Based off of the WS-Addressing Message ID, retrieve the query profile array (all the endpoints initially called)
		ArrayList<FederatedQueryProfile> queryProfileArray = null;
		queryProfileArray = (ArrayList<FederatedQueryProfile>)federatedQueryManager.get(requestID);
		
		if (queryProfileArray == null)
		{
			log.error ("The request ID had no corresponding map entry: " + requestID);
			throw new Exception("The response received could not be correlated to a request");
		}	
			
		boolean searchProfileFound = false;
		
		//Retrieve the search profile Camel header
		//this is typically an xpath to the source system called
		String searchProfile = (String)exchange.getIn().getHeader("searchProfile");
		log.debug("Search Profile Returned: " + searchProfile);
		
		//Mark the response that was received as true
		for (FederatedQueryProfile queryProfile : queryProfileArray)
		{
			if (queryProfile.getServiceName().equals(searchProfile))
			{
				queryProfile.setResponseReceived(true);
				searchProfileFound = true;
				break;
			}	
		}	
		
		//Log an error if the search profile is not found.  This typically happens after a late response is received after timeout
		if (searchProfileFound == false)
		{
			log.error("A response was received for operation: " + searchProfile + " however operation was not in the original request");
		}	
		
		//Update the manager with the latest query profile array
		federatedQueryManager.put(requestID, queryProfileArray);
		
		//Set the Camel headers used by the aggregator to indicate how many responses have been received
		exchange.getIn().setHeader("federatedQueryNumberOfEndpointsRequested", queryProfileArray.size() + 1);
		exchange.getIn().setHeader("federatedQueryRequestGUID", requestID);
		
	}


		public Map<String, List<FederatedQueryProfile>> getFederatedQueryManager() {
			return federatedQueryManager;
		}


		public void setFederatedQueryManager(
				Map<String, List<FederatedQueryProfile>> federatedQueryManager) {
			this.federatedQueryManager = federatedQueryManager;
		}


		public Map<String, String> getFederatedQueryEndpointMap() {
			return federatedQueryEndpointMap;
		}


		public void setFederatedQueryEndpointMap(
				Map<String, String> federatedQueryEndpointMap) {
			this.federatedQueryEndpointMap = federatedQueryEndpointMap;
		}
	
}
