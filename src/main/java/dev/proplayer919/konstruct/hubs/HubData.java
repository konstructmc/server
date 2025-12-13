package dev.proplayer919.konstruct.hubs;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.Collection;
import java.util.HashSet;

@Getter
public class HubData {
    @Setter
    private Collection<Player> players = new HashSet<>();

    private final Instance instance;
    private final String id;

    public HubData(Instance instance, String id) {
        this.instance = instance;
        this.id = id;
    }
}
