package it.auties.whatsapp.model.info;

import it.auties.protobuf.base.ProtobufProperty;
import it.auties.protobuf.base.ProtobufType;
import it.auties.whatsapp.model.sync.DeviceListMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import static it.auties.protobuf.base.ProtobufType.INT32;
import static it.auties.protobuf.base.ProtobufType.MESSAGE;
@AllArgsConstructor
@Data
@Builder
@Jacksonized
@Accessors(fluent = true)
public final class MessageContextInfo implements Info {
    @ProtobufProperty(index = 1, type = MESSAGE, implementation = DeviceListMetadata.class)
    private DeviceListMetadata deviceListMetadata;

    @ProtobufProperty(index = 2, type = INT32)
    private Integer deviceListMetadataVersion;

    @ProtobufProperty(index = 3, name = "messageSecret", type = ProtobufType.BYTES)
    private byte[] messageSecret;

    @ProtobufProperty(index = 4, name = "paddingBytes", type = ProtobufType.BYTES)
    private byte[] paddingBytes;
}