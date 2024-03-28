package org.mifos.chatbot.server.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.mifos.chatbot.server.model.Intent;
import org.mifos.chatbot.server.model.LatestMessage;
import org.mifos.chatbot.server.model.Tracker;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j
public class Helper {
    public JsonObject createJSON(String string) {
        Gson gson = new Gson();
//        log.info(string);
        JsonObject jsonObject = gson.fromJson(string, JsonObject.class);
        return jsonObject;
    }

    public String createJSONRequest(String string) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        ObjectWriter writer = mapper.writer().without(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);

        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("message", string);

        return writer.writeValueAsString(jsonNode);
    }

    public Tracker createTrackerPOJO(JsonObject obj) {
        Tracker tracker = new Tracker();
        obj.keySet().forEach(
                key->{
                    if(key.equals("sender_id"))
                        tracker.setConversationId(obj.get("sender_id").toString());
                    else if(key.equals("slots")){
                        Map<String,String > map=new HashMap<>();
                        JsonObject slotObject=obj.getAsJsonObject("slots");
                        slotObject.keySet().stream().forEach(k->{
                            if (!slotObject.get(k).isJsonNull())
                            map.put(k,slotObject.get(k).getAsString());
                        });
                        tracker.setSlots(map);
                    }

                    else if(key.equals("latest_message")) {
                        log.info(obj.get("latest_message").toString());
                        tracker.setLatestMessage(constructLatestMessage(obj.get("latest_message")));
                    }
                    else if(key.equals("latest_event_time"))
                        tracker.setLatestEventTime(null);
                    else if(key.equals("followup_action"))
                        tracker.setFollowupAction(obj.get("followup_action").toString());
                    else if(key.equals("paused"))
                        tracker.setPaused(obj.get("paused").getAsBoolean());
                    else if(key.equals("latest_input_channel"))
                        tracker.setLatestInputChannel(obj.get("latest_input_channel").toString());
                    else if(key.equals("active_loop"))
                        tracker.setActiveLoopName(obj.get("active_loop").toString());
                    else if(key.equals("latest_action"))
                        tracker.setLatestAction(null);
                    else if(key.equals("latest_action_name"))
                        tracker.setLatestActionName(obj.get("latest_action_name").toString());

                }
        );

        return tracker;
    }

    private LatestMessage constructLatestMessage(JsonElement obj) {
        JsonObject jsonObject = obj.getAsJsonObject();
        log.info(jsonObject.toString());
        return LatestMessage.builder()
                .intent(constructIntents(jsonObject))
                .entity(null)
                .text(jsonObject.get("text").toString())
                .intentRanking(null)
                .build();
    }

    public Intent constructIntents(JsonObject obj) {
        JsonElement ele = obj.get("intent");
        JsonObject a = ele.getAsJsonObject();
        return Intent.builder()
                .name(a.get("name").getAsString())
                .confidence(a.get("confidence").getAsDouble())
                .build();
    }

    public String getDate(List<Integer> dateList) {
        int year = dateList.get(0);
        int month = dateList.get(1);
        int day = dateList.get(2);

        // Format the date as 'MM/DD/YYYY'
        return String.format("%02d/%02d/%04d", month, day, year);
    }
}
