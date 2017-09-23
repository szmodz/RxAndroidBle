package com.polidea.rxandroidble;

/**
 * Created by szmodz on 23.09.17.
 */

public final class RxBleOptions {
    private RxBleOptions() {
    }

    private static int deviceLostTimeoutMs = 10000;

    public static int getDeviceLostTimeoutMs() {
        return deviceLostTimeoutMs;
    }

    public static void setDeviceLostTimeoutMs(int timeoutMs) {
        deviceLostTimeoutMs = timeoutMs;
    }

    private static boolean callbackTypeEmulationForced = true;

    public static boolean isCallbackTypeEmulationForced() {
        return callbackTypeEmulationForced;
    }

    public static void setCallbackTypeEmulationForced(boolean val) {
        callbackTypeEmulationForced = val;
    }

    private static boolean scanFilterEmulationForced = true;

    public static boolean isScanFilterEmulationForced() {
        return scanFilterEmulationForced;
    }

    public static void setScanFilterEmulationForced(boolean val) {
        scanFilterEmulationForced = val;
    }

    private static boolean startLeScanForced = false;

    public static boolean isStartLeScanForced() {
        return startLeScanForced;
    }

    public static void setStartLeScanForced(boolean val) {
        startLeScanForced = val;
    }
}
