package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;

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
    private String fileBase64;
    private String fileExtension;
    private String filePath;
    private long timestamp;

    public static ChatMessageDTO toDTO(Document doc) {
        return new ChatMessageDTO(doc.getLong("orderId"), doc.getLong("fromUserId"), doc.getLong("toUserId"), doc.getString("message"),
                doc.getString("fileBase64"), doc.getString("fileExtension"), doc.getString("filePath"), doc.getLong("timestamp"));
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.append("orderId", orderId);
        doc.append("fromUserId", fromUserId);
        doc.append("toUserId", toUserId);
        doc.append("message", message);
        doc.append("fileBase64", fileBase64);
        doc.append("fileExtension", fileExtension);
        doc.append("filePath", filePath);
        doc.append("timestamp", System.currentTimeMillis());

        return doc;
    }
}