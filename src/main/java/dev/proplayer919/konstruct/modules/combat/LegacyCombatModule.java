package dev.proplayer919.konstruct.modules.combat;

import dev.proplayer919.konstruct.CustomInstance;
import dev.proplayer919.konstruct.modules.Module;
import io.github.togar2.pvp.feature.CombatFeatureSet;
import io.github.togar2.pvp.feature.CombatFeatures;

public class LegacyCombatModule extends Module {
    public final CombatFeatureSet legacyCombat = CombatFeatures.legacyVanilla();

    @Override
    public void initialize(CustomInstance parent) {
        parent.eventNode().addChild(legacyCombat.createNode());
    }
}
