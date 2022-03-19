package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import us.ilite.common.config.Settings;
import us.ilite.common.lib.control.PIDController;
import us.ilite.common.lib.control.ProfileGains;
import us.ilite.common.types.EClimberModuleData;
import us.ilite.common.types.EMatchMode;
import us.ilite.robot.Enums;

import static us.ilite.common.types.EClimberModuleData.*;

public class ClimberModule extends Module{
    private TalonFX mCL12;
    private TalonFX mCLMR11;
    private DoubleSolenoid mCLPNDouble;
    private DoubleSolenoid mCLPNSingle;

    private PIDController mPositionPID;
    private PIDController mVelocityPID;

    // ========================================
    // DO NOT MODIFY THESE PID CONSTANTS
    // ========================================

    private ProfileGains kVelocityGains = new ProfileGains().p(0.0).f(0.0001);
    private ProfileGains kPositionGains = new ProfileGains().p(0.0001);

    // ========================================
    // DO NOT MODIFY THESE PHYSICAL CONSTANTS
    // ========================================

    public static final double kClimberRatio = (12.0 / 72.0) * (20.0 / 80.0) * (20.0 / 80.0) * (16.0 / 42.0);
    public static final double kMaxClimberSpeed = 6380 * kClimberRatio;
    public static final double kScaledUnitsToRPM = (600.0 / 2048.0) * kClimberRatio;
    public static final double kScaledUnitsToRotations = kClimberRatio / 2048;
    public static final double kVerticalAngleDeg = 90;
    public static final double kMaxAllowedVelocity = 0.75;
    public static final double kMaxScaledVelocity = kMaxClimberSpeed * kMaxAllowedVelocity;
    public static final double kWindmillCircumferenceFt = 2; // TODO Confirm if this is true
    public static final int kClimberToleranceDeg = 5; // Experiment with this to find the perfect tolerance range

    public ClimberModule() {
        mCLMR11 = new TalonFX(Settings.HW.CAN.kCLM1);
        mCL12 = new TalonFX(Settings.HW.CAN.kCL2);
        mCLMR11.setNeutralMode(NeutralMode.Brake);
        mCL12.setNeutralMode(NeutralMode.Brake);
        mCL12.configOpenloopRamp(0.5);
        mCLMR11.configOpenloopRamp(0.5);

        //Add 10 ms for current limit
        mCL12.configGetSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 19, 20, 0.01));
        mCLMR11.configGetSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 19, 20, 0.01));

        mCLPNDouble = new DoubleSolenoid(Settings.HW.PCH.kPCHCompressorModule, PneumaticsModuleType.REVPH, 2, 3);
        mCLPNSingle = new DoubleSolenoid(Settings.HW.PCH.kPCHCompressorModule, PneumaticsModuleType.REVPH, 4, 5);

        mCL12.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
        mCLMR11.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);

        mVelocityPID = new PIDController(kVelocityGains, -kMaxClimberSpeed, kMaxClimberSpeed, Settings.kControlLoopPeriod);
        mPositionPID = new PIDController(kPositionGains, -kMaxClimberSpeed, kMaxClimberSpeed, Settings.kControlLoopPeriod);
    }

    @Override
    public void modeInit(EMatchMode mode) {
        mPositionPID.setSetpoint(kVerticalAngleDeg);
        mPositionPID.setOutputRange(-kMaxScaledVelocity, kMaxScaledVelocity);
        mPositionPID.setContinuous(true);

        mVelocityPID.setSetpoint(0d);
        mVelocityPID.setOutputRange(-kMaxScaledVelocity, kMaxScaledVelocity);

        db.climber.set(CURRENT_RUNG, Enums.EClimberAngle.START);
        db.climber.set(DESIRED_RUNG, Enums.EClimberAngle.START);
    }

    @Override
    public void readInputs() {
        db.climber.set(L_VEL_rpm, mCL12.getSelectedSensorVelocity() * kScaledUnitsToRPM);
        db.climber.set(R_VEL_rpm, mCLMR11.getSelectedSensorVelocity() * kScaledUnitsToRPM);

        db.climber.set(L_POSITION_deg, mCL12.getSelectedSensorPosition() * kScaledUnitsToRotations * 360);
        db.climber.set(R_POSITION_deg, mCLMR11.getSelectedSensorPosition() * kScaledUnitsToRotations * 360);

        db.climber.set(L_OUTPUT_CURRENT, mCLMR11.getSupplyCurrent());
        db.climber.set(R_OUTPUT_CURRENT, mCLMR11.getSupplyCurrent());

        db.climber.set(BUS_VOLTAGE_LEFT, mCL12.getBusVoltage());
        db.climber.set(BUS_VOLTAGE_RIGHT, mCLMR11.getBusVoltage());

        updateRung();
    }

    @Override
    public void setOutputs() {
        Enums.EClimberMode mode = db.climber.get(EClimberModuleData.HANGER_STATE, Enums.EClimberMode.class);
        if (mode == null) return;

        if (db.climber.isSet(EClimberModuleData.SET_COAST)) {
            mCL12.setNeutralMode(NeutralMode.Coast);
            mCLMR11.setNeutralMode(NeutralMode.Coast);
        } else {
            mCL12.setNeutralMode(NeutralMode.Brake);
            mCLMR11.setNeutralMode(NeutralMode.Brake);
        }

        switch(mode) {
            case PERCENT_OUTPUT:
                mCL12.set(ControlMode.PercentOutput, db.climber.get(EClimberModuleData.DESIRED_VEL_pct));
                mCLMR11.set(ControlMode.PercentOutput, db.climber.get(EClimberModuleData.DESIRED_VEL_pct));
                break;
            case VELOCITY:
                double desiredVel = mVelocityPID.calculate(db.climber.get(EClimberModuleData.L_VEL_rpm), clock.getCurrentTimeInMillis());
                mCL12.set(ControlMode.Velocity, desiredVel);
                mCLMR11.set(ControlMode.Velocity, desiredVel);
                break;
            case POSITION:
                mPositionPID.setSetpoint(db.climber.get(EClimberModuleData.DESIRED_POS_deg));
                double desiredPos = mPositionPID.calculate(db.climber.get(EClimberModuleData.L_POSITION_deg), clock.getCurrentTimeInMillis());
                mCL12.set(ControlMode.Velocity, desiredPos);
                mCLMR11.set(ControlMode.Velocity, desiredPos);
                break;
            case BEGIN_HANG:
                mPositionPID.setSetpoint(90);
                double desiredOutput = mPositionPID.calculate(db.climber.get(EClimberModuleData.L_POSITION_deg), clock.getCurrentTimeInMillis());
                mCL12.set(ControlMode.Velocity, desiredOutput);
                mCLMR11.set(ControlMode.Velocity, desiredOutput);
                break;
        }

        if (db.climber.isSet(EClimberModuleData.DOUBLE_CLAMPED)) {
            mCLPNDouble.set(DoubleSolenoid.Value.kForward);
        } else {
            mCLPNDouble.set(DoubleSolenoid.Value.kReverse);
        }

        if (db.climber.isSet(EClimberModuleData.SINGLE_CLAMPED)) {
            mCLPNSingle.set(DoubleSolenoid.Value.kForward);
        } else {
            mCLPNSingle.set(DoubleSolenoid.Value.kReverse);
        }
    }

    private void updateRung() {
        Enums.EClimberAngle desiredRung = db.climber.get(DESIRED_RUNG, Enums.EClimberAngle.class);
        if (desiredRung == null) return;
        if (desiredRung.getStage() < 0) return; // Do not pass this point if the climber isn't looking for a rung

        double angle = db.climber.get(L_POSITION_deg);

        switch(desiredRung) {
            case VERTICAL:
                if (Math.abs(desiredRung.getAngle() - angle) < kClimberToleranceDeg && db.climber.get(DOUBLE_CLAMPED) == 1d && db.climber.get(SINGLE_CLAMPED) == 0d) {
                    db.climber.set(DESIRED_RUNG, Enums.EClimberAngle.MID);
                } else {
                    db.climber.set(DESIRED_POS_deg, desiredRung.getAngle());
                }
                break;
            case MID:
                break;
            case HIGH:
                break;
            case TRAVERSAL:
                break;
        }
    }
}
