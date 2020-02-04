package us.ilite.robot;

import com.flybotix.hfr.codex.CodexMetadata;
import com.flybotix.hfr.codex.ICodexTimeProvider;
import com.flybotix.hfr.codex.RobotCodex;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import us.ilite.common.Data;
import us.ilite.common.config.AbstractSystemSettingsUtils;
import us.ilite.common.config.Settings;
import us.ilite.common.lib.util.PerfTimer;
import us.ilite.common.types.EMatchMode;
import us.ilite.common.types.MatchMetadata;
import us.ilite.robot.controller.AbstractController;
import us.ilite.robot.controller.BaseAutonController;
import us.ilite.robot.controller.TeleopController;
import us.ilite.robot.controller.TestController;
import us.ilite.robot.hardware.Clock;
import us.ilite.robot.modules.*;

import java.util.TimerTask;

import static us.ilite.common.types.EMatchMode.*;

public class Robot extends TimedRobot {

    private ILog mLogger = Logger.createLog(this.getClass());
    public static final Data DATA = new Data();
    public static final Clock CLOCK = new Clock().simulated();
    private static EMatchMode MODE = DISABLED;
    private ModuleList mRunningModules = new ModuleList();
    private final Settings mSettings = new Settings();
    private CSVLogger mCSVLogger = new CSVLogger(DATA);

    private Limelight mLimelight;
    private PowerCellModule mIntake;
    private DriveModule mDrive;
    private RawLimelight mRawLimelight;
    private Timer initTimer = new Timer();
    private DJSpinnerModule mDJSpinnerModule;

    private PowerDistributionPanel pdp = new PowerDistributionPanel(Settings.Hardware.CAN.kPDP);
    private FlywheelModule mShooter;

    private OperatorInput mOI;
    private LEDControl mLedControl;

    private MatchMetadata mMatchMeta = null;

    private PerfTimer mClockUpdateTimer = new PerfTimer();

    private final AbstractController mTeleopController = new TeleopController();
    private final AbstractController mBaseAutonController = new BaseAutonController();
    private AbstractController mActiveController = null;
    private TestController mTestController;


    @Override
    public void robotInit() {
        MODE=INITIALIZING;
        mLogger.warn("===> ROBOT INIT Starting");
        mOI = new OperatorInput();
        mDrive = new DriveModule();
        mLedControl = new LEDControl();
        mShooter = new FlywheelModule();
        mIntake = new PowerCellModule();
        mLimelight = new Limelight();
        mRawLimelight = new RawLimelight();
        mDJSpinnerModule = new DJSpinnerModule();

        //look for practice robot config:
        AbstractSystemSettingsUtils.loadPracticeSettings(mSettings);

        // Init the actual robot
        initTimer.reset();
        initTimer.start();
        Logger.setLevel(ELevel.WARN);
        mLogger.info("Starting Robot Initialization...");

        mSettings.writeToNetworkTables();

//        new Thread(new DSConnectInitThread()).start();
        // Init static variables and get singleton instances first

        ICodexTimeProvider provider = new ICodexTimeProvider() {
            public long getTimestamp() {
                return (long) CLOCK.getCurrentTimeInNanos();
            }
        };
        CodexMetadata.overrideTimeProvider(provider);

        mRunningModules.clearModules();

        try {
        } catch (Exception e) {
            mLogger.exception(e);
        }

        LiveWindow.disableAllTelemetry();

        TimerTask shuffleupdate = new TimerTask(){
            public void run(){Shuffleboard.update();}
        };
        new java.util.Timer().scheduleAtFixedRate(shuffleupdate, 15000, 1000);

        initTimer.stop();
        mLogger.warn("Robot initialization finished. Took: ", initTimer.get(), " seconds");
    }

    /**
     * This contains code run in ALL robot modes.
     * It's also important to note that this runs AFTER mode-specific code
     */
    @Override
    public void robotPeriodic() {
        CLOCK.cycleEnded();
    }

    @Override
    public void autonomousInit() {
        MODE=AUTONOMOUS;
        mActiveController = mBaseAutonController;
        mActiveController.setEnabled(true);
    }

    @Override
    public void autonomousPeriodic() {
        commonPeriodic();
    }

    @Override
    public void teleopInit() {
        MODE=TELEOPERATED;
        mActiveController = mTeleopController;
        mActiveController.setEnabled(true);
        mRunningModules.addModule(mIntake);
        mLogger.error("kasdjdaksljsadl;kjfdas;ld");
    }

    @Override
    public void teleopPeriodic() {
        commonPeriodic();
    }

    @Override
    public void disabledInit() {
        MODE=DISABLED;
        mLogger.info("Disabled Initialization");
        mRunningModules.shutdown(CLOCK.getCurrentTime());
        mCSVLogger.stop(); // stop csv logging
        if(mActiveController != null) {
            mActiveController.setEnabled(false);
        }
    }

    @Override
    public void disabledPeriodic() {
    }

    @Override
    public void testInit() {
        if(mTestController == null) {
             mTestController = new TestController();
        }
        MODE = TEST;
        mActiveController = mTestController;
        mActiveController.setEnabled(true);
        mRunningModules.clearModules();
        mRunningModules.addModule(mOI);
//        mRunningModules.addModule(mLimelight);
        mRunningModules.addModule(mShooter);
        mRunningModules.addModule(mDrive);
        mRunningModules.addModule(mIntake);
        mRunningModules.addModule(mDJSpinnerModule);
        mRunningModules.modeInit(TEST, CLOCK.getCurrentTime());
        mRunningModules.checkModule(CLOCK.getCurrentTime());
    }

    @Override
    public void testPeriodic() {
        commonPeriodic();
    }

    void commonPeriodic() {
        double start = Timer.getFPGATimestamp();
        for(RobotCodex rc : DATA.mAllCodexes){
            rc.reset();
        }
//        EPowerDistPanel.map(mData.pdp, pdp);
        mRunningModules.readInputs(CLOCK.getCurrentTime());
        mActiveController.update(CLOCK.getCurrentTime());
        mRunningModules.setOutputs(CLOCK.getCurrentTime());
//        Robot.DATA.sendCodicesToNetworkTables();
        SmartDashboard.putNumber("common_periodic_dt", Timer.getFPGATimestamp() - start);
        SmartDashboard.putNumber("FPGA Time", Timer.getFPGATimestamp());
    }

    private void initMatchMetadata() {
        if (mMatchMeta == null) {
            mMatchMeta = new MatchMetadata();
            int gid = mMatchMeta.hash;
            for (RobotCodex c : DATA.mAllCodexes) {
                c.meta().setGlobalId(gid);
            }
        }
    }

    public static EMatchMode mode() {
        return MODE;
    }

    public String toString() {

        String mRobotMode = "Unknown";
        String mRobotEnabledDisabled = "Unknown";
        double mNow = Timer.getFPGATimestamp();

        if (this.isAutonomous()) {
            mRobotMode = "Autonomous";
        }
        if (this.isOperatorControl()) {
            mRobotMode = "OPERATOR Control";
        }
        if (this.isTest()) {
            mRobotEnabledDisabled = "Test";
        }

        if (this.isEnabled()) {
            mRobotEnabledDisabled = "Enabled";
        }
        if (this.isDisabled()) {
            mRobotEnabledDisabled = "Disabled";
        }

        return String.format("State: %s\tMode: %s\tTime: %s", mRobotEnabledDisabled, mRobotMode, mNow);

    }


}
