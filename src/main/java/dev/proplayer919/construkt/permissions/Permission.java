package dev.proplayer919.construkt.permissions;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Permission {
    private final UUID uuid;
    private final String permissionNode;

    public Permission(String permissionNode) {
        this.uuid = UUID.randomUUID();
        this.permissionNode = permissionNode;
    }
}
