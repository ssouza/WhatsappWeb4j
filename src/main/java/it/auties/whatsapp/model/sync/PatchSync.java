package it.auties.whatsapp.model.sync;

import it.auties.protobuf.api.model.ProtobufMessage;
import it.auties.protobuf.api.model.ProtobufProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;

import static it.auties.protobuf.api.model.ProtobufProperty.Type.*;

@AllArgsConstructor
@Data
@Builder
@Jacksonized
@Accessors(fluent = true)
public class PatchSync implements ProtobufMessage {
    @ProtobufProperty(index = 1, type = MESSAGE, concreteType = VersionSync.class)
    private VersionSync version;

    @ProtobufProperty(index = 2, type = MESSAGE,
            concreteType = MutationSync.class, repeated = true)
    private List<MutationSync> mutations;

    @ProtobufProperty(index = 3, type = MESSAGE, concreteType = ExternalBlobReference.class)
    private ExternalBlobReference externalMutations;

    @ProtobufProperty(index = 4, type = BYTES)
    private byte[] snapshotMac;

    @ProtobufProperty(index = 5, type = BYTES)
    private byte[] patchMac;

    @ProtobufProperty(index = 6, type = MESSAGE, concreteType = KeyId.class)
    private KeyId keyId;

    @ProtobufProperty(index = 7, type = MESSAGE, concreteType = ExitCode.class)
    private ExitCode exitCode;

    @ProtobufProperty(index = 8, type = UINT32)
    private int deviceIndex;

    public boolean hasVersion() {
        return version != null && version.version() != 0;
    }

    public boolean hasExternalMutations() {
        return externalMutations != null;
    }

    public static class PatchSyncBuilder {
        public PatchSyncBuilder mutations(List<MutationSync> mutations) {
            if (this.mutations == null) this.mutations = new ArrayList<>();
            this.mutations.addAll(mutations);
            return this;
        }
    }
}