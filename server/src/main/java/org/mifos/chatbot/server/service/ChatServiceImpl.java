package org.mifos.chatbot.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.mifos.chatbot.server.model.Intent;
import org.mifos.chatbot.server.model.Textmodel;
import org.mifos.chatbot.server.model.Tracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl {
    @Value("${RASA_SERVER}")
    private String RASA_SERVER;
    @Value("${WEBHOOK}")
    private String WEBHOOK;
    Helper helper=new Helper();

    private static final String LOGIN_CREDENTIALS = "login_credentials";
    private static final String LOAN_STATUS = "loan_status";
    private static final String DISBURSEMENT_AMOUNT = "disbursement_amount";
    private static final String CLIENT_COUNT = "get_client_count";
    private static final String MATURITY_DATE = "maturity_date";
    private static final String NEXT_DUE_DATE = "next_due_date";
    private static final String NEXT_DUE_PRINCIPAL = "next_due_principal";
    private static final String APPROVED_PRINCIPAL = "approved_principal";
    private static final String INTEREST_RATE = "interest_rate";
    private static final String PREVIOUS_PAYMENT_DATE = "previous_payment_date";
    private static final String PREVIOUS_PAYMENT_AMOUNT = "previous_payment_amount";
    private static final String PREVIOUS_PAYMENT_INTEREST = "previous_payment_interest";
    private static final String ARREAR_DAY = "arrear_day";
    private static final String LOAN_DISBURSED_DATE = "loan_disbursed_date";
    private static final String LOAN_APPROVED_DATE = "loan_approved_date";
    private static final String FIRST_REPAYMENT_DATE = "first_repayment_date";
    private static final String CLIENT_ACTIVATION_DATE = "client_activation_date";

    @Autowired
    private GetLoanServiceImpl loanService;

    public List<Textmodel> processUserUtterance( Response botResponse, String conversationId) throws IOException {
        log.info("hello this is from the processuserutterance method"+botResponse);
        String res = botResponse.body().string();
        log.info(res);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(res);
        Tracker tracker = retriveConversationTracker(jsonNode.get(0).get("recipient_id").textValue());
        Intent intent = findIntent(tracker);
        log.info("intent loggin "+ intent.toString());
        return classifyIntent(jsonNode, intent, tracker);
    }
    public Response getResponse(String message) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        System.out.println("JSON Payload: " + helper.createJSONRequest(message));
        log.info("request body in rasa service " + message);
        RequestBody body = RequestBody.create(mediaType,(message));
        Request request = new Request.Builder()
                .url("http://localhost:5005/webhooks/rest/webhook")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() != 200) {
            //TODO handle exception
        }
        log.info("ahasas" + response.message());
        return response;
    }

    public List<Textmodel> classifyIntent(JsonNode jsonNode, Intent intent, Tracker tracker) throws IOException {
        String botResponse = "";
        List<Textmodel> list= new ArrayList<>();
//        String res = botResponse1.body().string();
//        log.info(res);
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode jsonNode = mapper.readTree(res);
        log.info("hello this is "+jsonNode.toString());
//        log.info(jsonNode.get("text").toString());
        // Extract the recipient_id fi
        String intentName = intent.getName();
        String text = gettext11(intentName, botResponse, tracker);
        if(text.equals("Intent not found")){
            for (int i =0; i<jsonNode.size();i++) {
                list.add(new Textmodel(jsonNode.get(i).get("recipient_id").textValue(), jsonNode.get(i).get("text").textValue()));
            }
            return list;
        }
        else {
            list.add(new Textmodel(jsonNode.get(0).get("recipient_id").textValue(), text));
            return list;
        }
    }
    public String gettext11(String intentName, String botResponse, Tracker tracker){
        if(intentName.equals(LOGIN_CREDENTIALS)) {
            loanService.authorization(tracker.getLatestMessage().getText());
        }
        if(intentName.equals(LOAN_STATUS)) {
//            textmodel.setRecipientId(jsonNode.get("recipient_id").asText());
            return loanService.getLoanStatus(botResponse, tracker);
//            return textmodel;
        }
        else if(intentName.equals(APPROVED_PRINCIPAL)) {
            return loanService.getApprovedPrincipalAmount(botResponse).toString();
        }
        else if(intentName.equals(INTEREST_RATE)) {
            return loanService.getInterestRate(botResponse).toString();
        }
        else if(intentName.equals(MATURITY_DATE)) {
            return loanService.getMaturityDate(botResponse);
        }
        else if(intentName.equals(NEXT_DUE_DATE)) {
            return loanService.getNextDueDate(botResponse);
        }
        else if(intentName.equals(NEXT_DUE_PRINCIPAL)) {
            return loanService.getNextDuePrincipal(botResponse);
        }
        else if(intentName.equals(PREVIOUS_PAYMENT_DATE)) {
            return loanService.getPreviousPaymentDate(botResponse);
        }
        else if(intentName.equals(PREVIOUS_PAYMENT_AMOUNT)) {
            return loanService.getPreviousPaymentAmount(botResponse);
        }
        else if(intentName.equals(PREVIOUS_PAYMENT_INTEREST)) {
            return loanService.getPreviousPaymentInterest(botResponse);
        }
        else if(intentName.equals(ARREAR_DAY)) {
            return loanService.getArrearDays(botResponse);
        }
        else if (intentName.equals(CLIENT_COUNT)){
            return loanService.getClientCount(botResponse);
        }
        else if(intentName.equals(LOAN_DISBURSED_DATE)) {
            return loanService.getLoanDisbursedDate(botResponse);
        }
        else if(intentName.equals(LOAN_APPROVED_DATE)) {
            return loanService.getLoanApprovedDate(botResponse);
        }
        else if(intentName.equals(FIRST_REPAYMENT_DATE)) {
            return loanService.getFirstRepaymentDate(botResponse);
        }
        else if(intentName.equals(CLIENT_ACTIVATION_DATE)){
            return loanService.getClientActivationDate(botResponse);
        }
        else if(intentName.equals(DISBURSEMENT_AMOUNT)) {
            return loanService.getDisbursementAmount(botResponse);
        }
        else {
            return "Intent not found";
        }
    }

    public Tracker  retriveConversationTracker(String conversationId) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        log.info("hellodcsa");

        Request request = new Request.Builder()
                .url(RASA_SERVER + "/conversations/" + conversationId + "/tracker")
                .header("Connection", "close")
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();
//                    log.info("res body "+response.body().string());
        if(response.code() != 200) {
            //TODO add logging
        }
        JsonObject obj = helper.createJSON(response.body().string());
//        log.info("Json obj check "+ obj.toString());
        Tracker tracker = helper.createTrackerPOJO(obj);
        return tracker;
    }

    public Intent findIntent(Tracker tracker) {
        return tracker.getLatestMessage().getIntent();
    }
}
