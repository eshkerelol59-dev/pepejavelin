package tech.javelin.client.modules.impl.combat.aura;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tech.javelin.client.modules.impl.combat.aura.rotation.*;

@Getter
@RequiredArgsConstructor
public enum RotationType {

    FUNTIME("FunTime", new FunTimeRotation()),
    SPOOKYTIME("SpookyTime", new SpookyTimeRotation()),
    SPOOKYTIMEV2("SpookyTimeV2", new SpookytimeV2Rotation()),
    INTPOL("Intpol", new IntpolRotation()),
    SLIMEWORLD("SlimeWorld", new SlimeWorldRotation()),
    REALLYWORLD("ReallyWorld", new ReallyWorldRotation()),
    LONYGRIEF("LonyGrief", new LonyGriefRotation()),
    HVH("HvH", new HvHRotation()),
    WELLMINE("Shard", new WellMineRotation()),
    SLOTH("Sloth", new SlothRotation()),
    LEGIT("Legit", new LegitRotation());

    private final String name;
    private final RotationAura rotation;

}
