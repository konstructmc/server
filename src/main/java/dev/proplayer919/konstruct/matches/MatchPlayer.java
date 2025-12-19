package dev.proplayer919.konstruct.matches;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MatchPlayer {
    String getUsername();
    UUID getUuid();
    boolean isAlive();
    CompletableFuture<Void> setInstance(Instance instance);
    CompletableFuture<Void> setInstance(Instance instance, Pos spawnPosition);
    void playSound(Sound sound);
    boolean isFrozen();
    void setFrozen(boolean frozen);
    CompletableFuture<Void> teleport(Pos position);
    <T> void sendTitlePart(TitlePart<T> part, T value);
    boolean setGameMode(GameMode gameMode);
    void setPlayerStatus(PlayerStatus status);
    Pos getPosition();
    void setVelocity(Vec velocity);
    void setEnableRespawnScreen(boolean enable);
    void setRespawnPoint(Pos respawnPoint);
    void sendActionBar(Component message);
}
