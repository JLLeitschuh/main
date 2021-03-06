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
package org.ojbc.intermediaries.sn.dao;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.ojbc.intermediaries.sn.subscription.SubscriptionRequest;
import org.ojbc.util.model.rapback.Subscription;

/**
 * Interface for objects that compute a validation due date from a subscription
 */
public interface ValidationDueDateStrategy {
    
    /**
     * Given a subscription request object, figure out what the validation due date is.
     * This method should be used for add or modifying subscriptions
     * @param subscription the subscription
     * @return the date on which the subscription must be validated
     * @throws Exception
     */
    public DateTime getValidationDueDate(SubscriptionRequest request, LocalDate validationDate);
    
    /**
     * This method should be used when a subscription is retrieved from the database and we are validating it
     * 
     * @param subscription
     * @param validationDate
     * @return
     */
    public DateTime getValidationDueDate(Subscription subscription, LocalDate validationDate);

}
