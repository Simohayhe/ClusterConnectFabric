package net.simohaya.clusterconnect_fabric;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.FriendlyByteBuf;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public class VelocityForwardingHandler {

    public static PlayerData verifyAndExtract(FriendlyByteBuf buf, String secretKey) throws IOException {
        byte[] receivedHmac = new byte[32];
        buf.readBytes(receivedHmac);

        byte[] signedData = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), signedData);

        byte[] expectedHmac = computeHmac(secretKey.getBytes(StandardCharsets.UTF_8), signedData);
        if (!MessageDigest.isEqual(receivedHmac, expectedHmac)) {
            throw new IOException(
                "Velocity forwarding signature mismatch — " +
                "ensure secret_key in config/clusterconnect.json exactly matches " +
                "forwarding-secret in velocity.toml (no trailing whitespace or newline)"
            );
        }

        int version = buf.readVarInt();
        if (version < 1) {
            throw new IOException("Unsupported Velocity forwarding version: " + version);
        }

        buf.readUtf(Short.MAX_VALUE); // skip remote IP

        UUID uuid = new UUID(buf.readLong(), buf.readLong());
        String username = buf.readUtf(16);

        ArrayListMultimap<String, Property> properties = ArrayListMultimap.create();
        int propertyCount = buf.readVarInt();
        for (int i = 0; i < propertyCount; i++) {
            String name    = buf.readUtf(Short.MAX_VALUE);
            String value   = buf.readUtf(Short.MAX_VALUE);
            boolean hasSig = buf.readBoolean();
            Property prop  = hasSig
                ? new Property(name, value, buf.readUtf(Short.MAX_VALUE))
                : new Property(name, value);
            properties.put(name, prop);
        }

        return new PlayerData(uuid, username, properties);
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
