package dev.proplayer919.konstruct.modules;

import dev.proplayer919.konstruct.CustomInstance;

public class Module {
    public Class<?>[] getRequiredDependencies() {
        return this.getClass().getAnnotation(ModuleAnnotations.SoftDependsOn.class).value();
    }

    public Class<?>[] getSoftDependencies() {
        return this.getClass().getAnnotation(ModuleAnnotations.SoftDependsOn.class).value();
    }

    public Class<?>[] getIncompatibleModules() {
        return this.getClass().getAnnotation(ModuleAnnotations.IncompatibleWith.class).value();
    }

    public void initialize(CustomInstance parent) {
    }
}
