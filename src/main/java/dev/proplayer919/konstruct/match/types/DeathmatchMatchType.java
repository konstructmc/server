package dev.proplayer919.konstruct.match.types;

import dev.proplayer919.konstruct.generators.InstanceCreator;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import java.nio.file.Path;

public class DeathmatchMatchType extends MatchType {
    public DeathmatchMatchType() {
        int maxPlayers = 16;

        // Use the correct path to the anvil world folder inside the project
        String anvilPath = Path.of("data", "arenas", "deathmatch1").toString();
        Instance instance = InstanceCreator.createInstanceFromAnvil(anvilPath, true);

        super("deathmatch", "Deathmatch", maxPlayers, 4, new Pos(0.5, 60, 0.5), new Pos(0.5, 80, 0.5), instance);
    }
}
