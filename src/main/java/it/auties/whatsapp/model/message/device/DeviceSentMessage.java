package it.auties.whatsapp.model.message.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import it.auties.protobuf.api.model.ProtobufProperty;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.model.message.model.DeviceMessage;
import it.auties.whatsapp.model.message.model.MessageContainer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import static it.auties.protobuf.api.model.ProtobufProperty.Type.MESSAGE;
import static it.auties.protobuf.api.model.ProtobufProperty.Type.STRING;

/**
 * A model class that represents a WhatsappMessage that refers to a message sent by the device paired with the active WhatsappWeb session.
 * This class is only a model, this means that changing its values will have no real effect on WhatsappWeb's servers.
 * Instead, methods inside {@link Whatsapp} should be used.
 */
@AllArgsConstructor(staticName = "newDeviceSentMessage")
@NoArgsConstructor
@Data
@Jacksonized
@Builder(builderMethodName = "newDeviceSentMessage", buildMethodName = "create")
@Accessors(fluent = true)
public final class DeviceSentMessage implements DeviceMessage {
  /**
   * The unique identifier that this message update regards.
   */
  @ProtobufProperty(index = 1, type = STRING)
  private String destinationJid;

  /**
   * The message container that this object wraps.
   */
  @ProtobufProperty(index = 2, type = MESSAGE, concreteType = MessageContainer.class)
  private MessageContainer message;

  /**
   * The hash of the destination chat
   */
  @ProtobufProperty(index = 3, type = STRING)
  private String phash;
}