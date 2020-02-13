package us.ilite.robot.controller;

import com.flybotix.hfr.util.lang.EnumUtils;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import us.ilite.common.config.InputMap;
import us.ilite.common.types.EColorData;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.common.Field2020;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.robot.Robot;
import us.ilite.robot.modules.*;
import us.ilite.common.types.EHangerModuleData;

import static us.ilite.common.types.ELimelightData.*;
import static us.ilite.common.types.EPowerCellData.*;

import java.util.List;

public class TestController extends BaseManualController {

    private ILog mLog = Logger.createLog(TestController.class);

    private Double mLastTargetTrackingType = 0d;
    private Double mLastGroundTrackingType = 0d;

    private double mLimelightZoomThreshold = 7.0;

    private PowerCellModule.EIntakeState mIntakeState;
    private PowerCellModule.EArmState mArmState;
    private double mPreviousTime;
    private double mGoalBeamCountBroken = 0;
    private boolean crossedEntry = false;
    private Boolean allBeamsBroken = false;


    private static TestController INSTANCE;

    public static TestController getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TestController();
        }
        return INSTANCE;
    }

    private TestController() {
        for(String key : db.mMappedCodex.keySet()) {
            ShuffleboardTab tab = Shuffleboard.getTab("TEST-" + key);
            List<Enum<?>> enums =  EnumUtils.getEnums(db.mMappedCodex.get(key).meta().getEnum(), true);
            enums.stream().forEach(
                    e -> {
                        tab.addNumber(e.name(), ()->{
                            if(db.mMappedCodex.get(key).isSet(e)) {
                                return db.mMappedCodex.get(key).get(e);
                            } else {
                                return 0d;
                            }
                        });
                    }
            );
        }
    }

    protected void updateImpl(double pNow) {
        // ========================================
        // DO NOT COMMENT OUT THESE METHOD CALLS
        // ========================================
        Robot.CLOCK.report("updateLimelightTargetLock", t-> updateTargetTracking(pNow));
        Robot.CLOCK.report("updateDrivetrain", t->updateDrivetrain(pNow));
        Robot.CLOCK.report("updateFlywheel", t->updateFlywheel(pNow));
        Robot.CLOCK.report("updateIntake", t-> updatePowerCells(pNow));
        Robot.CLOCK.report("updateHanger", t->updateHanger(pNow));
        Robot.CLOCK.report("updateDJBooth", t->updateDJBooth());
//        updateArm(pNow);
    }

    private void updateHanger(double pNow){
        if(db.operatorinput.isSet(InputMap.OPERATOR.BEGIN_HANG)){
            Robot.DATA.hanger.set(EHangerModuleData.DESIRED_POSITION , 17.0);
        }
        else {
            Robot.DATA.hanger.set(EHangerModuleData.DESIRED_POSITION, 0.0);
        }

    }

    void updateFlywheel(double pNow) {
//        if (!db.driverinput.isSet(InputMap.DRIVER.FLYWHEEL_AXIS) ) {
//            mTurretMode = FlywheelModule.ETurretMode.LIMELIGHT;
//            mHoodState = FlywheelModule.EHoodState.ADJUSTABLE;
//            mAcceleratorState = FlywheelModule.EAcceleratorState.FEED;
//            mShooterState = FlywheelModule.EShooterState.SHOOT;
//        }
//        else {
//            mTurretMode = FlywheelModule.ETurretMode.GYRO;
//            mAcceleratorState = FlywheelModule.EAcceleratorState.STOP;
//            mShooterState = FlywheelModule.EShooterState.STOP;
//            mHoodState = FlywheelModule.EHoodState.STATIONARY;
//        }
//
//        switch(mAcceleratorState) {
//            case FEED: db.flywheel.set(EShooterSystemData.CURRENT_ACCELERATOR_VELOCITY, FlywheelModule.kAcceleratorTargetVelocity);
//                break;
//            case STOP: db.flywheel.set(EShooterSystemData.CURRENT_ACCELERATOR_VELOCITY, 0.0);
//                break;
//        }
//
//        switch(mTurretMode) {
//            case GYRO: db.flywheel.set(EShooterSystemData.TARGET_TURRET_VELOCITY, mTurretPid.calculate(-2 * mTurretGyro.getCompassHeading(), pNow - mPreviousTime));
//                break;
//            case LIMELIGHT:
//                if ( db.limelight.isSet(ETargetingData.tx)) {
//                    db.flywheel.set(EShooterSystemData.TARGET_TURRET_VELOCITY, mTurretPid.calculate(-10 * db.limelight.get(ETargetingData.tx), pNow - mPreviousTime));
//                }
//                break;
//        }
//
//        switch(mShooterState) {
//            case SHOOT:
//                if ( db.limelight.isSet(ETargetingData.ty)) {
//                    db.flywheel.set(EShooterSystemData.TARGET_FLYWHEEL_VELOCITY, mShooter.calcSpeedFromDistance(db.limelight.get(ETargetingData.calcDistToTarget)));
//                }
//                else {
//                    db.flywheel.set(EShooterSystemData.TARGET_FLYWHEEL_VELOCITY, mShooterPid.calculate(Settings.ShooterSystem.kShooterTargetVelocity, 0.5));
//                }
//                break;
//            case STOP: db.flywheel.set(EShooterSystemData.TARGET_FLYWHEEL_VELOCITY, 0.0);
//                break;
//        }
//
//        switch(mHoodState) {
//            case STATIONARY: db.flywheel.set(EShooterSystemData.TARGET_HOOD_ANGLE, Settings.ShooterSystem.kBaseHoodAngle);
//                break;
//            case ADJUSTABLE:
//                if (db.limelight.isSet(ETargetingData.ty)) {
//                    db.flywheel.set(EShooterSystemData.TARGET_HOOD_ANGLE, mShooter.calcAngleFromDistance(db.limelight.get(ETargetingData.calcDistToTarget), db.limelight.get(ETargetingData.ty)));
//                }
//                else {
//                    db.flywheel.set(EShooterSystemData.TARGET_HOOD_ANGLE, Settings.ShooterSystem.kBaseHoodAngle);
//                }
//        }
//        if ( db.attackoperatorinput.isSet(ELogitechAttack3.TRIGGER)) {
//            mAccelerator.set(ControlMode.PercentOutput, db.attackoperatorinput.get(ELogitechAttack3.TRIGGER));
//        }
//        mPreviousTime = pNow;
//        mLog.error("-------------------------------------------------------Flywheel Velocity: ", db.flywheel.get(EShooterSystemData.CURRENT_FLYWHEEL_VELOCITY));
    }

    public void updateTargetTracking(double pNow) {
        if (Robot.DATA.driverinput.isSet(InputMap.DRIVER.DRIVER_LIMELIGHT_LOCK_TARGET)) {
            Robot.DATA.limelight.set(TARGET_ID, (Field2020.FieldElement.TARGET.id()));
        } else if (Robot.DATA.driverinput.isSet(InputMap.DRIVER.DRIVER_LIMELIGHT_LOCK_TARGET_ZOOM)) {
            if (Robot.DATA.limelight.isSet(TY)) {
                if (Math.abs(Robot.DATA.limelight.get(TX)) < mLimelightZoomThreshold) {
                    Robot.DATA.limelight.set(TARGET_ID, Field2020.FieldElement.TARGET_ZOOM.id());
                }
            } else {
                Robot.DATA.limelight.set(TARGET_ID, Field2020.FieldElement.TARGET.id());
            }
        } else {
            Robot.DATA.limelight.set(TARGET_ID, Limelight.NONE.id());
        }
        if ((Robot.DATA.limelight.get(TARGET_ID.ordinal()) != (mLastTargetTrackingType))
                && !(Robot.DATA.limelight.get(TARGET_ID.ordinal()) == Limelight.NONE.id())) {
            //TargetLock(); something to do with targetlock here, need clarification on command structure
        }
        mLastTargetTrackingType = Robot.DATA.limelight.get(TARGET_ID.ordinal());
    }

    public void updateGroundTracking(double pNow) {
        if (Robot.DATA.driverinput.isSet(InputMap.DRIVER.DRIVER_LIMELIGHT_LOCK_BALL)) {
            Robot.DATA.groundTracking.set(TARGET_ID, Field2020.FieldElement.BALL.id());
        } else if (Robot.DATA.driverinput.isSet(InputMap.DRIVER.DRIVER_LIMELIGHT_LOCK_BALL_DUAL)) {
            Robot.DATA.groundTracking.set(TARGET_ID, Field2020.FieldElement.BALL_DUAL.id());
        } else if (Robot.DATA.driverinput.isSet(InputMap.DRIVER.DRIVER_LIMELIGHT_LOCK_BALL_TRI)) {
            Robot.DATA.groundTracking.set(TARGET_ID, Field2020.FieldElement.BALL_TRI.id());
        } else {
            Robot.DATA.groundTracking.set(TARGET_ID, RawLimelight.NONE.id());
        }
        if ((Robot.DATA.groundTracking.get(TARGET_ID.ordinal()) != (mLastGroundTrackingType))
                && !(Robot.DATA.groundTracking.get(TARGET_ID.ordinal()) == RawLimelight.NONE.id())) {
            //TargetLock(); something to do with targetlock here, need clarification on command structure
        }
        mLastGroundTrackingType = Robot.DATA.limelight.get(TARGET_ID.ordinal());
    }

    protected void updatePowerCells(double pNow) {
        // Practice bot testing, min power
        // Power of 1.0 was waaaaay too fast
        if(db.operatorinput.isSet(ELogitech310.L_BTN)) {
            db.powercell.set(UNUSED, 0.2);
        }

        if (db.operatorinput.isSet(InputMap.OPERATOR.INTAKE)) {
            setIntakeArmEnabled(pNow, true);
            crossedEntry = activateSerializer(pNow);
//            if (crossedEntry && !db.powercell.isSet(EPowerCellData.SECONDARY_BREAM_BREAKER)) {
//                db.powercell.set(EPowerCellData.DESIRED_H_VELOCITY, power);
//                db.powercell.set(EPowerCellData.DESIRED_V_VELOCITY, power);
//            } else if ( db.powercell.isSet(EPowerCellData.SECONDARY_BREAM_BREAKER) ) {
//                crossedEntry = false;
//            }

        } else if (db.operatorinput.isSet(InputMap.OPERATOR.REVERSE_INTAKE)) {
            reverseSerializer(pNow);
        } else if (db.operatorinput.isSet(InputMap.OPERATOR.STOW_INTAKE)){
            setIntakeArmEnabled(pNow, false);
            crossedEntry = activateSerializer(pNow);
        }




//        if (db.operatorinput.isSet(InputMap.OPERATOR.INTAKE)) {
////            db.powercell.set(EPowerCellData.DESIRED_V_VELOCITY, PowerCellModule.EIntakeState.INTAKE.getPower());
////            db.powercell.set(EPowerCellData.DESIRED_H_VELOCITY, PowerCellModule.EIntakeState.INTAKE.getPower());
////            db.powercell.set(EPowerCellData.DESIRED_INTAKE_VELOCITY, PowerCellModule.EIntakeState.INTAKE.getPower());
//
////            db.powercell.set(EPowerCellData.DESIRED_ARM_ANGLE, mArmState.getAngle()); TODO Commented cuz we don't have an arm
////            if (db.powercell.get(EPowerCellData.CURRENT_AMOUNT_OF_SENSORS_BROKEN) != db.powercell.get(EPowerCellData.DESIRED_AMOUNT_OF_SENSORS_BROKEN)){
//                if (db.powercell.get(EPowerCellData.ALL_BEAMS_BROKEN) == 0.0) {
//                    db.powercell.set(EPowerCellData.DESIRED_H_VELOCITY, PowerCellModule.EIntakeState.INTAKE.getPower());
//                    db.powercell.set(EPowerCellData.DESIRED_V_VELOCITY, PowerCellModule.EIntakeState.INTAKE.getPower());
//                    db.powercell.set(EPowerCellData.DESIRED_INTAKE_VELOCITY, PowerCellModule.EIntakeState.INTAKE.getPower());
//                    db.powercell.set(EPowerCellData.DESIRED_INTAKE_VELOCITY_FT_S, db.drivetrain.get(LEFT_VEL_IPS) + PowerCellModule.kDeltaIntakeVel);
//                } else {
//                    db.powercell.set(EPowerCellData.DESIRED_H_VELOCITY, PowerCellModule.EIntakeState.STOP.getPower());
//                    db.powercell.set(EPowerCellData.DESIRED_V_VELOCITY, PowerCellModule.EIntakeState.STOP.getPower());
//                    db.powercell.set(EPowerCellData.DESIRED_INTAKE_VELOCITY, PowerCellModule.EIntakeState.STOP.getPower());
//                    db.powercell.set(EPowerCellData.DESIRED_INTAKE_VELOCITY_FT_S, PowerCellModule.EIntakeState.STOP.getPower());
//                }
////            }
//
//        } else if (db.operatorinput.isSet(InputMap.OPERATOR.REVERSE_INTAKE)) {
//            db.powercell.set(EPowerCellData.DESIRED_V_VELOCITY , PowerCellModule.EIntakeState.REVERSE.getPower());
//            db.powercell.set(EPowerCellData.DESIRED_H_VELOCITY , PowerCellModule.EIntakeState.REVERSE.getPower());
//            db.powercell.set(EPowerCellData.DESIRED_INTAKE_VELOCITY , PowerCellModule.EIntakeState.REVERSE.getPower());
//            db.powercell.set(EPowerCellData.DESIRED_ARM_ANGLE, mArmState.getAngle());
////
////            if (db.powercell.get(EPowerCellData.CURRENT_AMOUNT_OF_SENSORS_BROKEN) != db.powercell.get(EPowerCellData.DESIRED_AMOUNT_OF_SENSORS_BROKEN)){
////                if ( db.powercell.get(EPowerCellData.CURRENT_AMOUNT_OF_SENSORS_BROKEN) < mGoalBeamCountBroken) {
////                    db.powercell.set(EPowerCellData.DESIRED_H_VELOCITY, PowerCellModule.EIntakeState.INTAKE.getPower());
////                    db.powercell.set(EPowerCellData.DESIRED_V_VELOCITY, PowerCellModule.EIntakeState.INTAKE.getPower());
////                    db.powercell.se   t(EPowerCellData.DESIRED_INTAKE_VELOCITY, PowerCellModule.EIntakeState.INTAKE.getPower());
////
////                } else {
////                    db.powercell.set(EPowerCellData.DESIRED_H_VELOCITY, PowerCellModule.EIntakeState.STOP.getPower());
////                    db.powercell.set(EPowerCellData.DESIRED_V_VELOCITY, PowerCellModule.EIntakeState.STOP.getPower());
////                    db.powercell.set(EPowerCellData.DESIRED_INTAKE_VELOCITY, PowerCellModule.EIntakeState.STOP.getPower());
////                }
////            }
//
//        } else {
//            mIntakeState = PowerCellModule.EIntakeState.STOP;
//            mArmState = PowerCellModule.EArmState.DISENGAGED;
//            db.powercell.set(EPowerCellData.DESIRED_ARM_ANGLE, mArmState.getAngle());
//            db.powercell.set(EPowerCellData.DESIRED_H_VELOCITY, mIntakeState.getPower());
//            db.powercell.set(EPowerCellData.DESIRED_V_VELOCITY, mIntakeState.getPower());
//            db.powercell.set(EPowerCellData.DESIRED_INTAKE_VELOCITY_FT_S, db.drivetrain.get(LEFT_VEL_IPS) + PowerCellModule.kDeltaIntakeVel);
//        }

    }

    void updateDJBooth() {
        if ( db.operatorinput.isSet(InputMap.OPERATOR.OPERATOR_POSITION_CONTROL)) {
            DJSpinnerModule.EColorMatch m = db.color.get(EColorData.SENSED_COLOR, DJSpinnerModule.EColorMatch.class);
            if(m != null && m.color.equals(db.DJ_COLOR)) {
                db.color.set(EColorData.DESIRED_MOTOR_POWER, DJSpinnerModule.EColorWheelState.OFF.power);
            } else {
                db.color.set(EColorData.DESIRED_MOTOR_POWER, DJSpinnerModule.EColorWheelState.POSITION.power);
                db.color.set(EColorData.COLOR_WHEEL_MOTOR_STATE, (double) DJSpinnerModule.EColorWheelState.POSITION.ordinal());
            }
        }

    }
}


//    void updateDJBooth() {
//        if ( db.operatorinput.isSet(InputMap.OPERATOR.OPERATOR_POSITION_CONTROL)) {
//            DJSpinnerModule.EColorMatch m =db.color.get(EColorData.SENSED_COLOR, DJSpinnerModule.EColorMatch.class);
//            if(m.color.equals(db.DJ_COLOR)) {
//                db.color.set(EColorData.DESIRED_MOTOR_POWER, DJSpinnerModule.EColorWheelState.OFF.power);
//            } else {
//                db.color.set(EColorData.DESIRED_MOTOR_POWER, DJSpinnerModule.EColorWheelState.POSITION.power);
//                db.color.set(EColorData.COLOR_WHEEL_MOTOR_STATE, (double) DJSpinnerModule.EColorWheelState.POSITION.ordinal());
//            }
//        }
//
//        if ( db.operatorinput.isSet(InputMap.OPERATOR.OPERATOR_ROTATION_CONTROL)) {
//
//            if(db.color.get(EColorData.WHEEL_ROTATION_COUNT) >= DJSpinnerModule.sTARGET_ROTATION_COUNT) {
//                db.color.set(EColorData.COLOR_WHEEL_MOTOR_STATE, (double) DJSpinnerModule.EColorWheelState.OFF.ordinal());
//                db.color.set(EColorData.DESIRED_MOTOR_POWER, DJSpinnerModule.EColorWheelState.OFF.power);
//            } else {
//                db.color.set(EColorData.COLOR_WHEEL_MOTOR_STATE, (double) DJSpinnerModule.EColorWheelState.ROTATION.ordinal());
//                db.color.set(EColorData.DESIRED_MOTOR_POWER, DJSpinnerModule.EColorWheelState.ROTATION.power);
//            }
//        }
//
//
//
//        if ( db.operatorinput.isSet(InputMap.OPERATOR.OPERATOR_POSITION_CONTROL) &&
//                db.operatorinput.isSet(InputMap.OPERATOR.OPERATOR_ROTATION_CONTROL) ) {
//            db.color.set(EColorData.POSITION_CONTROL_INPUT, (double)EColorData.EInput.NEGATIVE.ordinal());
//            db.color.set(EColorData.ROTATION_CONTROL_INPUT, (double)EColorData.EInput.NEGATIVE.ordinal());
//        }
//        else if (db.operatorinput.isSet(InputMap.OPERATOR.OPERATOR_POSITION_CONTROL)) {
//            db.color.set(EColorData.POSITION_CONTROL_INPUT, (double)EColorData.EInput.POSITIVE.ordinal());
//            if (db.color.get(EColorData.COLOR_WHEEL_MOTOR_STATE).equals(EColorData.EMotorState.ON.ordinal()) ) {
//                mVictor.set(ControlMode.PercentOutput, Settings.kDJBoothOuput );
//            }
//            else {
//                mVictor.set(ControlMode.PercentOutput, 0d );
//            }
//        }
//        else if (db.operatorinput.isSet(InputMap.OPERATOR.OPERATOR_ROTATION_CONTROL) ) {
//            db.color.set(EColorData.ROTATION_CONTROL_INPUT, (double)EColorData.EInput.POSITIVE.ordinal());
//            if (db.color.get(EColorData.COLOR_WHEEL_MOTOR_STATE).equals(EColorData.EMotorState.ON.ordinal()) ) {
//                mVictor.set(ControlMode.PercentOutput, Settings.kDJBoothOuput );
//            }
//            else {
//                mVictor.set(ControlMode.PercentOutput, 0d );
//            }
//        }
//        else {
//            db.color.set(EColorData.POSITION_CONTROL_INPUT, (double)EColorData.EInput.NEGATIVE.ordinal());
//            db.color.set(EColorData.ROTATION_CONTROL_INPUT, (double)EColorData.EInput.NEGATIVE.ordinal());
//        }

//    }
//
//    public void updateArm(double pNow) {
//        if (db.operatorinput.isSet(InputMap.OPERATOR.HIGHER_ARM)) {
//            mArmState = PowerCellModule.EArmState.ENGAGED;
//        } else {
//            mArmState = PowerCellModule.EArmState.DISENGAGED;
//        }
//        switch (mArmState) {
//                db.powercell.set(EPowerCellData.DESIRED_ARM_STATE , 1.0);
//                break;
//            case DISENGAGED:
//                db.powercell.set(EPowerCellData.DESIRED_ARM_STATE , 0.0);
//                break;
//        }
//        TODO default state
//    }

