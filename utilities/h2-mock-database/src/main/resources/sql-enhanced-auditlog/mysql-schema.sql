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
/*
 * You need to make these changes when generating from SQL PA:
 * For timestamps, use an example like this:  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 * 
 */
drop schema if exists enhanced_auditlog;
CREATE DATABASE `enhanced_auditlog`; 
use enhanced_auditlog;


CREATE TABLE TRIGGERING_EVENTS (
                TRIGGERING_EVENTS_ID INT AUTO_INCREMENT NOT NULL,
                TRIGGERING_EVENT VARCHAR(100) NOT NULL,
                PRIMARY KEY (TRIGGERING_EVENTS_ID)
);


CREATE TABLE FEDERAL_RAPBACK_NOTIFICATION (
                FEDERAL_RAPBACK_NOTIFICATION_ID INT AUTO_INCREMENT NOT NULL,
                PATH_TO_NOTIFICATION_FILE VARCHAR(200),
                STATE_SUBSCRIPTION_ID VARCHAR(20),
                RAPBACK_EVENT_TEXT VARCHAR(200),
                ORIGINAL_IDENTIFIER VARCHAR(20),
                UPDATED_IDENTIFIER VARCHAR(20),
                TRANSACTION_TYPE VARCHAR(50),
                NOTIFICATION_RECIEVED_TIMESTAMP DATETIME NOT NULL,
                PRIMARY KEY (FEDERAL_RAPBACK_NOTIFICATION_ID)
);


CREATE TABLE FEDERAL_RAPBACK_SUBSCRIPTION_ERRORS (
                FEDERAL_RAPBACK_SUBSCRIPTION_ERRORS_ID INT AUTO_INCREMENT NOT NULL,
                FEDERAL_RAPBACK_SUBSCRIPTION_ID INT NOT NULL,
                STATE_SUBSCRIPTION_ID VARCHAR(20),
                ERROR_REPORTED BOOLEAN,
                ERROR_RESOLVED BOOLEAN,
                PRIMARY KEY (FEDERAL_RAPBACK_SUBSCRIPTION_ERRORS_ID)
);


CREATE TABLE TRIGGERING_EVENTS_JOINER (
                TRIGGERING_EVENTS_JOINER_ID INT AUTO_INCREMENT NOT NULL,
                FEDERAL_RAPBACK_NOTIFICATION_ID INT NOT NULL,
                TRIGGERING_EVENTS_ID INT NOT NULL,
                PRIMARY KEY (TRIGGERING_EVENTS_JOINER_ID)
);


CREATE TABLE PRINT_RESULTS (
                PRINT_RESULTS_ID INT AUTO_INCREMENT NOT NULL,
                SYSTEM_NAME VARCHAR(200),
                DESCRIPTION VARCHAR(200),
                MESSAGE_ID VARCHAR(50) NOT NULL,
                TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
                PRIMARY KEY (PRINT_RESULTS_ID)
);


CREATE TABLE IDENTIFICATION_SEARCH_REASON_CODE (
                IDENTIFICATION_SEARCH_REASON_CODE_ID INT AUTO_INCREMENT NOT NULL,
                IDENTIFICATION_REASON_CODE_DESCRIPTION VARCHAR(80) NOT NULL,
                PRIMARY KEY (IDENTIFICATION_SEARCH_REASON_CODE_ID)
);


CREATE TABLE FEDERAL_RAPBACK_SUBSCRIPTION (
                FEDERAL_RAPBACK_SUBSCRIPTION_ID INT AUTO_INCREMENT NOT NULL,
                TRANSACTION_CONTROL_REFERENCE_IDENTIFICATION VARCHAR(100),
                TRANSACTION_CATEGORY_CODE_REQUEST VARCHAR(20),
                TRANSACTION_CATEGORY_CODE_RESPONSE VARCHAR(20),
                PATH_TO_REQUEST_FILE VARCHAR(200),
                PATH_TO_RESPONSE_FILE VARCHAR(200),
                SUBSCRIPTION_CATEGORY_CODE VARCHAR(20),
                STATE_SUBSCRIPTION_ID VARCHAR(20) NOT NULL,
                FBI_SUBSCRIPTION_ID VARCHAR(20),
                SID VARCHAR(20) NOT NULL,
                TRANSACTION_STATUS_TEXT VARCHAR(200),
                REQUEST_SENT_TIMESTAMP DATETIME,
                RESPONSE_RECIEVED_TIMESTAMP DATETIME,
                PRIMARY KEY (FEDERAL_RAPBACK_SUBSCRIPTION_ID)
);


CREATE TABLE SYSTEMS_TO_SEARCH (
                SYSTEMS_TO_SEARCH_ID INT AUTO_INCREMENT NOT NULL,
                SYSTEM_NAME VARCHAR(80) NOT NULL,
                SYSTEM_URI VARCHAR(200) NOT NULL,
                PRIMARY KEY (SYSTEMS_TO_SEARCH_ID)
);


CREATE TABLE USER_INFO (
                USER_INFO_ID INT AUTO_INCREMENT NOT NULL,
                FEDERATION_ID VARCHAR(100),
                EMPLOYER_NAME VARCHAR(100),
                EMPLOYER_SUBUNIT_NAME VARCHAR(100),
                USER_FIRST_NAME VARCHAR(100),
                USER_LAST_NAME VARCHAR(100),
                USER_EMAIL_ADDRESS VARCHAR(100),
                IDENTITY_PROVIDER_ID VARCHAR(100),
                PRIMARY KEY (USER_INFO_ID)
);


CREATE TABLE USER_LOGIN (
                USER_LOGIN_ID INT AUTO_INCREMENT NOT NULL,
                USER_INFO_ID INT NOT NULL,
                ACTION VARCHAR(30) NOT NULL,
                `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (USER_LOGIN_ID)
);


CREATE TABLE IDENTIFICATION_SEARCH_REQUEST (
                IDENTIFICATION_SEARCH_REQUEST_ID INT AUTO_INCREMENT NOT NULL,
                FIRST_NAME VARCHAR(80),
                LAST_NAME VARCHAR(80),
                IDENTIFICATION_RESULTS_STATUS VARCHAR(80),
                REPORTED_FROM_DATE DATE,
                REPORTED_TO_DATE DATE,
                OTN VARCHAR(80),
                MESSAGE_ID VARCHAR(50) NOT NULL,
                USER_INFO_ID INT,
                `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (IDENTIFICATION_SEARCH_REQUEST_ID)
);


CREATE TABLE IDENTIFICATION_REASON_CODE_JOINER (
                IDENTIFICATION_REASON_CODE_JOINER_ID INT AUTO_INCREMENT NOT NULL,
                IDENTIFICATION_SEARCH_REASON_CODE_ID INT NOT NULL,
                IDENTIFICATION_SEARCH_REQUEST_ID INT NOT NULL,
                PRIMARY KEY (IDENTIFICATION_REASON_CODE_JOINER_ID)
);


CREATE TABLE IDENTIFICATION_SEARCH_RESULTS (
                IDENTIFICATION_SEARCH_RESULTS_ID INT AUTO_INCREMENT NOT NULL,
                IDENTIFICATION_SEARCH_REQUEST_ID INT NOT NULL,
                AVAILABLE_RESULTS INT NOT NULL,
                MESSAGE_ID VARCHAR(50) NOT NULL,
				`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (IDENTIFICATION_SEARCH_RESULTS_ID)
);


CREATE TABLE QUERY_REQUEST (
                QUERY_REQUEST_ID INT AUTO_INCREMENT NOT NULL,
                IDENTIFICATION_ID VARCHAR(80) NOT NULL,
                SYSTEM_NAME VARCHAR(200) NOT NULL,
                MESSAGE_ID VARCHAR(50) NOT NULL,
                USER_INFO_ID INT,
                `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (QUERY_REQUEST_ID)
);


CREATE TABLE FIREARMS_QUERY_RESULTS (
                FIREARMS_QUERY_RESULTS_ID INT AUTO_INCREMENT NOT NULL,
                QUERY_REQUEST_ID INT NOT NULL,
                SYSTEM_NAME VARCHAR(80) NOT NULL,
                QUERY_RESULTS_ERROR_INDICATOR BOOLEAN,
                QUERY_RESULTS_ERROR_TEXT VARCHAR(300),
                MESSAGE_ID VARCHAR(50) NOT NULL,
                QUERY_RESULTS_TIMEOUT_INDICATOR BOOLEAN,
                TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP() NOT NULL,
                PRIMARY KEY (FIREARMS_QUERY_RESULTS_ID)
);


CREATE TABLE IDENTIFICATION_RESULTS_QUERY_DETAIL (
                IDENTIFICATION_RESULTS_QUERY_DETAIL_ID INT AUTO_INCREMENT NOT NULL,
                QUERY_REQUEST_ID INT NOT NULL,
                PERSON_LAST_NAME VARCHAR(80),
                PERSON_MIDDLE_NAME VARCHAR(80),
                PERSON_FIRST_NAME VARCHAR(80),
                SID VARCHAR(80),
                OCA VARCHAR(80),
                ID_DATE DATE,
                OTN VARCHAR(80),
                FBI_ID VARCHAR(80),
				`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (IDENTIFICATION_RESULTS_QUERY_DETAIL_ID)
);


CREATE TABLE WARRANT_QUERY_RESULTS (
                WARRANT_QUERY_RESULTS_ID INT AUTO_INCREMENT NOT NULL,
                QUERY_REQUEST_ID INT NOT NULL,
                SYSTEM_NAME VARCHAR(80) NOT NULL,
                FIRST_NAME VARCHAR(80),
                MIDDLE_NAME VARCHAR(80),
                LAST_NAME VARCHAR(80),
                FBI_ID VARCHAR(20),
                SID VARCHAR(20),
                QUERY_RESULTS_ERROR_INDICATOR BOOLEAN,
                QUERY_RESULTS_ACCESS_DENIED_INDICATOR BOOLEAN,
                QUERY_RESULTS_ERROR_TEXT VARCHAR(300),
                MESSAGE_ID VARCHAR(50) NOT NULL,
                QUERY_RESULTS_TIMEOUT_INDICATOR BOOLEAN,
                `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (WARRANT_QUERY_RESULTS_ID)
);


CREATE TABLE CRIMINAL_HISTORY_QUERY_RESULTS (
                CRIMINAL_HISTORY_QUERY_RESULTS_ID INT AUTO_INCREMENT NOT NULL,
                QUERY_REQUEST_ID INT NOT NULL,
                SYSTEM_NAME VARCHAR(80) NOT NULL,
                FIRST_NAME VARCHAR(80),
                MIDDLE_NAME VARCHAR(80),
                LAST_NAME VARCHAR(80),
                FBI_ID VARCHAR(20),
                SID VARCHAR(20),
                QUERY_RESULTS_ERROR_INDICATOR BOOLEAN,
                QUERY_RESULTS_ERROR_TEXT VARCHAR(300),
                MESSAGE_ID VARCHAR(50) NOT NULL,
                QUERY_RESULTS_TIMEOUT_INDICATOR BOOLEAN,
                `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (CRIMINAL_HISTORY_QUERY_RESULTS_ID)
);


CREATE TABLE SEARCH_QUALIFIER_CODES (
                SEARCH_QUALIFIER_CODES_ID INT AUTO_INCREMENT NOT NULL,
                CODE_NAME VARCHAR(80),
                PRIMARY KEY (SEARCH_QUALIFIER_CODES_ID)
);


CREATE TABLE PERSON_SEARCH_REQUEST (
                PERSON_SEARCH_REQUEST_ID INT AUTO_INCREMENT NOT NULL,
                FIRST_NAME VARCHAR(80),
                FIRST_NAME_QUALIFIER_CODE_ID INT,
                MIDDLE_NAME VARCHAR(80),
                LAST_NAME VARCHAR(80),
                LAST_NAME_QUALIFIER_CODE_ID INT,
                FBI_ID VARCHAR(20),
                SID VARCHAR(20),
                HEIGHT INT,
                HEIGHT_MIN INT,
                HEIGHT_MAX INT,
                SSN VARCHAR(11),
                DOB_START_DATE DATE,
                DOB_END_DATE DATE,
                DRIVERS_LICENSE_ISSUER VARCHAR(20),
                DRIVERS_LICENSE_NUMBER VARCHAR(30),
                RACE VARCHAR(30),
                GENDER VARCHAR(15),
                EYE_COLOR VARCHAR(20),
                PURPOSE VARCHAR(80),
                ON_BEHALF_OF VARCHAR(80),
                HAIR_COLOR VARCHAR(20),
                MESSAGE_ID VARCHAR(50) NOT NULL,
                USER_INFO_ID INT,
                `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (PERSON_SEARCH_REQUEST_ID)
);


CREATE TABLE PERSON_SYSTEMS_TO_SEARCH (
                PERSON_SYSTEMS_TO_SEARCH_ID INT AUTO_INCREMENT NOT NULL,
                SYSTEMS_TO_SEARCH_ID INT NOT NULL,
                PERSON_SEARCH_REQUEST_ID INT NOT NULL,
                PRIMARY KEY (PERSON_SYSTEMS_TO_SEARCH_ID)
);


CREATE TABLE PERSON_SEARCH_RESULTS (
                PERSON_SEARCH_RESULTS_ID INT AUTO_INCREMENT NOT NULL,
                PERSON_SEARCH_REQUEST_ID INT NOT NULL,
                SYSTEMS_TO_SEARCH_ID INT,
                SEARCH_RESULTS_COUNT INT,
                SEARCH_RESULTS_ERROR_INDICATOR BOOLEAN,
                SEARCH_RESULTS_ACCESS_DENIED_INDICATOR BOOLEAN,
                SEARCH_RESULTS_ERROR_TEXT VARCHAR(300),
                SEARCH_RESULTS_TIMEOUT_INDICATOR BOOLEAN,
                `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (PERSON_SEARCH_RESULTS_ID)
);


CREATE TABLE NOTIFICATIONS_SENT (
                NOTIFICATIONS_SENT_ID INT AUTO_INCREMENT NOT NULL,
                SUBSCRIPTION_TYPE VARCHAR(80) NOT NULL,
                TOPIC VARCHAR(100) NOT NULL,
                SUBSCRIPTION_IDENTIFIER INT NOT NULL,
                SUBSCRIPTION_OWNER_AGENCY VARCHAR(80),
                SUBSCRIPTION_OWNER_AGENCY_TYPE VARCHAR(80),
                SUBSCRIPTION_OWNER_EMAIL_ADDRESS VARCHAR(80),
                SUBSCRIPTION_OWNER VARCHAR(80) NOT NULL,
                NOTIFYING_SYSTEM_NAME VARCHAR(80) NOT NULL,
                SUBSCRIBING_SYSTEM_IDENTIFIER VARCHAR(80) NOT NULL,
                `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (NOTIFICATIONS_SENT_ID)
);


CREATE TABLE NOTIFICATION_MECHANISM (
                NOTIFICATION_MECHANISM_ID INT AUTO_INCREMENT NOT NULL,
                NOTIFICATIONS_SENT_ID INT NOT NULL,
                NOTIFICATION_MECHANSIM VARCHAR(80) NOT NULL,
                NOTIFICATION_ADDRESS VARCHAR(80) NOT NULL,
                NOTIFICATION_RECIPIENT_TYPE VARCHAR(10) NOT NULL,
                PRIMARY KEY (NOTIFICATION_MECHANISM_ID)
);


CREATE TABLE NOTIFICATION_PROPERTIES (
                NOTIFICATION_PROPERTIES_ID INT AUTO_INCREMENT NOT NULL,
                NOTIFICATIONS_SENT_ID INT NOT NULL,
                PROPERTY_NAME VARCHAR(100) NOT NULL,
                PROPERTY_VALUE VARCHAR(100) NOT NULL,
                PRIMARY KEY (NOTIFICATION_PROPERTIES_ID)
);


ALTER TABLE TRIGGERING_EVENTS_JOINER ADD CONSTRAINT triggering_events_triggering_events_joiner_fk
FOREIGN KEY (TRIGGERING_EVENTS_ID)
REFERENCES TRIGGERING_EVENTS (TRIGGERING_EVENTS_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TRIGGERING_EVENTS_JOINER ADD CONSTRAINT federal_rapback_notification_triggering_events_joiner_fk
FOREIGN KEY (FEDERAL_RAPBACK_NOTIFICATION_ID)
REFERENCES FEDERAL_RAPBACK_NOTIFICATION (FEDERAL_RAPBACK_NOTIFICATION_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE FEDERAL_RAPBACK_SUBSCRIPTION_ERRORS ADD CONSTRAINT frn_frn_errors_fk
FOREIGN KEY (FEDERAL_RAPBACK_SUBSCRIPTION_ID)
REFERENCES FEDERAL_RAPBACK_SUBSCRIPTION (FEDERAL_RAPBACK_SUBSCRIPTION_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE IDENTIFICATION_REASON_CODE_JOINER ADD CONSTRAINT reason_code_joiner_fk
FOREIGN KEY (IDENTIFICATION_SEARCH_REASON_CODE_ID)
REFERENCES IDENTIFICATION_SEARCH_REASON_CODE (IDENTIFICATION_SEARCH_REASON_CODE_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PERSON_SYSTEMS_TO_SEARCH ADD CONSTRAINT systems_to_search_person_systems_to_search_fk
FOREIGN KEY (SYSTEMS_TO_SEARCH_ID)
REFERENCES SYSTEMS_TO_SEARCH (SYSTEMS_TO_SEARCH_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PERSON_SEARCH_RESULTS ADD CONSTRAINT systems_to_search_person_search_results_fk
FOREIGN KEY (SYSTEMS_TO_SEARCH_ID)
REFERENCES SYSTEMS_TO_SEARCH (SYSTEMS_TO_SEARCH_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PERSON_SEARCH_REQUEST ADD CONSTRAINT user_info_person_search_request_fk
FOREIGN KEY (USER_INFO_ID)
REFERENCES USER_INFO (USER_INFO_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE QUERY_REQUEST ADD CONSTRAINT user_info_query_request_fk
FOREIGN KEY (USER_INFO_ID)
REFERENCES USER_INFO (USER_INFO_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE IDENTIFICATION_SEARCH_REQUEST ADD CONSTRAINT user_info_criminal_identification_search_request_fk
FOREIGN KEY (USER_INFO_ID)
REFERENCES USER_INFO (USER_INFO_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE USER_LOGIN ADD CONSTRAINT user_info_user_login_fk
FOREIGN KEY (USER_INFO_ID)
REFERENCES USER_INFO (USER_INFO_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE IDENTIFICATION_SEARCH_RESULTS ADD CONSTRAINT identification_search_request_identification_search_results_fk
FOREIGN KEY (IDENTIFICATION_SEARCH_REQUEST_ID)
REFERENCES IDENTIFICATION_SEARCH_REQUEST (IDENTIFICATION_SEARCH_REQUEST_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE IDENTIFICATION_REASON_CODE_JOINER ADD CONSTRAINT id_search_request_id_reason_code_joiner_fk
FOREIGN KEY (IDENTIFICATION_SEARCH_REQUEST_ID)
REFERENCES IDENTIFICATION_SEARCH_REQUEST (IDENTIFICATION_SEARCH_REQUEST_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE CRIMINAL_HISTORY_QUERY_RESULTS ADD CONSTRAINT query_request_criminal_history_query_results_fk
FOREIGN KEY (QUERY_REQUEST_ID)
REFERENCES QUERY_REQUEST (QUERY_REQUEST_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE WARRANT_QUERY_RESULTS ADD CONSTRAINT query_request_warrant_query_results_fk
FOREIGN KEY (QUERY_REQUEST_ID)
REFERENCES QUERY_REQUEST (QUERY_REQUEST_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE IDENTIFICATION_RESULTS_QUERY_DETAIL ADD CONSTRAINT query_request_identification_results_query_detail_fk
FOREIGN KEY (QUERY_REQUEST_ID)
REFERENCES QUERY_REQUEST (QUERY_REQUEST_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE FIREARMS_QUERY_RESULTS ADD CONSTRAINT query_request_firearms_query_results_fk
FOREIGN KEY (QUERY_REQUEST_ID)
REFERENCES QUERY_REQUEST (QUERY_REQUEST_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PERSON_SEARCH_REQUEST ADD CONSTRAINT search_qualifier_codes_person_search_request_fk
FOREIGN KEY (LAST_NAME_QUALIFIER_CODE_ID)
REFERENCES SEARCH_QUALIFIER_CODES (SEARCH_QUALIFIER_CODES_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PERSON_SEARCH_REQUEST ADD CONSTRAINT search_qualifier_codes_person_search_request_fk1
FOREIGN KEY (FIRST_NAME_QUALIFIER_CODE_ID)
REFERENCES SEARCH_QUALIFIER_CODES (SEARCH_QUALIFIER_CODES_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PERSON_SEARCH_RESULTS ADD CONSTRAINT person_search_request_person_search_results_fk
FOREIGN KEY (PERSON_SEARCH_REQUEST_ID)
REFERENCES PERSON_SEARCH_REQUEST (PERSON_SEARCH_REQUEST_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PERSON_SYSTEMS_TO_SEARCH ADD CONSTRAINT person_search_request_person_systems_to_search_fk
FOREIGN KEY (PERSON_SEARCH_REQUEST_ID)
REFERENCES PERSON_SEARCH_REQUEST (PERSON_SEARCH_REQUEST_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE NOTIFICATION_PROPERTIES ADD CONSTRAINT notifications_sent_subscription_properties_fk
FOREIGN KEY (NOTIFICATIONS_SENT_ID)
REFERENCES NOTIFICATIONS_SENT (NOTIFICATIONS_SENT_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE NOTIFICATION_MECHANISM ADD CONSTRAINT notifications_sent_email_address_fk
FOREIGN KEY (NOTIFICATIONS_SENT_ID)
REFERENCES NOTIFICATIONS_SENT (NOTIFICATIONS_SENT_ID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;