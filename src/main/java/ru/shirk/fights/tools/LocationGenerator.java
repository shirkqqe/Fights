package ru.shirk.fights.tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class LocationGenerator {

    private static final Random random = new Random();

    public static CompletableFuture<Location> generateRandomLocationAsync(int radius, Collection<Material> blockList, World world) {
        return CompletableFuture.supplyAsync(() -> {
            int centerX = world.getSpawnLocation().getBlockX();
            int centerZ = world.getSpawnLocation().getBlockZ();

            while (true) {
                int x = centerX + random.nextInt(radius * 2) - radius;
                int z = centerZ + random.nextInt(radius * 2) - radius;

                int y = world.getHighestBlockYAt(x, z);
                Location location = new Location(world, x, y, z);
                Material highestBlockType = location.getBlock().getType();

                if (!blockList.contains(highestBlockType)) {
                    return location.add(0, 1, 0);
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
