/*
 * Unless explicitly acquired and licensed from Licensor under another license, the contents of
 * this file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent
 * versions as allowed by the RPL, and You may not copy or use this file in either source code
 * or executable form, except in compliance with the terms and conditions of the RPL
 *
 * All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH
 * WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language
 * governing rights and limitations under the RPL.
 *
 * http://opensource.org/licenses/RPL-1.5
 *
 * Copyright 2012-2015 Open Justice Broker Consortium
 */
package org.ojbc.adapters.analyticaldatastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.junit4.CamelSpringJUnit4ClassRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.headers.Header;
import org.apache.cxf.message.MessageImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ojbc.adapters.analyticaldatastore.dao.AnalyticalDatastoreDAOImpl;
import org.ojbc.adapters.analyticaldatastore.dao.model.Arrest;
import org.ojbc.adapters.analyticaldatastore.dao.model.AssessedNeed;
import org.ojbc.adapters.analyticaldatastore.dao.model.Charge;
import org.ojbc.adapters.analyticaldatastore.dao.model.Incident;
import org.ojbc.adapters.analyticaldatastore.dao.model.Person;
import org.ojbc.adapters.analyticaldatastore.dao.model.PretrialServiceParticipation;
import org.ojbc.adapters.analyticaldatastore.processor.AbstractReportRepositoryProcessor;
import org.ojbc.util.camel.helper.OJBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:META-INF/spring/camel-context.xml",
        "classpath:META-INF/spring/cxf-endpoints.xml",      
        "classpath:META-INF/spring/properties-context.xml",
        "classpath:META-INF/spring/dao.xml",
        })
@DirtiesContext
public class CamelContextTest {
	
	private static final Log log = LogFactory.getLog( CamelContextTest.class );
	
    @Resource
    private ModelCamelContext context;
    
    @Produce
    protected ProducerTemplate template;
    
	@Autowired
	private AnalyticalDatastoreDAOImpl analyticalDatastoreDAOImpl;
	
	@EndpointInject(uri = "mock:direct:failedInvocation")
    protected MockEndpoint failedInvocationEndpoint;
    
	private static final DateFormat DATE_FOMRAT = new SimpleDateFormat("MM/dd/yyyy");
	
	@Test
	public void contextStartup() {
		assertTrue(true);
	}

	@Before
	public void setUp() throws Exception {
		
    	//We replace the 'from' web service endpoint with a direct endpoint we call in our test
    	context.getRouteDefinition("Incident_Repository_Service").adviceWith(context, new AdviceWithRouteBuilder() {
    	    @Override
    	    public void configure() throws Exception {
    	    	// The line below allows us to bypass CXF and send a message directly into the route
    	    	replaceFromWith("direct:incidentReportingServiceEndpoint");
    	    }              
    	});

    	context.getRouteDefinition("Incident_Repository_Service_Process_Incident").adviceWith(context, new AdviceWithRouteBuilder() {
    	    @Override
    	    public void configure() throws Exception {
    	    	//This assists testing an invocation failure
    	    	interceptSendToEndpoint("direct:failedInvocation").to("mock:direct:failedInvocation").stop();
    	    }              
    	});
    	
    	
    	//We replace the 'from' web service endpoint with a direct endpoint we call in our test
    	context.getRouteDefinition("Pretrial_Service_Enrollment_Reporting_Service_Route").adviceWith(context, new AdviceWithRouteBuilder() {
    	    @Override
    	    public void configure() throws Exception {
    	    	// The line below allows us to bypass CXF and send a message directly into the route
    	    	replaceFromWith("direct:pretrialEnrollmentReportingServiceEndpoint");
    	    }              
    	});

    	context.getRouteDefinition("Disposition_Reporting_Service_Route").adviceWith(context, new AdviceWithRouteBuilder() {
    	    @Override
    	    public void configure() throws Exception {
    	    	// The line below allows us to bypass CXF and send a message directly into the route
    	    	replaceFromWith("direct:dispositionReportingServiceEndpoint");
    	    }              
    	});

    	context.start();
	}	
	
	@Test
	public void testDispositionReportService() throws Exception
	{
    	Exchange dispostionReportExchange = createSenderExchange("src/test/resources/xmlInstances/disposition/Disposition_Report.xml");
	    
	    //Send the one-way exchange.  Using template.send will send an one way message
		Exchange returnExchange = template.send("direct:dispositionReportingServiceEndpoint", dispostionReportExchange);
		
		//Use getException to see if we received an exception
		if (returnExchange.getException() != null)
		{	
			throw new Exception(returnExchange.getException());
		}	

		//Sleep while a response is generated
		Thread.sleep(3000);
	}	
	
	@Test
	public void testIncidentReportService() throws Exception
	{
    	Exchange incidentReportExchange = createSenderExchange("src/test/resources/xmlInstances/incidentReport/incidentReportWithArrest.xml");
	    
	    //Send the one-way exchange.  Using template.send will send an one way message
		Exchange returnExchange = template.send("direct:incidentReportingServiceEndpoint", incidentReportExchange);
		
		//Use getException to see if we received an exception
		if (returnExchange.getException() != null)
		{	
			throw new Exception(returnExchange.getException());
		}	

		//Sleep while a response is generated
		Thread.sleep(3000);
	    
		List<Incident> incidents = analyticalDatastoreDAOImpl.searchForIncidentsByIncidentNumber("15999999999");
		assertEquals(1,incidents.size());
		
		Incident incident = incidents.get(0);
		
		assertEquals("15999999999",incident.getIncidentCaseNumber());
		assertEquals("45 VT ROUTE 100",incident.getIncidentLocationStreetAddress());
		assertEquals("Dover",incident.getIncidentLocationTown());
		assertEquals("13:46:43",incident.getIncidentTime().toString());
		assertEquals("05/23/2015",DATE_FOMRAT.format(incident.getIncidentDate()));
		assertEquals(2,incident.getReportingAgencyID());
		assertEquals(1,incident.getIncidentTypeID());
		assertEquals(0,incident.getIncidentLocationLatitude().compareTo(new BigDecimal("42.931071")));
		assertEquals(0,incident.getIncidentLocationLongitude().compareTo(new BigDecimal("-72.847988")));
		
		int incidentPk = incident.getIncidentID();
		
		log.info("PK of incident that was just saved: " + incidentPk);
		
		List<Arrest> arrestsInIncident = analyticalDatastoreDAOImpl.searchForArrestsByIncidentPk(incidentPk);
		
		assertEquals(1,arrestsInIncident.size());
		
		Arrest arrest = arrestsInIncident.get(0);
		
		assertEquals('N',arrest.getArrestDrugRelated());
		assertEquals("13:48:00",arrest.getArrestTime().toString());
		assertEquals(2,arrest.getArrestingAgencyID());
		assertEquals(null,arrest.getInvolvedDrugID());
		
		List<Charge> charges = analyticalDatastoreDAOImpl.returnChargesFromArrest(arrest.getArrestID());
		
		assertEquals(1,charges.size());
		assertEquals(1,charges.get(0).getArrestOffenseTypeID());
		
		//TODO: Determine whether the arrest row mapper should use ResultsSetExtractor.
		//This would allow for run join queries, however, the arrest row mapper is not used
		//in the actual running code.  It would only be used in test and might not be worth
		//the effort unless a use case is defined.
	}
	
	@Test
	public void testIncidentReportServiceFailure() throws Exception
	{
		failedInvocationEndpoint.reset();
		
		failedInvocationEndpoint.expectedMessageCount(1);
		
    	Exchange incidentReportExchange = createSenderExchange("src/test/resources/xmlInstances/incidentReport/incidentReportNoCaseNumberFail.xml");
	    
	    //Send the one-way exchange.  Using template.send will send an one way message
		Exchange returnExchange = template.send("direct:incidentReportingServiceEndpoint", incidentReportExchange);
		
		//Use getException to see if we received an exception
		if (returnExchange.getException() != null)
		{	
			throw new Exception(returnExchange.getException());
		}	

		//Sleep while a response is generated
		Thread.sleep(3000);

		failedInvocationEndpoint.assertIsSatisfied();
	}	

	@Test
	public void testPretrialServiceEnrollmentReportService() throws Exception
	{
    	Exchange pretrialEnrollmentReportExchange = createSenderExchange(
    			"src/test/resources/xmlInstances/pretrialServicesEnrollmentReport/pretrial_services_enrollment_report.xml");
	    
	    //Send the one-way exchange.  Using template.send will send an one way message
		Exchange returnExchange = template.send("direct:processPretrialServiceReport", pretrialEnrollmentReportExchange);
		
		//Use getException to see if we received an exception
		if (returnExchange.getException() != null)
		{	
			throw new Exception(returnExchange.getException());
		}	

		//Sleep while a response is generated
		Thread.sleep(3000);
		
		PretrialServiceParticipation pretrialServiceParticipation = 
				analyticalDatastoreDAOImpl.getPretrialServiceParticipationByIncidentNumber("Incident4557");
		
		assertNotNull(pretrialServiceParticipation);
		assertEquals("Incident4557", pretrialServiceParticipation.getArrestIncidentCaseNumber());
		assertTrue(DateUtils.isSameDay(AbstractReportRepositoryProcessor.DATE_TIME_FORMAT.parse("2001-12-17T09:30:47"), pretrialServiceParticipation.getIntakeDate())); 
		assertEquals("ORI", pretrialServiceParticipation.getArrestingAgencyORI());
		assertEquals(Integer.valueOf(1), pretrialServiceParticipation.getCountyID());
		
		List<AssessedNeed> associatedNeeds = analyticalDatastoreDAOImpl.getAssociatedNeeds(pretrialServiceParticipation.getPretrialServiceParticipationID());
		assertEquals(3, associatedNeeds.size()); 
		
		Person person = analyticalDatastoreDAOImpl.getPerson(pretrialServiceParticipation.getPersonID()); 
		assertNotNull(person); 
		log.info("Person: " + person.toString());
		assertEquals("Person[personID=3,personSexID=1,personRaceID=5,personSexDescription=M,"
				+ "personRaceDescription=W,personBirthDate=2001-12-17,",
				StringUtils.substringBefore(person.toString(), "personUniqueIdentifier"));
		
	}	
	
	protected Exchange createSenderExchange(String pathToInputFile) throws Exception, IOException {
		//Create a new exchange
    	Exchange incidentReportExchange = new DefaultExchange(context);

    	//Set the WS-Address Message ID
		Map<String, Object> requestContext = OJBUtils.setWSAddressingMessageID("123456789");
		
		//Set the operation name and operation namespace for the CXF exchange
		incidentReportExchange.getIn().setHeader(Client.REQUEST_CONTEXT , requestContext);
		
		Document doc = createDocument();
		List<SoapHeader> soapHeaders = new ArrayList<SoapHeader>();
		soapHeaders.add(makeSoapHeader(doc, "http://www.w3.org/2005/08/addressing", "MessageID", "12345"));
		incidentReportExchange.getIn().setHeader(Header.HEADER_LIST , soapHeaders);
		
		org.apache.cxf.message.Message message = new MessageImpl();

		incidentReportExchange.getIn().setHeader(CxfConstants.CAMEL_CXF_MESSAGE, message);
    	
	    //Read the firearm search request file from the file system
	    File inputFile = new File(pathToInputFile);
	    String inputStr = FileUtils.readFileToString(inputFile);

	    assertNotNull(inputStr);
	    
	    //Set it as the message message body
	    incidentReportExchange.getIn().setBody(inputStr);
		return incidentReportExchange;
	}
	
	private SoapHeader makeSoapHeader(Document doc, String namespace, String localName, String value) {
		Element messageId = doc.createElementNS(namespace, localName);
		messageId.setTextContent(value);
		SoapHeader soapHeader = new SoapHeader(new QName(namespace, localName), messageId);
		return soapHeader;
	}	
	
	public static Document createDocument() throws Exception{

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().newDocument();

		return doc;
	}
}
