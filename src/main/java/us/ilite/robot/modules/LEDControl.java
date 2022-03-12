package us.ilite.robot.modules;

import com.ctre.phoenix.CANifier;
import com.ctre.phoenix.CANifierStatusFrame;
import edu.wpi.first.wpilibj.Timer;
import us.ilite.common.config.Settings;

import static us.ilite.robot.Enums.*;
import us.ilite.common.types.ELEDControlData;
import us.ilite.common.types.EMatchMode;
import us.ilite.robot.Enums;


public class LEDControl extends Module {

    private CANifier mLEDCan;
    private Timer mBlinkTimer;
    private LEDColorMode mColor = LEDColorMode.DEFAULT;
    private Enums.LEDColorMode color;
    private double kBlinkTime = 0.25;

    public LEDControl() {
        this.mBlinkTimer = new Timer();
        this.mBlinkTimer.reset();
        mLEDCan = new CANifier(Settings.HW.CAN.kLEDCanifier);
        mLEDCan.setStatusFramePeriod(CANifierStatusFrame.Status_1_General, 20);
        mLEDCan.setStatusFramePeriod(CANifierStatusFrame.Status_2_General, 20);
    }

    public void modeInit( EMatchMode pMode ) {
        db.ledcontrol.set(ELEDControlData.LED_STATE, 0.0);
        this.mBlinkTimer.stop();
        this.mBlinkTimer.reset();
    }

    @Override
    protected void setOutputs() {
        //set the LED output to a color
        color = db.ledcontrol.get(ELEDControlData.DESIRED_COLOR, Enums.LEDColorMode.class);
        if (db.ledcontrol.get(ELEDControlData.LED_STATE) == 1d) {
            //start timer and turn off if mBlinkTimer < half of cycle time, and on if mBlinkTimer > half of cycle time
            mBlinkTimer.start();
            if(mBlinkTimer.hasElapsed(kBlinkTime / 2)) {
                setLEDColor(color.getRed(), color.getGreen(), color.getBlue());
                if (mBlinkTimer.hasElapsed(kBlinkTime)) {
                    mBlinkTimer.reset();
                }
            }
            else {
                setLEDColor(0, 0, 0);
            }
        }
        else {
            setLEDColor(color.getRed(), color.getGreen(), color.getBlue());
        }
    }

    public void setLEDColor(double r, double g, double b) {
        mLEDCan.setLEDOutput(r / 255, CANifier.LEDChannel.LEDChannelA);
        mLEDCan.setLEDOutput(g / 255, CANifier.LEDChannel.LEDChannelB);
        mLEDCan.setLEDOutput(b / 255, CANifier.LEDChannel.LEDChannelC);
    }


}