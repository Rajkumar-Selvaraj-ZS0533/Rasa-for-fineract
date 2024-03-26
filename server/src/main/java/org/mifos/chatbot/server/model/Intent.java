package org.mifos.chatbot.server.model;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Intent {
    Double confidence;
    String name;
}