package it.auties.whatsapp.manager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.auties.whatsapp.binary.BinaryArray;
import it.auties.whatsapp.crypto.SignalHelper;
import it.auties.whatsapp.protobuf.contact.ContactJid;
import it.auties.whatsapp.protobuf.signal.keypair.SignalKeyPair;
import it.auties.whatsapp.protobuf.signal.keypair.SignalPreKeyPair;
import it.auties.whatsapp.protobuf.signal.keypair.SignalSignedKeyPair;
import it.auties.whatsapp.protobuf.signal.sender.SenderKeyName;
import it.auties.whatsapp.protobuf.signal.sender.SenderKeyRecord;
import it.auties.whatsapp.protobuf.signal.session.Session;
import it.auties.whatsapp.protobuf.signal.session.SessionAddress;
import it.auties.whatsapp.protobuf.sync.AppStateSyncKey;
import it.auties.whatsapp.protobuf.sync.LTHashState;
import it.auties.whatsapp.util.Validate;
import lombok.*;
import lombok.Builder.Default;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.java.Log;

import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.prefs.BackingStoreException;

import static java.util.prefs.Preferences.userRoot;

/**
 * This class is a data class used to hold the clientId, serverToken, clientToken, publicKey, privateKey, encryptionKey and macKey.
 * It can be serialized using Jackson and deserialized using the fromPreferences named constructor.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Jacksonized
@Data
@Accessors(fluent = true, chain = true)
@Log
@SuppressWarnings("UnusedReturnValue") // Chaining
public class WhatsappKeys {
    /**
     * The path used to serialize and deserialize this object
     */
    private static final String PREFERENCES_PATH = WhatsappKeys.class.getName();

    /**
     * An instance of Jackson
     */
    private static final ObjectMapper JACKSON = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * The client id
     */
    @JsonProperty
    private int id;

    /**
     * The secret key pair used for buffer messages
     */
    @JsonProperty
    @NonNull
    private SignalKeyPair companionKeyPair;

    /**
     * The ephemeral key pair
     */
    @JsonProperty
    @NonNull
    private SignalKeyPair ephemeralKeyPair;

    /**
     * The signed identity key
     */
    @JsonProperty
    @NonNull
    private SignalKeyPair identityKeyPair;

    /**
     * The signed pre key
     */
    @JsonProperty
    @NonNull
    private SignalSignedKeyPair signedKeyPair;

    /**
     * The adv secret key
     */
    @JsonProperty
    private byte @NonNull [] companionKey;

    /**
     * Whether these keys have generated pre keys assigned to them
     */
    @JsonProperty
    @NonNull
    private LinkedList<@NonNull SignalPreKeyPair> preKeys;

    /**
     * The user using these keys
     */
    @JsonProperty
    private ContactJid companion;

    /**
     * Sender keys for signal implementation
     */
    @JsonProperty
    private Map<SenderKeyName, SenderKeyRecord> senderKeys;

    /**
     * Sessions map
     */
    @JsonProperty
    private Map<SessionAddress, Session> sessions;

    /**
     * Trusted keys
     */
    @JsonProperty
    private Map<SessionAddress, byte[]> identities;

    /**
     * Hash state
     */
    @JsonProperty
    private Map<String, LTHashState> hashStates;

    /**
     * App state keys
     */
    @JsonProperty
    private List<AppStateSyncKey> appStateKeys;

    /**
     * Write counter for IV
     */
    @NonNull
    @Default
    private AtomicLong writeCounter = new AtomicLong();

    /**
     * Read counter for IV
     */
    @NonNull
    @Default
    private AtomicLong readCounter = new AtomicLong();

    /**
     * Session dependent keys to write and read cyphered messages
     */
    private BinaryArray writeKey, readKey;

    /**
     * Returns a list containing all known IDs
     *
     * @return a non-null list of ints
     */
    public static LinkedList<Integer> knownIds() {
        try {
            var json = userRoot().get(PREFERENCES_PATH, "[]");
            return JACKSON.readValue(json, new TypeReference<>() {});
        }catch (JsonProcessingException exception){
            throw new UncheckedIOException("Cannot read IDs", exception);
        }
    }

    /**
     * Clears all the keys from this machine's memory.
     * This method doesn't clear this object's values.
     */
    public static void deleteAllKeys() {
        try {
            userRoot().clear();
        }catch (BackingStoreException exception){
            throw new RuntimeException("Cannot delete keys from memory", exception);
        }
    }

    /**
     * Clears the keys associated with the provided id
     *
     * @param id the id of the keys
     */
    public static void deleteKeys(int id) {
        var path = getPath(id);
        userRoot().remove(path);
    }

    /**
     * Returns a new instance of random keys
     *
     * @return a non-null instance of WhatsappKeys
     */
    public static WhatsappKeys random(){
        return new WhatsappKeys(SignalHelper.randomRegistrationId());
    }

    /**
     * Returns the keys saved in memory or constructs a new clean instance
     *
     * @param id the id of this session
     * @return a non-null instance of WhatsappKeys
     */
    public static WhatsappKeys fromMemory(int id){
        var json = userRoot().get(getPath(id), null);
        return Optional.ofNullable(json)
                .map(value -> deserialize(id, value))
                .orElseGet(() -> new WhatsappKeys(id));
    }

    private static String getPath(Object... path) {
        var joined = new ArrayList<>(Arrays.asList(path));
        joined.add(0, PREFERENCES_PATH);
        return "%s$%s".formatted(joined.toArray());
    }

    private static WhatsappKeys deserialize(int id, String json) {
        try {
            return JACKSON.readValue(json, WhatsappKeys.class);
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
            log.warning("Cannot read keys for id %s: defaulting to new keys".formatted(id));
            return null;
        }
    }

    private WhatsappKeys(int id) {
        this.id = saveId(id);
        this.companionKeyPair = SignalKeyPair.random();
        this.ephemeralKeyPair = SignalKeyPair.random();
        this.identityKeyPair = SignalKeyPair.random();
        this.signedKeyPair = SignalSignedKeyPair.of(id, identityKeyPair());
        this.companionKey = SignalHelper.randomSenderKey();
        this.senderKeys = new ConcurrentHashMap<>();
        this.preKeys = new LinkedList<>();
        this.sessions = new ConcurrentHashMap<>();
        this.identities = new ConcurrentHashMap<>();
        this.hashStates = new ConcurrentHashMap<>();
        this.appStateKeys = new LinkedList<>();
        this.readCounter = new AtomicLong();
        this.writeCounter = new AtomicLong();
    }

    private int saveId(int id){
        try {
            var knownIds = knownIds();
            knownIds.add(id);
            userRoot().put(PREFERENCES_PATH, JACKSON.writeValueAsString(knownIds));
            return id;
        }catch (JsonProcessingException exception){
            throw new UncheckedIOException("Cannot serialize IDs", exception);
        }
    }

    /**
     * Serializes this object to a json and saves it in memory
     *
     * @return this
     */
    public WhatsappKeys save(){
        try {
            var path = getPath(id);
            userRoot().put(path, JACKSON.writeValueAsString(this));
            return this;
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Cannot save keys to memory", exception);
        }
    }

    /**
     * Clears the signal keys associated with this object
     *
     * @return this
     */
    public WhatsappKeys clear() {
        this.readKey = null;
        this.writeKey = null;
        this.writeCounter.set(0);
        this.readCounter.set(0);
        return this;
    }

    /**
     * Clears all the keys from this machine's memory.
     * This method doesn't clear this object's values.
     *
     * @return this
     */
    public WhatsappKeys delete() {
        deleteKeys(id);
        return this;
    }

    /**
     * Checks if the serverToken and clientToken are not null
     *
     * @return true if both the serverToken and clientToken are not null
     */
    public boolean hasCompanion() {
        return companion != null;
    }

    /**
     * Checks if the client sent pre keys to the server
     *
     * @return true if the client sent pre keys to the server
     */
    public boolean hasPreKeys() {
        return !preKeys.isEmpty();
    }

    /**
     * Queries the first {@link SenderKeyRecord} that matches {@code name}
     *
     * @param name the non-null name to search
     * @return a non-null Optional SenderKeyRecord
     */
    public Optional<SenderKeyRecord> findSenderKeyByName(@NonNull SenderKeyName name) {
        return Optional.ofNullable(senderKeys.get(name));
    }

    /**
     * Queries the {@link Session} that matches {@code address}
     *
     * @param address the non-null address to search
     * @return a non-null Optional SessionRecord
     */
    public Optional<Session> findSessionByAddress(@NonNull SessionAddress address){
        return Optional.ofNullable(sessions.get(address));
    }

    /**
     * Queries the trusted key that matches {@code id}
     *
     * @param id the id to search
     * @return a non-null signed key pair
     * @throws IllegalArgumentException if no element can be found
     */
    public SignalSignedKeyPair findSignedKeyPairById(int id) {
        Validate.isTrue(id == signedKeyPair.id(), "Id mismatch: %s != %s", id, signedKeyPair.id());
        return signedKeyPair;
    }

    /**
     * Queries the trusted key that matches {@code id}
     *
     * @param id the non-null id to search
     * @return a non-null Optional singal pre key
     */
    public Optional<SignalPreKeyPair> findPreKeyById(int id) {
        return preKeys.stream()
                .filter(preKey -> preKey.id() == id)
                .findFirst();
    }

    /**
     * Queries the app state key that matches {@code id}
     *
     * @param id the non-null id to search
     * @return a non-null Optional app state dataSync key
     */
    public Optional<AppStateSyncKey> findAppKeyById(byte[] id) {
        return appStateKeys.stream()
                .filter(preKey -> preKey.keyId() != null && Arrays.equals(preKey.keyId().keyId(), id))
                .findFirst();
    }

    /**
     * Queries the hash state that matches {@code name}.
     * Otherwise, creates a new one.
     *
     * @param name the non-null name to search
     * @return a non-null hash state
     */
    public LTHashState findHashStateByName(@NonNull String name) {
        return Objects.requireNonNullElseGet(hashStates.get(name),
                () -> createHashState(name));
    }

    private LTHashState createHashState(String name) {
        var state = new LTHashState(name);
        hashStates.put(name, state);
        return state;
    }

    /**
     * Checks whether {@code identityKey} is trusted for {@code address}
     *
     * @param address the non-null address
     * @param identityKey the nullable identity key
     * @return true if any match is found
     */
    public boolean hasTrust(@NonNull SessionAddress address, byte[] identityKey) {
        return true; // At least for now
    }

    /**
     * Adds the provided address and record to the known sessions
     *
     * @param address the non-null address
     * @param record the non-null record
     * @return this
     */
    public WhatsappKeys addSession(@NonNull SessionAddress address, @NonNull Session record){
        sessions.put(address, record);
        return this;
    }

    /**
     * Adds the provided name and key record to the known sender keys
     *
     * @param name the non-null name
     * @param record the non-null record
     * @return this
     */
    public WhatsappKeys addSenderKey(@NonNull SenderKeyName name, @NonNull SenderKeyRecord record){
        senderKeys.put(name, record);
        return this;
    }

    public long writeCounter(boolean increment){
        return increment ? writeCounter.getAndIncrement()
                : writeCounter.get();
    }

    public long readCounter(boolean increment){
        return increment ? readCounter.getAndIncrement()
                : readCounter.get();
    }
}