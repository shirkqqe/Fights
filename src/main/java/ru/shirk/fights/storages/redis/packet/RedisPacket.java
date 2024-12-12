package ru.shirk.fights.storages.redis.packet;

public interface RedisPacket {
    void read();

    void write();
}
