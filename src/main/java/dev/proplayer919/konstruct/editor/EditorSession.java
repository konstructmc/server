package dev.proplayer919.konstruct.editor;

import lombok.Getter;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.Collection;
import java.util.HashSet;

@Getter
public final class EditorSession {
    private final Instance instance;
    private final Player host;
    private final Collection<Player> players = new HashSet<>();

    public EditorSession(Instance instance, Player host) {
        this.instance = instance;
        this.host = host;

        this.players.add(host);
    }

    public void save() {
        instance.saveChunksToStorage();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }
}
