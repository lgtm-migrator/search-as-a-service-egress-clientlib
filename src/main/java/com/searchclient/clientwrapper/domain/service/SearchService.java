package com.searchclient.clientwrapper.domain.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.searchclient.clientwrapper.domain.dto.SearchResponse;
import com.searchclient.clientwrapper.domain.dto.logger.Loggers;
import com.searchclient.clientwrapper.domain.port.api.SearchServicePort;
import com.searchclient.clientwrapper.domain.utils.LoggerUtils;
import com.searchclient.clientwrapper.domain.utils.MicroserviceHttpGateway;
import com.searchclient.clientwrapper.domain.utils.SearchUtil;

@Service
public class SearchService implements SearchServicePort {
    private static final String RESULTS = "results";
	private static final String STATUS_CODE = "statusCode";
	private static final String QUERY_PROCESS_ERROR = "Could not execute query to fetch records. URL: %s";
	/*
     * Solr Search Records for given collection- Egress Service
     */
    @Value("${microservice.base-url}")
    private String baseMicroserviceUrl;
    @Value("${microservice.version}")
    private String microserviceVersion;
    private String apiEndpoint = "";
    private final Logger logger = LoggerFactory.getLogger(SearchService.class);
    
    private String servicename = "Search_Service";

	private String username = "Username";

    @Autowired
    SearchResponse searchResponse = new SearchResponse();
    @Autowired
    MicroserviceHttpGateway microserviceHttpGateway;

    private void requestMethod(Loggers loggersDTO, String nameofCurrMethod) {

		String timestamp = LoggerUtils.utcTime().toString();
		loggersDTO.setNameofmethod(nameofCurrMethod);
		loggersDTO.setTimestamp(timestamp);
		loggersDTO.setServicename(servicename);
		loggersDTO.setUsername(username);
	}
    
    @Override
    public SearchResponse setUpSelectQuerySearchViaQueryField(
    		int clientId, 
    		String tableName, 
    		String queryField, String searchTerm, 
    		String startRecord, 
    		String pageSize, 
    		String orderBy, String order, 
    		Loggers loggersDTO) {
        /* Egress API -- table records -- SEARCH VIA QUERY FIELD */
        logger.debug("Performing search-records VIA QUERY FIELD for given table");
        
        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        requestMethod(loggersDTO, nameofCurrMethod);
		LoggerUtils.printlogger(loggersDTO,true,false);	

		// Perform Validations on input data
		// VALIDATE queryField
		boolean isQueryFieldValidated = SearchUtil.checkIfNameIsAlphaNumeric(queryField.trim());

		if(!isQueryFieldValidated) {
			searchResponse.setStatusCode(406);
			searchResponse.setMessage("Query-field validation unsuccessful. Query-field entry can only be in alphanumeric format");
			searchResponse.setSolrDocuments(null);
			return searchResponse;
		}
					
        microserviceHttpGateway.setApiEndpoint(
        		baseMicroserviceUrl + microserviceVersion + apiEndpoint
        		+ "/" + clientId
        		+ "/" + tableName
        		+ "?queryField=" + queryField + "&searchTerm=" + searchTerm
        		+ "&startRecord=" + startRecord
                + "&pageSize=" + pageSize
                + "&orderBy=" + orderBy + "&order=" + order);

        try {
            JSONObject jsonObject = microserviceHttpGateway.getRequest();

            if(Integer.parseInt(jsonObject.get(STATUS_CODE).toString()) == 400) {
            	searchResponse.setStatusCode(400);
            	searchResponse.setMessage(
            			String.format(
            					QUERY_PROCESS_ERROR, 
            					microserviceHttpGateway.getApiEndpoint()));
            	searchResponse.setSolrDocuments(null);
            } else {
        		searchResponse.setStatusCode(Integer.parseInt(jsonObject.get(STATUS_CODE).toString()));
                searchResponse.setMessage(jsonObject.get("responseMessage").toString());
                searchResponse.setSolrDocuments(jsonObject.get(RESULTS));
            }
            logger.debug("completed service run");
            
            loggersDTO.setTimestamp(LoggerUtils.utcTime().toString());
    		LoggerUtils.printlogger(loggersDTO,false,false);
            
        } catch (Exception e) {
        	logger.error("Exception occurred while performing getRequest operation via Http Gateway: {}", e.getMessage());
        	searchResponse.setStatusCode(400);
        	searchResponse.setMessage(
        			String.format(
        					QUERY_PROCESS_ERROR, 
        					microserviceHttpGateway.getApiEndpoint()));
        	searchResponse.setSolrDocuments(null);
        	LoggerUtils.printlogger(loggersDTO,false,true);
        }

        return searchResponse;
    }

	@Override
	public SearchResponse setUpSelectQuerySearchViaQuery(
			int clientId, String tableName, 
			String searchQuery, 
			String startRecord, String pageSize, String orderBy, String order, Loggers loggersDTO) {
        /* Egress API -- table records -- SEARCH VIA QUERY BUILDER */
        logger.debug("Performing search-records VIA QUERY BUILDER for given table");
        
        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
		requestMethod(loggersDTO,nameofCurrMethod);
		LoggerUtils.printlogger(loggersDTO,true,false);
		
		apiEndpoint = "/query";
        microserviceHttpGateway.setApiEndpoint(
        		baseMicroserviceUrl + microserviceVersion + apiEndpoint
        		+ "/" + clientId
        		+ "/" + tableName
        		+ "?searchQuery=" + searchQuery
        		+ "&startRecord=" + startRecord
                + "&pageSize=" + pageSize
                + "&orderBy=" + orderBy + "&order=" + order);
        
        try {
            JSONObject jsonObject = microserviceHttpGateway.getRequest();
            
            // testing
            logger.info("gateway >>>>>>> {}", microserviceHttpGateway.getRequest());
            logger.info("gateway obj >>>>>>> {}", jsonObject.get(RESULTS));
            
			searchResponse.setStatusCode(Integer.parseInt(jsonObject.get(STATUS_CODE).toString()));
			searchResponse.setMessage(jsonObject.get("responseMessage").toString());
			if(Integer.parseInt(jsonObject.get(STATUS_CODE).toString()) != 400)
				searchResponse.setSolrDocuments(jsonObject.get(RESULTS));
			else
				searchResponse.setSolrDocuments(jsonObject.get(null));
            logger.debug("completed service run");
            
            loggersDTO.setTimestamp(LoggerUtils.utcTime().toString());
    		LoggerUtils.printlogger(loggersDTO,false,false);
            
        } catch (Exception e) {
        	logger.error("Exception occurred while performing getRequest operation via Http Gateway: {}", e.getMessage());
        	searchResponse.setStatusCode(400);
        	searchResponse.setMessage(
        			String.format(
        					QUERY_PROCESS_ERROR, 
        					microserviceHttpGateway.getApiEndpoint()));
        	searchResponse.setSolrDocuments(null);
        	LoggerUtils.printlogger(loggersDTO,false,true);
        }
        
        // testing
        logger.info("searchResponse ######## {}", searchResponse);

        return searchResponse;
	}

}
