package us.ilite.common.types;

import com.flybotix.hfr.codex.CodexOf;

public enum EPowerCellData implements CodexOf<Double> {
    UNUSED,

    BREAK_SENSOR_0_STATE,
    BREAK_SENSOR_1_STATE,
    BREAK_SENSOR_2_STATE,

    // etc - 1 per beam break sensor

    DESIRED_H_VELOCITY,
    CURRENT_H_VELOCITY,
    DESIRED_V_VELOCITY,
    CURRENT_V_VELOCITY,

    DESIRED_INTAKE_VELOCITY, //Talon
    CURRENT_INTAKE_VELOCITY,

    CURRENT_ARM_ANGLE,
    DESIRED_ARM_ANGLE,

    CURRENT_INTAKE_VELOCITY_FT_S,
    DESIRED_INTAKE_VELOCITY_FT_S,

    DESIRED_INDEXING_STATE,
    CURRENT_INDEXING_STATE;
}
