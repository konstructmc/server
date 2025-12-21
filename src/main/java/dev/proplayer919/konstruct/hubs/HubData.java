package dev.proplayer919.konstruct.hubs;

import dev.proplayer919.konstruct.CustomInstance;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.entity.Player;

import java.util.Collection;
import java.util.HashSet;

@Getter
public class HubData {
    @Setter
    private Collection<Player> players = new HashSet<>();

    private final CustomInstance instance;
    private final String id;

    public HubData(CustomInstance instance, String id) {
        this.instance = instance;
        this.id = id;
    }
}
