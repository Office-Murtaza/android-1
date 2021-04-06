package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;

import javax.persistence.Transient;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageDTO {

    private Long orderId;
    private Long fromUserId;
    private Long toUserId;
    private String message;
    private String file;
    private long timestamp;

    @Transient
    public static ChatMessageDTO toDTO(Document doc) {
        return new ChatMessageDTO(doc.getLong("orderId"), doc.getLong("fromUserId"), doc.getLong("toUserId"), doc.getString("message"), doc.getString("file"), doc.getLong("timestamp"));
    }

    @Transient
    public Document toDocument() {
        Document doc = new Document();
        doc.append("orderId", orderId);
        doc.append("fromUserId", fromUserId);
        doc.append("toUserId", toUserId);
        doc.append("message", message);
        doc.append("file", file);
        doc.append("timestamp", System.currentTimeMillis());

        return doc;
    }
}