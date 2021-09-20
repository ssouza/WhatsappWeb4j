package it.auties.whatsapp4j.common.protobuf.model.button;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.auties.whatsapp4j.common.protobuf.message.business.HighlyStructuredMessage;
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
public class URLButton {
  @JsonProperty(value = "2")
  private HighlyStructuredMessage url;

  @JsonProperty(value = "1")
  private HighlyStructuredMessage displayText;
}