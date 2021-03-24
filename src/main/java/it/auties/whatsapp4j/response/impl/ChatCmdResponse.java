package it.auties.whatsapp4j.response.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.auties.whatsapp4j.response.model.JsonResponseModel;
import lombok.Builder;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A json model that contains information about an update about the metadata of a WhatsappChat
 *
 * @param jid the jid of the WhatsappChat this update regards
 * @param cmd a nullable String used to describe the update
 * @param data a list of objects that represent the encoded update
 */
public record ChatCmdResponse(@JsonProperty("id") @NotNull String jid, @Nullable String cmd,
                              @NotNull List<Object> data) implements JsonResponseModel {

}