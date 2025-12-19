package dev.proplayer919.konstruct.bot;

import dev.proplayer919.konstruct.matches.MatchPlayer;
import dev.proplayer919.konstruct.matches.PlayerStatus;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;

import java.util.*;

@Getter
public class BotPlayer extends LivingEntity implements MatchPlayer {
    private final UUID uuid;
    private final String username;
    private final PlayerSkin skin;

    @Setter
    private PlayerStatus playerStatus;

    @Setter
    private boolean frozen;

    private final Collection<Player> botViewers = new HashSet<>();

    private final PlayerInfoUpdatePacket playerViewBotPacket;
    private final PlayerInfoRemovePacket playerRemoveBotPacket;

    public BotPlayer(UUID uuid, String username, PlayerSkin skin, int order) {
        super(EntityType.PLAYER, uuid);

        this.setHealth(20);

        this.uuid = uuid;
        this.username = username;
        this.skin = skin;
        this.playerStatus = PlayerStatus.ALIVE;

        this.playerViewBotPacket = new PlayerInfoUpdatePacket(
                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                new PlayerInfoUpdatePacket.Entry(
                        uuid,
                        username,
                        List.of(
                                new PlayerInfoUpdatePacket.Property(
                                        "textures",
                                        skin.textures(),
                                        skin.signature()
                                )
                        ),
                        true,
                        0,
                        GameMode.SURVIVAL,
                        Component.text(username),
                        null,
                        order,
                        true
                )
        );

        this.playerRemoveBotPacket = new PlayerInfoRemovePacket(uuid);

        this.eventNode().addListener(EntityDeathEvent.class, event -> {
            if (event.getEntity().getUuid().equals(this.uuid)) {
                this.setFireTicks(0);
                this.entityMeta.setOnFire(false);
                this.setHealth(20);
                this.refreshIsDead(false);
                this.updatePose();
            }
        });
    }

    @Override
    public boolean isAlive() {
        return playerStatus == PlayerStatus.ALIVE;
    }

    @Override
    public <T> void sendTitlePart(TitlePart<T> part, T value) {
    }

    @Override
    public void playSound(Sound sound) {
    }

    @Override
    public boolean setGameMode(GameMode gameMode) {
        return false;
    }

    @Override
    public void setEnableRespawnScreen(boolean enable) {
    }

    @Override
    public void setRespawnPoint(Pos respawnPoint) {
    }

    @Override
    public void sendActionBar(Component message) {
    }

    public void addPlayerViewer(Player viewer) {
        viewer.sendPacket(playerViewBotPacket);
        this.updateNewViewer(viewer);
        botViewers.add(viewer);
    }

    public void removePlayerViewer(Player viewer) {
        viewer.sendPacket(playerRemoveBotPacket);
        botViewers.remove(viewer);
    }
}
