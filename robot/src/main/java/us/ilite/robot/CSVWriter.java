package us.ilite.robot;

import com.flybotix.hfr.codex.CodexMetadata;
import com.flybotix.hfr.codex.RobotCodex;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.wpilibj.DriverStation;
import us.ilite.common.Data;
import us.ilite.common.config.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

import static us.ilite.robot.CSVLogger.kCSVLoggerQueue;

public class CSVWriter {

    public static final String USB_DIR = "/u";
    public static final String USER_DIR = System.getProperty("user.home");
    private static final String LOG_PATH_FORMAT = "/logs/%s/%s-%s-%s.csv";

    private final ILog mLog = Logger.createLog(CSVWriter.class);
    private static int mLogFailures;

    private RobotCodex<?> mCodex;
    private final Optional<BufferedWriter> writer;

    public CSVWriter(RobotCodex<?> pCodex) {
        mCodex = pCodex;

        mLogFailures = 0;
        File file = file();
        Data.handleCreation( file );
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter( new FileWriter( file ) );
        } catch (IOException pE) {
            System.out.println( pE.getLocalizedMessage() );
        }
        writer = Optional.ofNullable( bw );
    }

    public void log( String s ) {
        if ( writer.isPresent() ) {
            try {
                writer.get().append( s );
                writer.get().newLine();
            } catch (IOException pE) {
                pE.printStackTrace();
            }
        }
        else {
            if ( mLogFailures < Settings.kAcceptableLogFailures ) {
                mLog.error( "Could not find Path: \"" + USB_DIR + "\" on roborio! Try plugging in the USB." );
                mLogFailures++;
            }
            else if ( mLogFailures == Settings.kAcceptableLogFailures ) {
                mLog.error("---------------------CSV LOGGING DISABLED----------------------");
                mLogFailures++;
            }
        }
    }

    public void writeHeader() {
        kCSVLoggerQueue.add( new Log( mCodex.getCSVHeader(), mCodex.meta().gid() ) );
    }

    public CodexMetadata<?> getMetaDataOfAssociatedCodex() {
        return mCodex.meta();
    }

    public File file() {

        // Don't default to home dir to avoid filling up memory
//        String dir = "";
//        if(Files.notExists(new File(USB_DIR).toPath())) {
//            dir = USER_DIR;
//        } else {
//            dir = USB_DIR;
//        }

        String dir = USB_DIR;

        String eventName = DriverStation.getInstance().getEventName();
        if ( eventName.length() <= 0 ) {
            // event name format: MM-DD-YYYY_HH-MM-SS
            eventName =  new SimpleDateFormat("MM-dd-YYYY_HH-mm-ss").format(Calendar.getInstance().getTime());
        }

        File file = null;
        if ( mCodex.meta().getEnum().getSimpleName().equals("ELogitech310")) {
            if ( mCodex.meta().gid() == Robot.DATA.driverinput.meta().gid() ) {
                file = new File(String.format( dir + LOG_PATH_FORMAT,
                        eventName,
                        "DriverInput",
                        DriverStation.getInstance().getMatchType().name(),
                        Integer.toString(DriverStation.getInstance().getMatchNumber())
                ));
            } else {
                file = new File(String.format( dir + LOG_PATH_FORMAT,
                        eventName,
                        "OperatorInput",
                        DriverStation.getInstance().getMatchType().name(),
                        Integer.toString(DriverStation.getInstance().getMatchNumber())
                ));
            }
        }
        else {
            file = new File(String.format( dir + LOG_PATH_FORMAT,
                    eventName,
                    mCodex.meta().getEnum().getSimpleName(),
                    DriverStation.getInstance().getMatchType().name(),
                    Integer.toString(DriverStation.getInstance().getMatchNumber())
            ));
        }

        mLog.error("Creating log file at ", file.toPath());

        return file;
    }

    public void closeWriter() {
        if ( writer.isPresent() ) {
            try {
                writer.get().flush();
                writer.get().close();
            } catch (IOException pE) {
                pE.printStackTrace();
            }
        }
    }

}