package com.polidea.rxandroidble;

import android.os.Build;

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
}
