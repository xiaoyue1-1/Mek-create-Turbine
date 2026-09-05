package com.mekcreateturbine.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side tuning.
 *
 * <p>mechanicalStressPerFlowRate: SU (stress units) granted per mB/t of steam actually flowing
 * through the turbine in MECHANICAL mode. Tune the ratio so a given steam flow gives the SU you
 * want. Applies on next change without restart.
 */
public final class MCTConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue MECHANICAL_STRESS_PER_FLOW_RATE =
          BUILDER.comment("SU (Create stress units) per mB/t of steam flow while in MECHANICAL mode.")
                .defineInRange("mechanicalStressPerFlowRate", 2.0, 0.001, 1000000.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MCTConfig() {
    }
}
