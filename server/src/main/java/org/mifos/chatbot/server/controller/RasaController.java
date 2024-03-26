package org.mifos.chatbot.server.controller;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import okhttp3.Response;
import org.mifos.chatbot.server.model.Textmodel;
import org.mifos.chatbot.server.service.Helper;
import org.mifos.chatbot.server.service.ChatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.http.HttpRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/")
public class RasaController {
    @Value("${RASA_SERVER}")
    private String RASA_SERVER;

    @Value("${WEBHOOK}")
    private String WEBHOOK;

    @Autowired
    ChatServiceImpl chatService;

    Helper helper = new Helper();
    @CrossOrigin(origins = "https://rajkumar-selvaraj-zs0533.github.io/")
    @PostMapping("/getUserUtterance")
    public ResponseEntity<List<Textmodel>> getUserUtterance(@org.springframework.web.bind.annotation.RequestBody String message) throws IOException {
        log.info("request body "+ message);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, helper.createJSONRequest(message));
        Response response = chatService.getResponse(message);
        //TODO how to fetch conversationId?
        log.info("hello this is the rasa controller class" + response);
//        log.info("res body of the chatservice" + response.toString());
//        log.info(response.body().string());
        return new ResponseEntity<>(chatService.processUserUtterance(response,"default"), HttpStatus.OK);
    }

    private String generateConversationId() {
        // Generate a unique conversation ID here
        return "CONV_" + System.currentTimeMillis();
    }
}
