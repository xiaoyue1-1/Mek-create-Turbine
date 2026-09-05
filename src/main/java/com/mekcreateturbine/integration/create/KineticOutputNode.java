package com.mekcreateturbine.integration.create;

/**
 * Create rotational output node (soft dependency - only loaded when Create is present).
 *
 * <p>TODO(content): a standalone {@code GeneratingKineticBlockEntity} placed directly above the
 * roof-centre "coupler" block of our turbine. It is NOT part of the multiblock structure to avoid
 * the single-inheritance clash between Create's BE base and Mek's tile base.
 *
 * <ul>
 *   <li>server tick: read neighbouring coupler tile -> {@code MechanicalTurbineData};</li>
 *   <li>override {@code getGeneratedSpeed()} from our current mechanical output;</li>
 *   <li>publish capacity via {@code updateGeneratedRotation()} /
 *       {@code calculateAddedStressCapacity()};</li>
 *   <li>read actual network load ({@code KineticNetwork}) and write it back into the data as
 *       load-utilisation so MECHANICAL mode throttles steam accordingly.</li>
 * </ul>
 *
 * <p>Reference API (Create 6 / mc1.21.1): see {@code reference/src/create-api/}.
 */
public class KineticOutputNode {
}
