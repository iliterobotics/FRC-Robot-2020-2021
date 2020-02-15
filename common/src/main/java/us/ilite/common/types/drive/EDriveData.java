package us.ilite.common.types.drive;

import com.flybotix.hfr.codex.CodexOf;

public enum EDriveData implements CodexOf<Double> {

    // Sensor inputs
    L_ACTUAL_POS_FT, R_ACTUAL_POS_FT,
    L_ACTUAL_VEL_FT_s, R_ACTUAL_VEL_FT_s,
    LEFT_CURRENT, RIGHT_CURRENT,
    LEFT_VOLTAGE, RIGHT_VOLTAGE,
    IS_CURRENT_LIMITING,

    // Outputs from a generated path
    L_PATH_FT_s, R_PATH_FT_s,

    // Commanded Outputs
    LEFT_DEMAND, RIGHT_DEMAND,
    DESIRED_TURN_PCT, DESIRED_THROTTLE_PCT,DESIRED_NEUTRAL_MODE,
    DESIRED_STATE,
//    LEFT_ACCEL, RIGHT_ACCEL,
//    LEFT_FEEDFORWARD, RIGHT_FEEDFORWARD,

    // Raw Outputs
    LEFT_MESSAGE_OUTPUT, RIGHT_MESSAGE_OUTPUT,
    LEFT_MESSAGE_CONTROL_MODE, RIGHT_MESSAGE_CONTROL_MODE,
    LEFT_MESSAGE_NEUTRAL_MODE, RIGHT_MESSAGE_NEUTRAL_MODE;

}
