package us.ilite.robot.commands;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import us.ilite.common.types.ELimelightData;
import us.ilite.robot.Robot;
import us.ilite.common.IFieldComponent;
import us.ilite.robot.modules.IThrottleProvider;
import us.ilite.robot.modules.Limelight;

public class LimelightTargetLock extends TargetLock {

    private ILog mLog = Logger.createLog(LimelightTargetLock.class);

    private Limelight mLimelight;


//    public LimelightTargetLock(Drive pDrive, Limelight pLimelight, double pAllowableError, ETrackingType pTrackingType, IThrottleProvider pThrottleProvider) {
//        super(pDrive, pAllowableError, pTrackingType, pLimelight, pThrottleProvider);
//        this.mLimelight = pLimelight;
//        mLimelight.setTracking(pTrackingType);
//        mLog.error("STARTED LIMELIGHT TARGET LOCK");
//    }

    public LimelightTargetLock(Limelight pLimelight, double pAllowableError, IFieldComponent pTrackingType, IThrottleProvider pThrottleProvider, boolean pEndOnAlignment) {
        super(pAllowableError, pTrackingType, pLimelight, pThrottleProvider, pEndOnAlignment);

        this.mLimelight = pLimelight;
        mLog.error("STARTED LIMELIGHT TARGET LOCK");
    }

    @Override
    public void init(double pNow) {
        super.init(pNow);
        mLog.error("STARTED LIMELIGHT TARGET LOCK");
    }

    public void shutdown(double pNow) {
        super.shutdown(pNow);
        mLog.warn("SHUT DOWN LIMELIGHT TARGET LOCK");
        Robot.DATA.limelight.set(ELimelightData.TARGET_ID , (double)Limelight.NONE.id());
    }

}
