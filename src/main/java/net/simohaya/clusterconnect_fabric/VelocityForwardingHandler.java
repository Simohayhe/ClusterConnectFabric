package net.simohaya.clusterconnect_fabric;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.PacketByteBuf;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public class VelocityForwardingHandler {

    /**
     * Wire format sent by Velocity Modern Forwarding (confirmed against Velocity source):
     *
     *   [32 bytes] HMAC-SHA256
     *   [varint]   forwarding version  ← part of the signed data
     *   [string]   remote IP address
     *   [long]     UUID most significant bits
     *   [long]     UUID least significant bits
     *   [string]   username
     *   [varint]   property count
     *     [string]   name
     *     [string]   value
     *     [bool]     has signature
     *     [string?]  signature (only present when has_signature == true)
     *   (versions 2+ may append key/signing data — we ignore anything past properties)
     *
     * The HMAC covers EVERYTHING after the 32-byte signature field,
     * i.e. HMAC(secret, [version_varint + IP + UUID + username + properties...])
     */
    public static GameProfile verifyAndExtract(PacketByteBuf buf, String secretKey) throws IOException {
        // --- read HMAC (32 bytes) — comes first ---
        byte[] receivedHmac = new byte[32];
        buf.readBytes(receivedHmac);

        // --- snapshot the signed portion without advancing the reader ---
        byte[] signedData = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), signedData);

        // --- verify HMAC(secret, signedData) ---
        byte[] expectedHmac = computeHmac(secretKey.getBytes(StandardCharsets.UTF_8), signedData);
        if (!MessageDigest.isEqual(receivedHmac, expectedHmac)) {
            throw new IOException(
                "Velocity forwarding signature mismatch — " +
                "ensure secret_key in config/clusterconnect.json exactly matches " +
                "forwarding-secret in velocity.toml (no trailing whitespace or newline)"
            );
        }

        // --- parse signed data ---
        // version VarInt (1 = basic modern, 2-4 = with player key; we support all)
        int version = buf.readVarInt();
        if (version < 1) {
            throw new IOException("Unsupported Velocity forwarding version: " + version);
        }

        // skip remote IP
        buf.readString(Short.MAX_VALUE);

        // UUID (two big-endian longs)
        UUID uuid = new UUID(buf.readLong(), buf.readLong());

        // username (Minecraft caps at 16 chars)
        String username = buf.readString(16);

        // properties — the "textures" entry is what makes skins visible to others
        ArrayListMultimap<String, Property> multimap = ArrayListMultimap.create();
        int propertyCount = buf.readVarInt();
        for (int i = 0; i < propertyCount; i++) {
            String name    = buf.readString(Short.MAX_VALUE);
            String value   = buf.readString(Short.MAX_VALUE);
            boolean hasSig = buf.readBoolean();
            Property prop  = hasSig
                ? new Property(name, value, buf.readString(Short.MAX_VALUE))
                : new Property(name, value);
            multimap.put(name, prop);
        }
        // any remaining bytes (key data for version 2+) are intentionally ignored

        return new GameProfile(uuid, username, new PropertyMap(multimap));
    }

    private static byte[] computeHmac(byte[] key, byte[] data) throws IOException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            mac.update(data);
            return mac.doFinal();
        } catch (Exception e) {
            throw new IOException("Failed to compute HMAC-SHA256", e);
        }
    }
}
