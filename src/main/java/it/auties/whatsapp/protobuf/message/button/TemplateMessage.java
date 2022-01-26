package it.auties.whatsapp.protobuf.message.button;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.protobuf.button.FourRowTemplate;
import it.auties.whatsapp.protobuf.button.HydratedFourRowTemplate;
import it.auties.whatsapp.protobuf.info.ContextInfo;
import it.auties.whatsapp.protobuf.message.model.ButtonMessage;
import it.auties.whatsapp.protobuf.message.model.ContextualMessage;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;

/**
 * A model class that represents a WhatsappMessage sent in a WhatsappBusiness chat that provides a list of buttons to choose from.
 * This class is only a model, this means that changing its values will have no real effect on WhatsappWeb's servers.
 * Instead, methods inside {@link Whatsapp} should be used.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(builderMethodName = "newTemplateMessage", buildMethodName = "create")
@Accessors(fluent = true)
public final class TemplateMessage extends ContextualMessage implements ButtonMessage {
  /**
   * Four row template.
   * This property is defined only if {@link TemplateMessage#type()} == {@link Format#FOUR_ROW_TEMPLATE}.
   */
  @JsonProperty("1")
  @JsonPropertyDescription("template")
  private FourRowTemplate fourRowTemplate;

  /**
   * Hydrated four row template.
   * This property is defined only if {@link TemplateMessage#type()} == {@link Format#HYDRATED_FOUR_ROW_TEMPLATE}.
   */
  @JsonProperty("2")
  @JsonPropertyDescription("template")
  private HydratedFourRowTemplate hydratedFourRowTemplate;

  /**
   * The context info of this message
   */
  @JsonProperty("3")
  @JsonPropertyDescription("context")
  private ContextInfo contextInfo; // Overrides ContextualMessage's context info

  /**
   * Hydrated template.
   * This property is defined only if {@link TemplateMessage#type()} == {@link Format#HYDRATED_FOUR_ROW_TEMPLATE}.
   */
  @JsonProperty("4")
  @JsonPropertyDescription("template")
  private HydratedFourRowTemplate hydratedTemplate;

  /**
   * Returns the type of format of this message
   *
   * @return a non-null {@link Format}
   */
  public Format type() {
    if (fourRowTemplate != null) return Format.FOUR_ROW_TEMPLATE;
    if (hydratedFourRowTemplate != null) return Format.HYDRATED_FOUR_ROW_TEMPLATE;
    return Format.UNKNOWN;
  }

  /**
   * The constant of this enumerated type define the various of types of visual formats for a {@link TemplateMessage}
   */
  @AllArgsConstructor
  @Accessors(fluent = true)
  public enum Format {
    /**
     * Unknown format
     */
    UNKNOWN(0),

    /**
     * Four row template
     */
    FOUR_ROW_TEMPLATE(1),

    /**
     * Hydrated four row template
     */
    HYDRATED_FOUR_ROW_TEMPLATE(2);

    @Getter
    private final int index;

    @JsonCreator
    public static Format forIndex(int index) {
      return Arrays.stream(values())
          .filter(entry -> entry.index() == index)
          .findFirst()
          .orElse(Format.UNKNOWN);
    }
  }
}