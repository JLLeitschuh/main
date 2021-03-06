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
 * Copyright 2012-2017 Open Justice Broker Consortium
 */
package org.ojbc.intermediaries.sn.topic.courtdispositionupdate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.camel.Message;
import org.junit.Test;
import org.mockito.Mockito;
import org.ojbc.intermediaries.sn.SubscriptionNotificationConstants;
import org.ojbc.util.xml.XmlUtils;
import org.w3c.dom.Document;

public class CourtDispositionUpdateSubscriptionRequestTest {
		
	@Test
	public void testCourtDispositionUpdateSubscriptionRequest() throws Exception {
		
		Message message = Mockito.mock(Message.class);
		
		Mockito.when(message.getBody(Document.class)).thenReturn(getMessageBody());
		
		CourtDispositionUpdateSubscriptionRequest subscription = new CourtDispositionUpdateSubscriptionRequest(message, null);		
		
		assertThat(subscription.getSubscriptionQualifier(), is("302593"));
		
		assertThat(subscription.getSubjectName(), is("John Doe"));
		
		assertThat(subscription.getEmailAddresses().size(), is(1));
		assertThat(subscription.getEmailAddresses().contains("po6@localhost"), is(true));
		
		assertThat(subscription.getSubjectIdentifiers().size(), is(5));
		assertThat(subscription.getSubjectIdentifiers().get(SubscriptionNotificationConstants.FIRST_NAME), is("John"));
		assertThat(subscription.getSubjectIdentifiers().get(SubscriptionNotificationConstants.LAST_NAME), is("Doe"));
		assertThat(subscription.getSubjectIdentifiers().get(SubscriptionNotificationConstants.DATE_OF_BIRTH), is("1990-10-20"));
		assertThat(subscription.getSubjectIdentifiers().get(SubscriptionNotificationConstants.SUBSCRIPTION_QUALIFIER), is("302593"));
	}
	
	private Document getMessageBody() throws Exception {
		return XmlUtils.parseFileToDocument(new File("src/test/resources/xmlInstances/courtDispositionUpdate/subscribeSoapRequest-courtDispositionUpdate.xml"));	
	}

}
