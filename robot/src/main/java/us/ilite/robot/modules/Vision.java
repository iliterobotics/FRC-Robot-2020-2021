package us.ilite.robot.modules;

import com.flybotix.hfr.util.lang.EnumUtils;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import us.ilite.common.Data;
import static us.ilite.common.types.ERawLimelightData.*;

import us.ilite.common.Field2020;
import us.ilite.common.IFieldComponent;
import us.ilite.common.types.ELimelightData;
import us.ilite.robot.Robot;

import java.util.Optional;

import static java.lang.Math.*;

public class Vision extends Module {
    private Limelight mLimelight;
    private final NetworkTable mTable = NetworkTableInstance.getDefault().getTable("limelight");
    private Data mData;
    private IFieldComponent mTrackingType;

    private double lastXPosition = 0.0;
    private double lastYPosition = 0.0;
    private final double acceptableError = 0.1;
    private final double acceptableXError = 10.0;
    private final double acceptableYError = 10.0;
    private int selectedTarget = -1;

    private boolean isTracking = false;

    public Vision(Data pData, Limelight pLimelight) {
        mData = pData;
        mLimelight = pLimelight;
//        mTrackingType = mLimelight.getTracking();
    }
    @Override
    public void readInputs(double pNow) {

    }

    @Override
    public void setOutputs(double pNow) {
        Robot.DATA.rawLimelight.reset();
        boolean targetValid = mTable.getEntry("tv").getDouble((Double.NaN)) > 0.0;
        Robot.DATA.rawLimelight.set(tv, targetValid ? 1.0 : null);
//        mTrackingType = mLimelight.getTracking();
        if (targetValid) {
            Robot.DATA.rawLimelight.set(tx, mTable.getEntry("tx").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tx0, mTable.getEntry("tx0").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tx1, mTable.getEntry("tx1").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tx2, mTable.getEntry("tx2").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ty, mTable.getEntry("ty").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ty0, mTable.getEntry("ty0").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ty1, mTable.getEntry("ty1").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ty2, mTable.getEntry("ty2").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ta, mTable.getEntry("ta").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ta0, mTable.getEntry("ta0").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ta1, mTable.getEntry("ta1").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ta2, mTable.getEntry("ta2").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ts, mTable.getEntry("ts").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ts0, mTable.getEntry("ts0").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ts1, mTable.getEntry("ts1").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(ts2, mTable.getEntry("ts2").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tl, mTable.getEntry("tl").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tshort, mTable.getEntry("tshort").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tshort0, mTable.getEntry("tshort0").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tshort1, mTable.getEntry("tshort1").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tshort2, mTable.getEntry("tshort2").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tlong, mTable.getEntry("tlong").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tlong0, mTable.getEntry("tlong0").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tlong1, mTable.getEntry("tlong1").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tlong2, mTable.getEntry("tlong2").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(thoriz, mTable.getEntry("thor").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(thoriz0, mTable.getEntry("thor0").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(thoriz1, mTable.getEntry("thor1").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(thoriz2, mTable.getEntry("thor2").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tvert, mTable.getEntry("tvert").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tvert0, mTable.getEntry("tvert0").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tvert1, mTable.getEntry("tvert1").getDouble(Double.NaN));
            Robot.DATA.rawLimelight.set(tvert2, mTable.getEntry("tvert2").getDouble(Double.NaN));

            Robot.DATA.rawLimelight.set(calcDistToTarget, Robot.DATA.limelight.get(ELimelightData.CALC_DIST_TO_TARGET));
            Robot.DATA.rawLimelight.set(calcAngleToTarget, Robot.DATA.limelight.get(ELimelightData.CALC_ANGLE_TO_TARGET));

            if (mLimelight.mVisionTarget != null) {
                Optional<Translation2d> p = mLimelight.calcTargetLocation(mLimelight.mVisionTarget);
                if (p.isPresent()) {
                   // Robot.DATA.rawLimelight.set(calcTargetX, p.get().x());
                   // Robot.DATA.rawLimelight.set(calcTargetY, p.get().y());
                }
            }
        }

    }

    @Override
    public void shutdown(double pNow) {

    }

    public void sortTrackingData() {
        Robot.DATA.selectedTarget.reset();

        if (mTrackingType.equals(Field2020.FieldElement.POWER_CELL)) {
            mData.selectedTarget.set(ELimelightData.TV, mData.rawLimelight.get(tv));
            boolean targetValid = Robot.DATA.selectedTarget.isSet(ELimelightData.TV);
            if (targetValid) {
                if (!isTracking) {
                    if (abs(Robot.DATA.rawLimelight.get(ty0) - Robot.DATA.rawLimelight.get(ty1)) < acceptableError) {
                        selectedTarget = Robot.DATA.rawLimelight.get(tx0) > Robot.DATA.rawLimelight.get(tx1) ? 0 : 1;
                        isTracking = true;
                    } else {
                        selectedTarget = 0;
                    }
                }

                System.out.println("Selected Target" + selectedTarget);
                Robot.DATA.selectedTarget.set(ELimelightData.TX, mTable.getEntry("tx" + selectedTarget).getDouble(Double.NaN) * (Limelight.llFOVHorizontal / 2));
                Robot.DATA.selectedTarget.set(ELimelightData.TY, mTable.getEntry("ty" + selectedTarget).getDouble(Double.NaN) * (Limelight.llFOVVertical / 2));
                Robot.DATA.selectedTarget.set(ELimelightData.TA, mTable.getEntry("ta" + selectedTarget).getDouble(Double.NaN));
                Robot.DATA.selectedTarget.set(ELimelightData.TS, mTable.getEntry("ts" + selectedTarget).getDouble(Double.NaN));
                Robot.DATA.selectedTarget.set(ELimelightData.TL, Robot.DATA.rawLimelight.get(tl));
                Robot.DATA.selectedTarget.set(ELimelightData.TSHORT, mTable.getEntry("tshort" + selectedTarget).getDouble(Double.NaN));
                Robot.DATA.selectedTarget.set(ELimelightData.TLONG, mTable.getEntry("tlong" + selectedTarget).getDouble(Double.NaN));
                Robot.DATA.selectedTarget.set(ELimelightData.THORIZ, mTable.getEntry("thor" + selectedTarget).getDouble(Double.NaN));
                Robot.DATA.selectedTarget.set(ELimelightData.TVERT, mTable.getEntry("tvert" + selectedTarget).getDouble(Double.NaN));

                Robot.DATA.selectedTarget.set(ELimelightData.CALC_DIST_TO_TARGET, Robot.DATA.rawLimelight.get(calcDistToTarget));
                Robot.DATA.selectedTarget.set(ELimelightData.CALC_ANGLE_TO_TARGET, Robot.DATA.rawLimelight.get(calcAngleToTarget));

                Robot.DATA.selectedTarget.set(ELimelightData.CALC_TARGET_X, Robot.DATA.rawLimelight.get(calcTargetX));
                Robot.DATA.selectedTarget.set(ELimelightData.CALC_TARGET_Y, Robot.DATA.rawLimelight.get(calcTargetY));

                System.out.println("last tx and ty: " + lastXPosition + ", " + lastYPosition);
                System.out.println("current tx and ty: " + Robot.DATA.selectedTarget.get(ELimelightData.TX) + ", " + Robot.DATA.selectedTarget.get(ELimelightData.TY));
                if (abs(lastXPosition - Robot.DATA.selectedTarget.get(ELimelightData.TX)) > acceptableXError || abs(lastYPosition - Robot.DATA.selectedTarget.get(ELimelightData.TY)) > acceptableYError) {
                    isTracking = false;
                }
                lastXPosition = Robot.DATA.selectedTarget.get(ELimelightData.TX);
                lastYPosition = Robot.DATA.selectedTarget.get(ELimelightData.TY);
            }
        } else {          //set selectedTarget codex straight from limelight codex
            for (ELimelightData e : EnumUtils.getEnums(ELimelightData.class)) {
                Robot.DATA.selectedTarget.set(e, Robot.DATA.limelight.get(e));
            }
//            System.out.println("SELECTED TARGET FROM LIMELIGHT");
        }
    }
}