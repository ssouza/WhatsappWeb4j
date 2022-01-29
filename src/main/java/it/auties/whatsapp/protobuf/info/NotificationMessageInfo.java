package it.auties.whatsapp.protobuf.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import it.auties.whatsapp.protobuf.message.model.Message;
import it.auties.whatsapp.protobuf.message.model.MessageKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Accessors(fluent = true)
public final class NotificationMessageInfo implements WhatsappInfo {
  @JsonProperty("1")
  @JsonPropertyDescription("MessageKey")
  private MessageKey key;

  @JsonProperty("2")
  @JsonPropertyDescription("Message")
  private Message message;

  @JsonProperty("3")
  @JsonPropertyDescription("uint64")
  private long messageTimestamp;

  @JsonProperty("4")
  @JsonPropertyDescription("string")
  private String participant;
}