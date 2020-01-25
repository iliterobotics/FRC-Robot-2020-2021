package us.ilite.robot;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.Data;
import us.ilite.common.config.InputMap;
import us.ilite.common.config.Settings;
import us.ilite.common.types.EMatchMode;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.EInputScale;
import us.ilite.common.types.input.ELogitech310;
import static us.ilite.robot.hardware.ECommonControlMode.*;

import us.ilite.robot.modules.Module;
import us.ilite.robot.modules.*;

public class DriverInput extends Module implements IThrottleProvider, ITurnProvider {

    protected static final double
            DRIVER_SUB_WARP_AXIS_THRESHOLD = 0.5;
    private ILog mLog = Logger.createLog(DriverInput.class);


    protected DriveModule mDrive;
    private FlywheelModule mShooter;
    private CommandManager mTeleopCommandManager;
    private CommandManager mAutonomousCommandManager;
    private Limelight mLimelight;
    private Data mData;
    private Timer mGroundCargoTimer = new Timer();

    private ETrackingType mTrackingType;

    private boolean mIsCargo = false;
    private Joystick mDriverJoystick;
    private Joystick mOperatorJoystick;

    protected Codex<Double, ELogitech310> mDriverInputCodex, mOperatorInputCodex;

    private ETrackingType mLastTrackingType = null;

    public DriverInput(DriveModule pDrivetrain, FlywheelModule pShooter, Limelight pLimelight, Data pData,
                       boolean pSimulated) {
        this.mLimelight = pLimelight;
        this.mShooter = pShooter;
        this.mData = pData;
//        this.mTeleopCommandManager = pTeleopCommandManager;
//        this.mAutonomousCommandManager = pAutonomousCommandManager;

        this.mDriverInputCodex = mData.driverinput;
        this.mOperatorInputCodex = mData.operatorinput;
        if(pSimulated) {
            // Use a different joystick library?

        } else {
            this.mDriverJoystick = new Joystick(0);
            this.mOperatorJoystick = new Joystick(1);
        }
    }


    @Override
    public void modeInit(EMatchMode pMode, double pNow) {

    }

    @Override
    public void readInputs(double pNow) {
        ELogitech310.map(mData.driverinput, mDriverJoystick);
        ELogitech310.map(mData.operatorinput, mOperatorJoystick);
    }

    @Override
    public void setOutputs(double pNow) {
        updateShooter();
        updateDriveTrain();
        updateLimelightTargetLock();
    }

    private void updateShooter() {
        if (mDriverInputCodex.isSet(InputMap.DRIVER.SUB_WARP_AXIS)) {
            mShooter.setShooterState(FlywheelModule.EShooterState.SHOOT);
            mShooter.setAcceleratorState(FlywheelModule.EAcceleratorState.FEED);
        }
        else {
            mShooter.setShooterState(FlywheelModule.EShooterState.STOP);
        }
        mShooter.isGyro = !mDriverInputCodex.isSet(InputMap.DRIVER.TARGET_LOCK);

    }

    private void updateLimelightTargetLock() {
        if ( mDriverInputCodex.isSet(InputMap.DRIVER.TARGET_LOCK) ) {
            mTrackingType = ETrackingType.TARGET;
        }
        else if ( mDriverInputCodex.isSet(InputMap.DRIVER.CELL_LOCK) ) {
            mTrackingType = ETrackingType.CELL;
        }
        else {
            mTrackingType = ETrackingType.NONE;
        }
    }

    private void updateDriveTrain() {
        double rotate = getTurn();
        double throttle = getThrottle();

        //		    throttle = EInputScale.EXPONENTIAL.map(throttle, 2);
        rotate = EInputScale.EXPONENTIAL.map(rotate, 2);
        rotate *= Settings.Input.kNormalPercentThrottleReduction;

        if (mData.driverinput.isSet(InputMap.DRIVER.SUB_WARP_AXIS) && mData.driverinput.get(InputMap.DRIVER.SUB_WARP_AXIS) > DRIVER_SUB_WARP_AXIS_THRESHOLD) {
            throttle *= Settings.Input.kSnailModePercentThrottleReduction;
            rotate *= Settings.Input.kSnailModePercentRotateReduction;
        }

        DriveMessage driveMessage = new DriveMessage().throttle(throttle).turn(rotate).mode(PERCENT_OUTPUT).normalize().calculateCurvature();

        mDrive.setDriveMessage(driveMessage);
    }

    @Override
    public void shutdown(double pNow) {

    }

    @Override
    public double getThrottle() {
        if(mData.driverinput.isSet(InputMap.DRIVER.THROTTLE_AXIS)) {
            return -mData.driverinput.get(InputMap.DRIVER.THROTTLE_AXIS);
        } else {
            return 0.0;
        }
    }

    @Override
    public double getTurn() {
        if(mData.driverinput.isSet(InputMap.DRIVER.TURN_AXIS)) {
            return mData.driverinput.get(InputMap.DRIVER.TURN_AXIS);
        } else {
            return 0.0;
        }
    }
}
