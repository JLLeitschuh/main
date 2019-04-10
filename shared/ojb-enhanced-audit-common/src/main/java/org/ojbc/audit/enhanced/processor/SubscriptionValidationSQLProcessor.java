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
package org.ojbc.audit.enhanced.processor;

import org.apache.camel.Exchange;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ojbc.audit.enhanced.dao.EnhancedAuditDAO;
import org.ojbc.audit.enhanced.dao.model.ValidationRequest;

public class SubscriptionValidationSQLProcessor extends AbstractValidationRequestProcessor {

	private static final Log log = LogFactory.getLog(SubscriptionValidationSQLProcessor.class);
	
	private EnhancedAuditDAO enhancedAuditDAO;
	
	private UserInfoSQLProcessor userInfoProcessor;
	
	@Override
	public void auditValidationRequest(Exchange exchange) {
		
		try {
			Integer userInfoPk = userInfoProcessor.auditUserInfo(exchange);
			
			String subscriptionId = (String) exchange.getIn().getHeader("subscriptionId");
			String validationDueDateString = (String) exchange.getIn().getHeader("validationDueDateString");
			
			ValidationRequest validationRequest = processValidationRequest(subscriptionId, validationDueDateString);
			validationRequest.setUserInfoFK(userInfoPk);
			
			log.info(validationRequest.toString());
			
			enhancedAuditDAO.saveValidationRequest(validationRequest);
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Unable to audit subscription validation request: " + ExceptionUtils.getStackTrace(e));
		}
		
	}

	public EnhancedAuditDAO getEnhancedAuditDAO() {
		return enhancedAuditDAO;
	}

	public void setEnhancedAuditDAO(EnhancedAuditDAO enhancedAuditDAO) {
		this.enhancedAuditDAO = enhancedAuditDAO;
	}

	public UserInfoSQLProcessor getUserInfoProcessor() {
		return userInfoProcessor;
	}

	public void setUserInfoProcessor(UserInfoSQLProcessor userInfoProcessor) {
		this.userInfoProcessor = userInfoProcessor;
	}

}