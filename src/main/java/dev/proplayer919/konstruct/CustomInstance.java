package dev.proplayer919.konstruct;

import dev.proplayer919.konstruct.modules.Module;
import lombok.Getter;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Getter
public class CustomInstance extends InstanceContainer {
    private final Collection<Module> modules = new ArrayList<>();

    public CustomInstance(UUID uuid, RegistryKey<@NotNull DimensionType> dimensionType) {
        super(uuid, dimensionType);
    }

    public void addModule(Module module) {
        this.modules.add(module);
        module.initialize(this);
    }
}
