package ru.shirk.fights.storages.redis.packet.list;

import com.google.gson.*;
import lombok.Getter;
import lombok.extern.java.Log;
import ru.shirk.fights.storages.redis.packet.AbstractRedisPacket;

import javax.annotation.Nullable;

@Getter
@Log
public class CrossServerBroadcastPacket extends AbstractRedisPacket {

    private String server;
    private String message;

    public CrossServerBroadcastPacket(String server, String message) {
        super(null);
        this.server = server;
        this.message = message;
    }

    public CrossServerBroadcastPacket() {
        this(null);
    }

    public CrossServerBroadcastPacket(@Nullable String source) {
        super(source);
    }

    @Override
    public void read() {
        try {
            final JsonElement element = new JsonParser().parse(getSource());
            if (!element.isJsonObject()) return;
            final JsonObject object = element.getAsJsonObject();

            this.server = object.get("server").getAsString();
            this.message = object.get("message").getAsString();
        } catch (JsonSyntaxException e) {
            log.warning("Packet parse error: "+ e.getMessage());
        }
    }

    @Override
    public void write() {
        JsonObject jo = new JsonObject();
        jo.add("server", new JsonPrimitive(server));
        jo.add("message", new JsonPrimitive(message));
        this.setSource(jo.toString());
    }
}
