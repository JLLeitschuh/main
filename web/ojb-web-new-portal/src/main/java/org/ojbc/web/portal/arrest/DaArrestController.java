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
package org.ojbc.web.portal.arrest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.ojbc.web.OjbcWebConstants.ArrestType;
import org.ojbc.web.portal.AppProperties;
import org.ojbc.web.portal.services.CodeTableService;
import org.ojbc.web.portal.services.SamlService;
import org.ojbc.web.portal.services.SearchResultConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({"arrestSearchResults", "arrestSearchRequest", "arrestDetail", "arrestDetailTransformed", "daDispoCodeMapping", 
	"daAmendedChargeCodeMapping", "daFiledChargeCodeMapping", "daAlternateSentenceMapping", "daReasonsForDismissalMapping", 
	"daProvisionCodeMapping", "chargeSeverityCodeMapping", "submitArrestConfirmationMessage", "daGeneralOffenseCodeMapping", 
	"daGeneralOffenseDescMapping", "dispoCodesNotRequiringChargeSeverity", "yearList"})
@RequestMapping("/daArrests")
public class DaArrestController {
	private static final Log log = LogFactory.getLog(DaArrestController.class);

	@Autowired
	ArrestService arrestService;
	
	@Resource
	SearchResultConverter searchResultConverter;

	@Resource
	SamlService samlService;
	
	@Resource
	CodeTableService codeTableService;
	
	@Resource
	AppProperties appProperties;
	
	@Resource
	DispositionValidator dispositionValidator;
	
    @ModelAttribute
    public void addModelAttributes(Model model) {
    	
		model.addAttribute("disposition", new Disposition(ArrestType.DA));
		
		if (!model.containsAttribute("daDispoCodeMapping")) {
			model.addAttribute("daDispoCodeMapping", codeTableService.getDaDispositionCodeMap());
		}
		
		if (!model.containsAttribute("daAmendedChargeCodeMapping")) {
			model.addAttribute("daAmendedChargeCodeMapping", codeTableService.getDaAmendedChargeCodeMap());
		}
		
		if (!model.containsAttribute("daFiledChargeCodeMapping")) {
			model.addAttribute("daFiledChargeCodeMapping", codeTableService.getDaFiledChargeCodeMap());
		}
		
		if (!model.containsAttribute("daAlternateSentenceMapping")) {
			model.addAttribute("daAlternateSentenceMapping", codeTableService.getDaAlternateSentenceMap());
		}
		
		if (!model.containsAttribute("daReasonsForDismissalMapping")) {
			model.addAttribute("daReasonsForDismissalMapping", codeTableService.getDaReasonsForDismissalMap());
		}
		if (!model.containsAttribute("daProvisionCodeMapping")) {
			model.addAttribute("daProvisionCodeMapping", codeTableService.getDaProvisions());
		}
		if (!model.containsAttribute("chargeSeverityCodeMapping")) {
			model.addAttribute("chargeSeverityCodeMapping", appProperties.getChargeSeverityCodeMapping());
		}
		if (!model.containsAttribute("daCaseTypeCodeMapping")) {
			model.addAttribute("daCaseTypeCodeMapping", appProperties.getDaCaseTypeCodeMapping());
		}
		if (!model.containsAttribute("daGeneralOffenseCodeMapping")) {
			model.addAttribute("daGeneralOffenseCodeMapping", appProperties.getDaGeneralOffenseCodeMapping());
		}
		if (!model.containsAttribute("daGeneralOffenseDescMapping")) {
			model.addAttribute("daGeneralOffenseDescMapping", appProperties.getDaGeneralOffenseDescMapping());
		}
		if (!model.containsAttribute("submitArrestConfirmationMessage")) {
			model.addAttribute("submitArrestConfirmationMessage", appProperties.getSubmitArrestConfirmationMessage());
		}
		if (!model.containsAttribute("dispoCodesNotRequiringChargeSeverity")) {
			model.addAttribute("dispoCodesNotRequiringChargeSeverity", appProperties.getDispoCodesNotRequiringChargeSeverity());
		}
		if (!model.containsAttribute("yearList")) {
			List<String> yearList = new ArrayList<>(); 
			yearList.add(String.valueOf(LocalDate.now().getYear())); 
			yearList.add(String.valueOf(LocalDate.now().getYear() -1 )); 
			model.addAttribute("yearList", yearList);
		}
		
    }
    
	@InitBinder("disposition")
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(dispositionValidator);
	}

	@GetMapping("")
	public String defaultSearch(HttpServletRequest request, Map<String, Object> model) throws Throwable {
		ArrestSearchRequest arrestSearchRequest = (ArrestSearchRequest) model.get("arrestSearchRequest");
		
		if (arrestSearchRequest == null) {
			arrestSearchRequest = new ArrestSearchRequest(ArrestType.DA);
			model.put("arrestSearchRequest", arrestSearchRequest);
		}
		getArrestSearchResults(request, arrestSearchRequest, model);
		return "arrest/da/arrests::resultsPage";
	}

	@PostMapping("/advancedSearch")
	public String advancedSearch(HttpServletRequest request, @Valid @ModelAttribute ArrestSearchRequest arrestSearchRequest, BindingResult bindingResult, 
			Map<String, Object> model) throws Throwable {
		
		log.info("arrestSearchRequest:" + arrestSearchRequest );
		getArrestSearchResults(request, arrestSearchRequest, model);
		return "arrest/da/arrests::resultsList";
	}

	private void getArrestSearchResults(HttpServletRequest request, ArrestSearchRequest arrestSearchRequest,
			Map<String, Object> model) throws Throwable {
		arrestSearchRequest.setFirstNameSearchMetaData(); 
		arrestSearchRequest.setLastNameSearchMetaData();
		String searchContent = arrestService.findArrests(arrestSearchRequest, samlService.getSamlAssertion(request));
		String transformedResults = searchResultConverter.convertDaArrestSearchResult(searchContent);
		model.put("arrestSearchResults", searchContent); 
		model.put("arrestSearchContent", transformedResults); 
		model.put("arrestSearchRequest", arrestSearchRequest);
	}
	
	@GetMapping("/{id}")
	public String getArrest(HttpServletRequest request, @PathVariable String id, Map<String, Object> model) throws Throwable {
		getArrestDetail(request, id, model); 
		return "arrest/da/arrestDetail::arrestDetail";
	}

	@GetMapping("/{id}/unhide")
	public String unhideArrest(HttpServletRequest request, @PathVariable String id, Map<String, Object> model) throws Throwable {
		arrestService.unhideArrest(id, samlService.getSamlAssertion(request));
		
		ArrestSearchRequest arrestSearchrequest = (ArrestSearchRequest) model.get("arrestSearchRequest"); 
		getArrestSearchResults(request, arrestSearchrequest, model);
		return "arrest/da/arrests::resultsList";
	}
	
	@GetMapping("/{id}/hide")
	public String hideArrest(HttpServletRequest request, @PathVariable String id, Map<String, Object> model) throws Throwable {
		arrestService.hideArrest(id, samlService.getSamlAssertion(request));
		
		ArrestSearchRequest arrestSearchrequest = (ArrestSearchRequest) model.get("arrestSearchRequest"); 
		getArrestSearchResults(request, arrestSearchrequest, model);
		return "arrest/da/arrests::resultsList";
	}
	
	@GetMapping("/{id}/finalize")
	public String finalizeArrest(HttpServletRequest request, @PathVariable String id, Map<String, Object> model) throws Throwable {
		String response = arrestService.finalizeArrest(id, samlService.getSamlAssertion(request));
		log.info("finalize arrest response:" + response);
		ArrestSearchRequest arrestSearchrequest = (ArrestSearchRequest) model.get("arrestSearchRequest"); 
		getArrestSearchResults(request, arrestSearchrequest, model);
		return "arrest/da/arrests::resultsPage";
	}
	
	@GetMapping("/summary")
	public @ResponseBody String presentArrest(Map<String, Object> model) throws Throwable {
		String arrrestDetail = (String) model.get("arrestDetail");
		String transformedArrestSummary = searchResultConverter.convertArrestSummary(arrrestDetail);
		return transformedArrestSummary;
	}
	
	@GetMapping("/{id}/refer")
	public String referArrest(HttpServletRequest request, @PathVariable String id, Map<String, Object> model) throws Throwable {
		
		arrestService.referArrestToMuni(id, samlService.getSamlAssertion(request));
		
		ArrestSearchRequest arrestSearchrequest = (ArrestSearchRequest) model.get("arrestSearchRequest"); 
		getArrestSearchResults(request, arrestSearchrequest, model);
		return "arrest/da/arrests::resultsList";
	}
	
	private void getArrestDetail(HttpServletRequest request, String id, Map<String, Object> model) throws Throwable {
		String searchContent = arrestService.getArrest(id, samlService.getSamlAssertion(request));
		String transformedResults = searchResultConverter.convertDaArrestDetail(searchContent);
		model.put("arrestDetail", searchContent); 
		model.put("arrestDetailTransformed", transformedResults);
	}

	@GetMapping("/dispositionForm")
	public String getDispositionForm(HttpServletRequest request, Disposition disposition, 
			Map<String, Object> model) throws Throwable {
		model.put("disposition", disposition);
		return "arrest/da/dispositionForm::dispositionForm";
	}

	@PostMapping("/saveDisposition")
	public String saveDisposition(HttpServletRequest request, @Valid @ModelAttribute Disposition disposition, BindingResult bindingResult, 
			Map<String, Object> model) throws Throwable {
		
		if (bindingResult.hasErrors()) {
			log.info("has binding errors");
			log.info(bindingResult.getAllErrors());
			return "arrest/da/dispositionForm::dispositionForm";
		}
		
		disposition.assembleCourtCaseNumber();
		setCodeDescriptions(disposition, model); 
		log.info(disposition);
		arrestService.saveDisposition(disposition, samlService.getSamlAssertion(request));
//		String response = arrestService.saveDisposition(disposition, samlService.getSamlAssertion(request));
		getArrestDetail(request, disposition.getArrestIdentification(), model); 
		return "arrest/da/arrestDetail::arrestDetail";
	}

	@PostMapping("/dispositions/delete")
	public String deleteDisposition(HttpServletRequest request,  Disposition disposition,
			Map<String, Object> model) throws Throwable {
		log.debug("deleting disposition " + disposition.toString());
		arrestService.deleteDisposition(disposition, samlService.getSamlAssertion(request));
//		String response = arrestService.deleteDisposition(disposition, samlService.getSamlAssertion(request));
		getArrestDetail(request, disposition.getArrestIdentification(), model); 
		return "arrest/da/arrestDetail::arrestDetail";
	}
	
	@PostMapping("/charges/decline")
	public String delineCharge(HttpServletRequest request,  ArrestCharge arrestCharge,
			Map<String, Object> model) throws Throwable {
		log.debug("decline charge: " + arrestCharge);
		arrestCharge.setArrestType(ArrestType.DA);
		arrestService.declineCharge(arrestCharge, samlService.getSamlAssertion(request));
//		String response = arrestService.deleteDisposition(disposition, samlService.getSamlAssertion(request));
		getArrestDetail(request, arrestCharge.getArrestIdentification(), model); 
		return "arrest/da/arrestDetail::arrestDetail";
	}
	
	@GetMapping("/expungeDispositionForm")
	public String getExpungeDispositionForm(HttpServletRequest request, Disposition disposition, 
			Map<String, Object> model) throws Throwable {
		disposition.setDispositionDate(null);
		model.put("disposition", disposition);
		return "arrest/expungeDispositionForm::expungeDispositionForm";
	}
	
	@PostMapping("/expungeDisposition")
	public String expungeDisposition(HttpServletRequest request, @ModelAttribute Disposition disposition, BindingResult bindingResult, 
			Map<String, Object> model) throws Throwable {
		
		expunge(request, disposition, model); 
		return "arrest/da/arrestDetail::arrestDetail";
	}
	
	protected void expunge(HttpServletRequest request, Disposition disposition, Map<String, Object> model)
			throws Throwable {
		log.info("Expunge disposition" + disposition);
		arrestService.expungeDisposition(disposition, samlService.getSamlAssertion(request));
		getArrestDetail(request, disposition.getArrestIdentification(), model);
	}

	@SuppressWarnings("unchecked")
	private void setCodeDescriptions(@Valid Disposition disposition, Map<String, Object> model) {
		Map<String, String> daReasonsForDismissalMapping = (Map<String, String>) model.get("daReasonsForDismissalMapping"); 
		Map<String, String> daDispoCodeMapping = (Map<String, String>) model.get("daDispoCodeMapping"); 
		Map<String, String> daAmendedChargeCodeMapping = (Map<String, String>) model.get("daAmendedChargeCodeMapping"); 
		Map<String, String> daFiledChargeCodeMapping = (Map<String, String>) model.get("daFiledChargeCodeMapping"); 
		Map<String, String> daAlternateSentenceMapping = (Map<String, String>) model.get("daAlternateSentenceMapping"); 
		
		if (disposition.getAlternateSentences() != null && !disposition.getAlternateSentences().isEmpty()) {
			List<String> alternateSentenceDescriptions = disposition.getAlternateSentences().stream()
					.map(daAlternateSentenceMapping::get)
					.collect(Collectors.toList());
			disposition.setAlternateSentenceDescriptions(alternateSentenceDescriptions);
		}
		disposition.setAmendedChargeDescription(daAmendedChargeCodeMapping.get(disposition.getAmendedCharge()));
		disposition.setFiledChargeDescription(daFiledChargeCodeMapping.get(disposition.getFiledCharge()));
		disposition.setDispositionDescription(daDispoCodeMapping.get(disposition.getDispositionCode()));
		disposition.setReasonForDismissalDescripiton(daReasonsForDismissalMapping.get(disposition.getReasonForDismissal()));
	}
	

}