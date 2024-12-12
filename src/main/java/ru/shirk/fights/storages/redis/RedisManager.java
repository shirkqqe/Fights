package ru.shirk.fights.storages.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import ru.shirk.fights.Fights;
import ru.shirk.fights.storages.redis.packet.list.CrossServerBroadcastPacket;

public class RedisManager {

    private static final String CHANNEL = "CROSS_SERVER_BROADCAST_FIGHTS";
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    @SuppressWarnings("FieldCanBeLocal")
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;

    public RedisManager() {
        final ConfigurationSection section = Fights.getConfigurationManager().getConfig("settings.yml")
                .getFile().getConfigurationSection("redis");
        if (section == null) {
            throw new IllegalStateException("Redis is not configured!");
        }
        this.client = RedisClient.create("redis://" + section.getString("host") + ":" + section.getString("port"));
        connection = client.connect();
        pubSubConnection = client.connectPubSub();

        pubSubConnection.addListener(new RedisMessageListener() {
            @Override
            public void message(String channel, String message) {
                final CrossServerBroadcastPacket packet = new CrossServerBroadcastPacket(message);

                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage(packet.getMessage());
                });
                Bukkit.getLogger().info("Broadcast from " + packet.getServer() + " | " + packet.getMessage());
            }
        });

        pubSubConnection.async().subscribe(CHANNEL);
    }

    public void unload() {
        client.shutdown();
    }

    public void sendMessage(String server, String message) {
        RedisAsyncCommands<String, String> commands = connection.async();
        final var packet = new CrossServerBroadcastPacket(server, message);
        packet.write();
        commands.publish(CHANNEL, packet.getSource());
    }
}
