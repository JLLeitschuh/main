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
package org.ojbc.adapters.analyticaldatastore.processor;

import java.math.BigDecimal;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ojbc.adapters.analyticaldatastore.dao.AnalyticalDatastoreDAO;
import org.ojbc.adapters.analyticaldatastore.dao.AnalyticalDatastoreDAOImpl;
import org.ojbc.adapters.analyticaldatastore.dao.model.Arrest;
import org.ojbc.adapters.analyticaldatastore.dao.model.Incident;
import org.ojbc.adapters.analyticaldatastore.dao.model.Person;
import org.ojbc.adapters.analyticaldatastore.personid.IdentifierGenerationStrategy;
import org.ojbc.adapters.analyticaldatastore.util.AnalyticalDataStoreUtils;
import org.ojbc.util.xml.XmlUtils;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IncidentReportProcessor {

	private static final Log log = LogFactory.getLog( IncidentReportProcessor.class );
	
	private static final String PATH_TO_LEXS_DATA_ITEM_PACKAGE="/ir:IncidentReport/lexspd:doPublish/lexs:PublishMessageContainer/lexs:PublishMessage/lexs:DataItemPackage";
	
	private static final String PATH_TO_LEXS_DIGEST= PATH_TO_LEXS_DATA_ITEM_PACKAGE + "/lexs:Digest";
	
	private IdentifierGenerationStrategy identifierGenerationStrategy;
	
	private AnalyticalDatastoreDAO analyticalDatastoreDAO;
	
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	@Transactional
	public void processIncidentReport(Document incidentReport) throws Exception
	{
		Incident incident = new Incident();
		
		String reportingAgencyName = XmlUtils.xPathStringSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:EntityOrganization/nc:Organization[@s:id= " + PATH_TO_LEXS_DIGEST + " /lexsdigest:Associations/nc:ActivityReportingOrganizationAssociation[nc:ActivityReference/@s:ref=" + PATH_TO_LEXS_DIGEST + "/lexsdigest:EntityActivity/nc:Activity[nc:ActivityCategoryText='Incident']/@s:id]/nc:OrganizationReference/@s:ref]/nc:OrganizationName");
		log.debug("Agency Name: " + reportingAgencyName);
		
		if (StringUtils.isNotBlank(reportingAgencyName))
		{
			int reportingAgencyId = analyticalDatastoreDAO.returnAgencyKeyfromAgencyName(reportingAgencyName);
			incident.setReportingAgencyID(reportingAgencyId);
		}	
		
		String incidentDateTimeAsString = XmlUtils.xPathStringSearch(incidentReport, PATH_TO_LEXS_DIGEST+ "/lexsdigest:EntityActivity/nc:Activity/nc:ActivityDateRange/nc:StartDate/nc:DateTime");
		log.debug("Incident Date: " + incidentDateTimeAsString);
		
		Calendar incidentDateTimeCal = DatatypeConverter.parseDateTime(incidentDateTimeAsString);
		Date incidentDateTime = incidentDateTimeCal.getTime();

		if (incidentDateTime != null)
		{
			incident.setIncidentDate(incidentDateTime);
		}	
		
		Time incidentTime =  new Time(incidentDateTime.getTime());
		log.debug("Incident Time: " + incidentTime.toString());
		
		if (incidentTime != null)
		{
			incident.setIncidentTime(incidentTime);
		}	
		
		String mapHorizontalCoordinateText =XmlUtils.xPathStringSearch(incidentReport, PATH_TO_LEXS_DATA_ITEM_PACKAGE + "/lexs:StructuredPayload/inc-ext:IncidentReport/inc-ext:Location/nc:LocationMapLocation/nc:MapHorizontalCoordinateText");
		log.debug("Map horizontal coordinate text: " + mapHorizontalCoordinateText);
		
		if (StringUtils.isNotBlank(mapHorizontalCoordinateText))
		{
			BigDecimal latitute = new BigDecimal(mapHorizontalCoordinateText).setScale(4);
			incident.setIncidentLocationLatitude(latitute);
		}	
		
		String mapVerticalCoordinateText =XmlUtils.xPathStringSearch(incidentReport, PATH_TO_LEXS_DATA_ITEM_PACKAGE + "/lexs:StructuredPayload/inc-ext:IncidentReport/inc-ext:Location/nc:LocationMapLocation/nc:MapVerticalCoordinateText");
		log.debug("Map vertical coordinate text: " + mapVerticalCoordinateText);
		
		if (StringUtils.isNotBlank(mapVerticalCoordinateText))
		{
			BigDecimal longitude = new BigDecimal(mapVerticalCoordinateText).setScale(4);
			incident.setIncidentLocationLongitude(longitude);
		}	
		
		
		String incidentCaseNumber=XmlUtils.xPathStringSearch(incidentReport,  PATH_TO_LEXS_DATA_ITEM_PACKAGE + "/lexs:PackageMetadata/lexs:DataItemID");
		log.debug("Incident Case Number: " + incidentCaseNumber);
		
		if (StringUtils.isNotBlank(incidentCaseNumber))
		{
			incident.setIncidentCaseNumber(incidentCaseNumber);
		}	
		
		String incidentLocationReference = XmlUtils.xPathStringSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:Associations/lexsdigest:IncidentLocationAssociation/nc:LocationReference/@s:ref");
		
		if (StringUtils.isNotEmpty(incidentLocationReference))
		{
			Node locationNode = XmlUtils.xPathNodeSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:EntityLocation/nc:Location[@s:id='" + incidentLocationReference + "']");
			
			String streetFullText = XmlUtils.xPathStringSearch(locationNode, "nc:LocationAddress/nc:StructuredAddress/nc:LocationStreet/nc:StreetFullText");
			log.debug("Street Full Text: " + streetFullText);

			if (StringUtils.isNotBlank(streetFullText))
			{
				incident.setIncidentLocationStreetAddress(streetFullText);
			}	
			else
			{
				String streetNumberText = XmlUtils.xPathStringSearch(locationNode, "nc:LocationAddress/nc:StructuredAddress/nc:LocationStreet/nc:StreetNumberText");
				log.debug("Street Number Text: " + streetNumberText);
				
				String streetName = XmlUtils.xPathStringSearch(locationNode, "nc:LocationAddress/nc:StructuredAddress/nc:LocationStreet/nc:StreetName");
				log.debug("Street Name Text: " + streetName);

				String streetCategoryText = XmlUtils.xPathStringSearch(locationNode, "nc:LocationAddress/nc:StructuredAddress/nc:LocationStreet/nc:StreetCategoryText");
				log.debug("Street Category Text: " + streetCategoryText);

				streetFullText = streetNumberText + streetName + streetFullText;
				log.debug("Street Full Text built from number, name and category: " + streetFullText);

				if (StringUtils.isNotBlank(streetFullText))
				{
					incident.setIncidentLocationStreetAddress(streetFullText);
				}	
	
			}	
			
			String cityTown = XmlUtils.xPathStringSearch(locationNode, "nc:LocationAddress/nc:StructuredAddress/nc:LocationCityName");
			log.debug("City/Town: " + cityTown);
			
			if (StringUtils.isNotBlank(cityTown))
			{
				incident.setIncidentLocationTown(cityTown);
			}	

		}	
		
		String incidentTypeDescription="Placeholder";
		
		int incidentTypePK = analyticalDatastoreDAO.returnIncidentTypeKeyfromIncidentTypeDescription(incidentTypeDescription);
		incident.setIncidentTypeID(incidentTypePK);
		
		int incidentPk = analyticalDatastoreDAO.saveIncident(incident);
		
		processArrests(incidentReport, incidentPk);
			
	}

	protected void processArrests(Document incidentReport, int incidentPk)
			throws Exception {
		NodeList arrestSubjectAssocationNodes = XmlUtils.xPathNodeListSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:Associations/lexsdigest:ArrestSubjectAssociation");
		
	    if (arrestSubjectAssocationNodes == null || arrestSubjectAssocationNodes.getLength() == 0) 
	    {
			log.debug("No Arrest nodes in document");
			return;
	    }
		
		for (int i = 0; i < arrestSubjectAssocationNodes.getLength(); i++) 
		{
			Arrest arrest = new Arrest();
			
			arrest.setIncidentID(incidentPk);
			
		    if (arrestSubjectAssocationNodes.item(i).getNodeType() == Node.ELEMENT_NODE)
		    {
		        String personId = XmlUtils.xPathStringSearch(arrestSubjectAssocationNodes.item(i), "nc:PersonReference/@s:ref");
		        log.debug("Arrestee Person ID: " + personId);
		        
		        Node personNode = XmlUtils.xPathNodeSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:EntityPerson/lexsdigest:Person[@s:id='" + personId + "']");
		        
		        Map<String, Object> arrestee = AnalyticalDataStoreUtils.retrieveMapOfPersonAttributes(personNode);
		        String personIdentifierKey = identifierGenerationStrategy.generateIdentifier(arrestee);
		        log.debug("Arrestee person identifier keys: " + personIdentifierKey);

		        String personBirthDateAsString = (String)arrestee.get("personDateOfBirth");
		        String personRace = (String)arrestee.get("personRace");
		        String personSex = (String)arrestee.get("personSex");
		        
		        int personPk = savePerson(personBirthDateAsString, personSex, personRace, personIdentifierKey);	
		        arrest.setPersonID(personPk);
		        
		        Date arrestDateTime = returnArrestDate(incidentReport,
		        		arrestSubjectAssocationNodes.item(i));
		        
		        arrest.setArrestDate(arrestDateTime);

				Time arrestTime =  new Time(arrestDateTime.getTime());
				log.debug("Arrest Time: " + arrestTime.toString());

				arrest.setArrestTime(arrestTime);
				
				String arrestingAgency = returnArrestingAgency(incidentReport);
				log.debug("Arresting Agency: " + arrestingAgency);

				if (StringUtils.isNotBlank(arrestingAgency))
				{
					int arrestingAgencyId = analyticalDatastoreDAO.returnAgencyKeyfromAgencyName(arrestingAgency);
					arrest.setArrestingAgencyID(arrestingAgencyId);
				}	

				arrest.setArrestDrugRelated('N');
				
				//Save arrest
		        analyticalDatastoreDAO.saveArrest(arrest);
		        
		        //Save Charges
		        //lexsdigest:ArrestOffenseAssociation
		        NodeList arrestOffenseNodes = XmlUtils.xPathNodeListSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:Associations/lexsdigest:ArrestOffenseAssociation");
		        
		        processArrestOffenseNodes(arrestOffenseNodes);
		    }
		}
	}

	private void processArrestOffenseNodes(NodeList arrestOffenseNodes) throws Exception{
		for (int i = 0; i < arrestOffenseNodes.getLength(); i++) 
		{
			Node arrestOffenseNode = (arrestOffenseNodes.item(i));
			
		    if (arrestOffenseNode.getNodeType() == Node.ELEMENT_NODE)
		    {
		    	String offenseReference = XmlUtils.xPathStringSearch(arrestOffenseNode, "nc:ActivityReference[2]/@s:ref");
		    	log.debug("Arrest offense reference: " + offenseReference);
		    	
		    	//TODO: determine whether to go after ndexia:Offense or OJB ext:Offense
		    }
		}	    
		
	}

	protected Date returnArrestDate(Document incidentReport,
			Node arrestSubjectAssocationNode) throws Exception {
		String arrestActivityReference = XmlUtils.xPathStringSearch(arrestSubjectAssocationNode, "nc:ActivityReference/@s:ref");
		log.debug("Arrest Activity Reference: " + arrestActivityReference);
		
		Node arrestNode = XmlUtils.xPathNodeSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:EntityActivity/nc:Activity[@s:id='" + arrestActivityReference + "']");
		
		String arrestDateTimeAsString = XmlUtils.xPathStringSearch(arrestNode, "nc:ActivityDate/nc:DateTime");
		
		Calendar arrestDateTimeCal = DatatypeConverter.parseDateTime(arrestDateTimeAsString);
		Date arrestDateTime = arrestDateTimeCal.getTime();
		return arrestDateTime;
	}

	protected int savePerson(String personBirthDateAsString, String personRace, String personSex,
			String personIdentifierKey) throws Exception {
		//Save person
		Person person = new Person();
		
		person.setPersonUniqueIdentifier(personIdentifierKey);
		
		if (StringUtils.isNotEmpty(personBirthDateAsString))
		{
			Date personBirthDate = DATE_FORMAT.parse(personBirthDateAsString);
			person.setPersonBirthDate(personBirthDate);
		}	
		
		//Get Person Race from code table
		int personRacePk = analyticalDatastoreDAO.returnPersonRaceKeyfromRaceDescription(personRace);
		person.setPersonRaceID(personRacePk);
		//Get Person Sex from code table
		int personSexPk = analyticalDatastoreDAO.returnPersonSexKeyfromSexDescription(personSex);
		person.setPersonSexID(personSexPk);
		
		int personPk = analyticalDatastoreDAO.savePerson(person);
		return personPk;
	}

	private String returnArrestingAgency(Node incidentReport) throws Exception
	{
		String arrestingOfficerRefernce = XmlUtils.xPathStringSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:Associations/lexsdigest:ArrestOfficerAssociation/nc:PersonReference/@s:ref");
		log.debug("Arresting Officer Reference: " + arrestingOfficerRefernce);
		
		String arrestAgencyReference=XmlUtils.xPathStringSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:Associations/nc:PersonAssignedUnitAssociation/nc:PersonReference[@s:ref='" + arrestingOfficerRefernce + "']/following-sibling::nc:OrganizationReference/@s:ref");
		log.debug("Arresting Agency Reference: " + arrestAgencyReference);
		
		String arrestingAgency = XmlUtils.xPathStringSearch(incidentReport, PATH_TO_LEXS_DIGEST + "/lexsdigest:EntityOrganization/nc:Organization[@s:id='" + arrestAgencyReference + "']/nc:OrganizationName");
		log.debug("Arresting Agency: " + arrestingAgency);
		
		return arrestingAgency;
	}
	
	public IdentifierGenerationStrategy getIdentifierGenerationStrategy() {
		return identifierGenerationStrategy;
	}

	public void setIdentifierGenerationStrategy(
			IdentifierGenerationStrategy identifierGenerationStrategy) {
		this.identifierGenerationStrategy = identifierGenerationStrategy;
	}

	public AnalyticalDatastoreDAO getAnalyticalDatastoreDAO() {
		return analyticalDatastoreDAO;
	}

	public void setAnalyticalDatastoreDAO(
			AnalyticalDatastoreDAO analyticalDatastoreDAO) {
		this.analyticalDatastoreDAO = analyticalDatastoreDAO;
	}

	
}
