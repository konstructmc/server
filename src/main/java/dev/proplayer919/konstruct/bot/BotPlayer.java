package dev.proplayer919.konstruct.bot;

import dev.proplayer919.konstruct.matches.MatchData;
import dev.proplayer919.konstruct.matches.MatchPlayer;
import dev.proplayer919.konstruct.matches.PlayerStatus;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
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

    private final MatchData matchData;

    private final Collection<Player> botViewers = new HashSet<>();

    private final PlayerInfoUpdatePacket playerViewBotPacket;
    private final PlayerInfoRemovePacket playerRemoveBotPacket;

    private final Random random = new Random();

    public BotPlayer(UUID uuid, String username, PlayerSkin skin, int order, MatchData matchData) {
        super(EntityType.PLAYER, uuid);

        setHealth(20);

        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);

        this.uuid = uuid;
        this.username = username;
        this.skin = skin;
        this.playerStatus = PlayerStatus.ALIVE;
        this.matchData = matchData;

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

        eventNode().addListener(EntityDeathEvent.class, event -> {
            if (event.getEntity().getUuid().equals(uuid)) {
                setFireTicks(0);
                entityMeta.setOnFire(false);
                setHealth(20);
                refreshIsDead(false);
                updatePose();
            }
        });
    }

    public void runBot() {
        // Create a thread to run the bot logic
        new Thread(() -> {
            while (true) {
                try {
                    MatchPlayer nearestPlayer = findNearestPlayer();
                    if (nearestPlayer != null) {
                        Pos playerPos = nearestPlayer.getPosition();

                        // Find the direction that would face the player
                        Pos newPosition = position.add(0, getEyeHeight(), 0).withLookAt(playerPos.withY(playerPos.y() + nearestPlayer.getEyeHeight()));
                        moveTo(newPosition);

                        // Move towards the player
                        float speed = (float) getAttribute(Attribute.MOVEMENT_SPEED).getValue();

                        float deltaX = (float) (newPosition.x() + (newPosition.yaw() * speed));
                        float deltaZ = (float) (newPosition.z() + (newPosition.yaw() * speed));
                        Pos finalPosition = newPosition.withX(deltaX).withZ(deltaZ);
                        moveTo(finalPosition);
                    } else {
                        // No players found, look around randomly
                        float randomYaw = random.nextFloat() * 360;
                        float randomPitch = (random.nextFloat() * 60) - 30; // Pitch between -30 and +30 degrees
                        Pos randomLookPosition = position.add(0, getEyeHeight(), 0).withView(randomYaw, randomPitch);
                        moveTo(randomLookPosition);
                    }

                    Thread.sleep(1000); // Bot logic runs every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "bot-run-thread-" + uuid).start();
    }

    private void moveTo(Pos position) {
        Pos currentPosition = getPosition();
        if (currentPosition.equals(position)) {
            return;
        }

        refreshPosition(currentPosition);

        if (currentPosition.sameView(position)) {
            return;
        }

        setView(position.yaw(), position.pitch());
    }

    private MatchPlayer findNearestPlayer() {
        float minDistance = Float.MAX_VALUE;
        MatchPlayer nearestPlayer = null;
        for (MatchPlayer player : matchData.getPlayers()) {
            float distance = (float) getPosition().distance(player.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                nearestPlayer = player;
            }
        }
        return nearestPlayer;
    }

    public void addPlayerViewer(Player viewer) {
        viewer.sendPacket(playerViewBotPacket);
        updateNewViewer(viewer);
        botViewers.add(viewer);
    }

    public void removePlayerViewer(Player viewer) {
        viewer.sendPacket(playerRemoveBotPacket);
        botViewers.remove(viewer);
    }

    public void sendPacketToViewers(SendablePacket packet) {
        for (Player viewer : botViewers) {
            viewer.sendPacket(packet);
        }
    }

    @Override
    protected void updatePose() {
        EntityPose oldPose = getPose();
        EntityPose newPose;

        // Figure out their expected state
        var meta = getEntityMeta();
        if (meta.isFlyingWithElytra()) {
            newPose = EntityPose.FALL_FLYING;
        } else if (meta instanceof LivingEntityMeta livingMeta && livingMeta.getBedInWhichSleepingPosition() != null) {
            newPose = EntityPose.SLEEPING;
        } else if (meta.isSwimming()) {
            newPose = EntityPose.SWIMMING;
        } else if (meta instanceof LivingEntityMeta livingMeta && livingMeta.isInRiptideSpinAttack()) {
            newPose = EntityPose.SPIN_ATTACK;
        } else if (isSneaking()) {
            newPose = EntityPose.SNEAKING;
        } else {
            newPose = EntityPose.STANDING;
        }

        // Try to put them in their expected state, or the closest if they don't fit.
        if (canFitWithBoundingBox(newPose)) {
            // Use expected state
        } else if (canFitWithBoundingBox(EntityPose.SNEAKING)) {
            newPose = EntityPose.SNEAKING;
        } else if (canFitWithBoundingBox(EntityPose.SWIMMING)) {
            newPose = EntityPose.SWIMMING;
        } else {
            // If they can't fit anywhere, just use standing
            newPose = EntityPose.STANDING;
        }

        if (newPose != oldPose) setPose(newPose);
    }

    private boolean canFitWithBoundingBox(EntityPose pose) {
        BoundingBox bb = pose == EntityPose.STANDING ? boundingBox : BoundingBox.fromPose(pose);
        if (bb == null) return false;

        var position = getPosition();
        var iter = bb.getBlocks(getPosition());
        while (iter.hasNext()) {
            var pos = iter.next();
            Block block;
            try {
                block = instance.getBlock(pos.blockX(), pos.blockY(), pos.blockZ(), Block.Getter.Condition.TYPE);
            } catch (NullPointerException ignored) {
                block = null;
            }

            // Block was in unloaded chunk, no bounding box.
            if (block == null) continue;

            // For now just ignore scaffolding. It seems to have a dynamic bounding box, or is just parsed
            // incorrectly in MinestomDataGenerator.
            if (block.id() == Block.SCAFFOLDING.id()) continue;

            var hit = block.registry().collisionShape()
                    .intersectBox(position.sub(pos.blockX(), pos.blockY(), pos.blockZ()), bb);
            if (hit) return false;
        }

        return true;
    }

    @Override
    public double getEyeHeight() {
        return switch (getPose()) {
            case SLEEPING -> 0.2;
            case FALL_FLYING, SWIMMING, SPIN_ATTACK -> 0.4;
            case SNEAKING -> 1.27;
            default -> 1.62;
        };
    }

    protected void refreshAfterTeleport() {
        sendPacketToViewers(getSpawnPacket());
        sendPacketToViewers(getVelocityPacket());
        sendPacketToViewers(getMetadataPacket());
        sendPacketToViewers(getPropertiesPacket());
        sendPacketToViewers(getEquipmentsPacket());
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
}
