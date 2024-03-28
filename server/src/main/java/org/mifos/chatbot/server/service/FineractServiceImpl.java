package org.mifos.chatbot.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mifos.chatbot.server.config.DisableCertificateValidation;
import org.mifos.chatbot.server.config.openFeign.Response.GetClientInfoResponse;
import org.mifos.chatbot.server.config.openFeign.Response.GetLoansResponse;
import org.mifos.chatbot.server.model.ClientCountapi;
import org.mifos.chatbot.server.model.LoanAccounts;
import org.mifos.chatbot.server.config.openFeign.FineractServiceOpenFeign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class FineractServiceImpl {

   @Autowired
   private FineractServiceOpenFeign openFeign;

    public LoanAccounts getClientDetails(int clientId, HttpServletRequest request) {
        LoanAccounts loanAccounts = new LoanAccounts();
        try {
            String basicAuthCredentials=request.getHeader("Authorization");
            DisableCertificateValidation.disable();
//            String basicAuthCredentials = "Basic " + Base64.getEncoder().encodeToString((admin_username + ":" + admin_password).getBytes());
            log.info(basicAuthCredentials.toString());
            ResponseEntity feignResponse = openFeign.getClient(clientId , "default", basicAuthCredentials); //TODO: change hard coded client to soft code
            if(feignResponse.getStatusCode().value() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    loanAccounts = objectMapper.readValue(feignResponse.getBody().toString(), LoanAccounts.class);
                    log.info("feign response " + feignResponse.getBody().toString());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }

        return loanAccounts;
    }
    public GetLoansResponse getLoanDetails(String response, HttpServletRequest request) {
         String basicAuthCredentials=request.getHeader("Authorization");
         ResponseEntity feignResponse = openFeign.getLoans(1, "all", "guarantors,futureSchedule",
                "default", basicAuthCredentials);
        if(feignResponse.getStatusCode().value() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(feignResponse.getBody().toString(), GetLoansResponse.class);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    public ClientCountapi getAllClient(HttpServletRequest request) {
        try {
            DisableCertificateValidation.disable();

            String basicAuthCredentials=request.getHeader("Authorization");
            log.info(basicAuthCredentials.toString());
            String requestBody = "{\"request\": {\"text\": \"\"}, \"page\": 0, \"size\": 50}";
            ResponseEntity feignResponse=openFeign.getClientCount(requestBody,"default",basicAuthCredentials);
        log.info(feignResponse.getBody().toString());
        if(feignResponse.getStatusCode().value() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(feignResponse.getBody().toString(), ClientCountapi.class);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        }catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
    public GetClientInfoResponse getClientInfo(HttpServletRequest request) {
        String basicAuthCredentials=request.getHeader("Authorization");
        ResponseEntity feignResponse = openFeign.getClientInfo(1,"gsoc", basicAuthCredentials);
        if(feignResponse.getStatusCode().value() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(feignResponse.getBody().toString(), GetClientInfoResponse.class);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
