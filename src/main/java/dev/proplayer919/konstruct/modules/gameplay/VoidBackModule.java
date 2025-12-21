package dev.proplayer919.konstruct.modules.gameplay;

import dev.proplayer919.konstruct.CustomInstance;
import dev.proplayer919.konstruct.messages.MessageType;
import dev.proplayer919.konstruct.messages.MessagingHelper;
import dev.proplayer919.konstruct.modules.Module;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;

public class VoidBackModule extends Module {
    private final Pos spawnPos;

    public VoidBackModule() {
        this.spawnPos = new Pos(0.5, 40, 0.5);
    }

    public VoidBackModule(Pos spawnPos) {
        this.spawnPos = spawnPos;
    }

    @Override
    public void initialize(CustomInstance parent) {
        parent.eventNode().addListener(PlayerMoveEvent.class, event -> {
            if (event.getNewPosition().y() < 0) {
                Player player = event.getPlayer();
                player.teleport(spawnPos);
                player.setVelocity(Vec.ZERO);
                MessagingHelper.sendMessage(player, MessageType.SERVER, "You fell into the abyss, teleporting you back to spawn.");
            }
        });
    }
}
