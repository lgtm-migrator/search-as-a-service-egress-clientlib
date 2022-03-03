package com.searchclient.clientwrapper.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.searchclient.clientwrapper.domain.dto.SearchResponseDTO;
import com.searchclient.clientwrapper.domain.dto.logger.LoggersDTO;
import com.searchclient.clientwrapper.domain.port.api.SearchServicePort;
import com.searchclient.clientwrapper.domain.utils.LoggerUtils;
import com.searchclient.clientwrapper.domain.utils.MicroserviceHttpGateway;

@Service
public class SearchService implements SearchServicePort {
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
    SearchResponseDTO solrSearchResponseDTO;

    private void requestMethod(LoggersDTO loggersDTO, String nameofCurrMethod) {

		String timestamp = LoggerUtils.utcTime().toString();
		loggersDTO.setNameofmethod(nameofCurrMethod);
		loggersDTO.setTimestamp(timestamp);
		loggersDTO.setServicename(servicename);
		loggersDTO.setUsername(username);
	}
    
    @Override
    public SearchResponseDTO setUpSelectQueryAdvancedSearch(
    		int clientId, 
    		String tableName, 
    		String queryField, String searchTerm, 
    		String startRecord, 
    		String pageSize, 
    		String orderBy, String order, 
    		LoggersDTO loggersDTO) {
        /* Egress API -- solr collection records -- ADVANCED SEARCH */
        logger.debug("Performing ADVANCED solr search for given collection");
        
        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
		requestMethod(loggersDTO, nameofCurrMethod);
		LoggerUtils.printlogger(loggersDTO,true,false);	
		
        MicroserviceHttpGateway microserviceHttpGateway = new MicroserviceHttpGateway();
        microserviceHttpGateway.setApiEndpoint(
        		baseMicroserviceUrl + microserviceVersion + apiEndpoint
        		+ "/" + clientId
        		+ "/" + tableName
        		+ "?queryField=" + queryField + "&searchTerm=" + searchTerm
        		+ "&startRecord=" + startRecord
                + "&pageSize=" + pageSize
                + "&orderBy=" + orderBy + "&order=" + order);
        JSONObject jsonObject = microserviceHttpGateway.getRequest();
        
        solrSearchResponseDTO.setSolrDocuments(jsonObject);
        logger.debug("completed service run");
        
        loggersDTO.setTimestamp(LoggerUtils.utcTime().toString());
		LoggerUtils.printlogger(loggersDTO,false,false);
        
        return solrSearchResponseDTO;
    }

	@Override
	public SearchResponseDTO setUpSelectQuerySearchViaQuery(
			int clientId, String tableName, 
			String searchQuery,
			String startRecord, String pageSize, String orderBy, String order, LoggersDTO loggersDTO) {
        /* Egress API -- solr collection records -- ADVANCED SEARCH */
        logger.debug("Performing ADVANCED solr search for given collection");
        
        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
		requestMethod(loggersDTO,nameofCurrMethod);
		LoggerUtils.printlogger(loggersDTO,true,false);
		
		apiEndpoint = "/query";
        MicroserviceHttpGateway microserviceHttpGateway = new MicroserviceHttpGateway();
        microserviceHttpGateway.setApiEndpoint(
        		baseMicroserviceUrl + microserviceVersion + apiEndpoint
        		+ "/" + clientId
        		+ "/" + tableName
        		+ "?searchQuery=" + searchQuery
        		+ "&startRecord=" + startRecord
                + "&pageSize=" + pageSize
                + "&orderBy=" + orderBy + "&order=" + order);
        JSONObject jsonObject = microserviceHttpGateway.getRequest();
        
        solrSearchResponseDTO.setSolrDocuments(jsonObject);
        logger.debug("completed service run");
        
        loggersDTO.setTimestamp(LoggerUtils.utcTime().toString());
		LoggerUtils.printlogger(loggersDTO,false,false);
        
        return solrSearchResponseDTO;
	}

}
