package us.ilite.common;

import java.util.Objects;

public class Log {
    private String mCodexIdentifier;
    private String mLogData;
    private int mGlobalId;

    public int getmGlobalId() {
        return mGlobalId;
    }

    public Log(String pCodexIdentifier, String pLogData, int pGlobalId) {
        mCodexIdentifier = pCodexIdentifier;
        mLogData = pLogData;
        mGlobalId = pGlobalId;
    }

    @Override
    public String toString() {
        return "Log{" +
                "mCodexIdentifier=" + mCodexIdentifier +
                ", mData='" + mLogData + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Log log = (Log) o;
        return Objects.equals(mCodexIdentifier, log.mCodexIdentifier) &&
                Objects.equals(mLogData, log.mLogData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mCodexIdentifier, mLogData);
    }

    public String getmCodexIdentifier() {
        return mCodexIdentifier;
    }

    public String getmLogData() {
        return mLogData;
    }

}
