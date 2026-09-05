package com.mekcreateturbine.common.content.mode;

/**
 * Turbine output mode.
 *
 * <p>ELECTRICAL behaves like the stock Mekanism industrial turbine: steam is converted to
 * energy stored in an internal energy container and pushed out of valves.
 *
 * <p>MECHANICAL converts the same steam budget into Create rotational force. In this mode the
 * turbine keeps no energy buffer, therefore it can never output electricity.
 */
public enum OutputMode {
    ELECTRICAL,
    MECHANICAL
}
