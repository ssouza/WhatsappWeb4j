package it.auties.whatsapp.protobuf.message.model;

import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.manager.WhatsappStore;
import it.auties.whatsapp.protobuf.media.AttachmentProvider;
import it.auties.whatsapp.protobuf.message.payment.PaymentInvoiceMessage;
import it.auties.whatsapp.protobuf.message.standard.*;
import it.auties.whatsapp.util.Medias;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Locale;
import java.util.Objects;

/**
 * A model class that represents a WhatsappMessage sent by a contact and that holds media inside.
 * This class is only a model, this means that changing its values will have no real effect on WhatsappWeb's servers.
 * Instead, methods inside {@link Whatsapp} should be used.
 */
@AllArgsConstructor
@SuperBuilder(buildMethodName = "create")
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(exclude = "store", callSuper = true)
public abstract sealed class MediaMessage extends ContextualMessage implements AttachmentProvider
        permits PaymentInvoiceMessage, AudioMessage, DocumentMessage, ImageMessage, StickerMessage, VideoMessage {
    /**
     * The cached decoded media, by default null
     */
    private byte[] decodedMedia;

    /**
     * The store where this message is located.
     *
     * @apiNote even though the same instance is in the wrapping message info(MessageInfo -> MessageContainer -> MediaMessage),
     *          there is currently no way to navigate the tree upwards or any reason to do so considering that this is a special use case.
     *          Considering that passing the same instance to {@link MediaMessage#decodedMedia()} is verbose and unnecessary, there is a copy here.
     */
    @NonNull
    @Getter
    @Setter
    private WhatsappStore store;

    /**
     * Returns the cached decoded media wrapped by this object if available.
     * Otherwise, the encoded media that this object wraps is decoded, cached and returned.
     *
     * @return a non-null array of bytes
     */
    public byte @NonNull [] decodedMedia(){
        return Objects.requireNonNullElseGet(decodedMedia,
                () -> (this.decodedMedia = Medias.download(this, store)));
    }

    /**
     * Decodes the encoded media that this object wraps, caches it and returns the decoded media.
     *
     * @return a non-null array of bytes
     */
    public byte @NonNull [] refreshMedia(){
       this.decodedMedia = null;
       return decodedMedia();
    }

    /**
     * Returns the upload url of the encoded media that this object wraps
     *
     * @return a non-null string
     */
    public abstract @NonNull String url();

    /**
     * Returns the direct path to the encoded media that this object wraps
     *
     * @return a non-null string
     */
    public abstract @NonNull String directPath();


    /**
     * Returns the media type of the media that this object wraps
     *
     * @return a non-null {@link MediaMessageType}
     */
    public abstract @NonNull MediaMessageType type();


    /**
     * Returns the media key of the media that this object wraps
     *
     * @return a non-null array of bytes
     */
    public abstract byte @NonNull [] key();

    /**
     * Returns the timestamp, that is the seconds elapsed since {@link java.time.Instant#EPOCH}, for {@link MediaMessage#key()}
     *
     * @return an unsigned long
     */
    public abstract long mediaKeyTimestamp();


    /**
     * Returns the sha256 of the decoded media that this object wraps
     *
     * @return a non-null array of bytes
     */
    public abstract byte @NonNull [] fileSha256();


    /**
     * Returns the sha256 of the encoded media that this object wraps
     *
     * @return a non-null array of bytes
     */
    public abstract byte @NonNull [] fileEncSha256();


    /**
     * Returns the size of the decoded media that this object wraps
     *
     * @return an unsigned int
     */
    public abstract long fileLength();

    @Override
    public String name() {
        return type().name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String keyName() {
        return type().whatsappName();
    }
}