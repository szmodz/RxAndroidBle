package com.polidea.rxandroidble.internal.scan;


import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;

import com.polidea.rxandroidble.internal.operations.ScanOperationApi18;
import com.polidea.rxandroidble.internal.operations.ScanOperationApi21;
import com.polidea.rxandroidble.internal.util.RxBleAdapterWrapper;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanSettings;
import com.polidea.rxandroidble.RxBleOptions;
import javax.inject.Inject;
import rx.Observable;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class ScanSetupBuilderImplApi21 implements ScanSetupBuilder {

    private final RxBleAdapterWrapper rxBleAdapterWrapper;
    private final InternalScanResultCreator internalScanResultCreator;
    private final ScanSettingsEmulator scanSettingsEmulator;
    private final AndroidScanObjectsConverter androidScanObjectsConverter;

    @Inject
    ScanSetupBuilderImplApi21(
            RxBleAdapterWrapper rxBleAdapterWrapper,
            InternalScanResultCreator internalScanResultCreator,
            ScanSettingsEmulator scanSettingsEmulator,
            AndroidScanObjectsConverter androidScanObjectsConverter
    ) {
        this.rxBleAdapterWrapper = rxBleAdapterWrapper;
        this.internalScanResultCreator = internalScanResultCreator;
        this.scanSettingsEmulator = scanSettingsEmulator;
        this.androidScanObjectsConverter = androidScanObjectsConverter;
    }

    private ScanSetup buildScan18(ScanSettings scanSettings, ScanFilter... scanFilters) {
        final Observable.Transformer<RxBleInternalScanResult, RxBleInternalScanResult> scanModeTransformer
                = scanSettingsEmulator.emulateScanMode(scanSettings.getScanMode());
        final Observable.Transformer<RxBleInternalScanResult, RxBleInternalScanResult> callbackTypeTransformer
                = scanSettingsEmulator.emulateCallbackType(scanSettings.getCallbackType());
        return new ScanSetup(
                new ScanOperationApi18(
                        rxBleAdapterWrapper,
                        internalScanResultCreator,
                        new EmulatedScanFilterMatcher(scanFilters)
                ),
                new Observable.Transformer<RxBleInternalScanResult, RxBleInternalScanResult>() {
                    @Override
                    public Observable<RxBleInternalScanResult> call(Observable<RxBleInternalScanResult> observable) {
                        return observable.compose(scanModeTransformer)
                                .compose(callbackTypeTransformer);
                    }
                }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanSetup buildEmulated(ScanSettings scanSettings, EmulatedScanFilterMatcher matcher, ScanFilter[] scanFilters) {
        /*
         Android 5.0 (API21) does not handle FIRST_MATCH and / or MATCH_LOST callback type
         https://developer.android.com/reference/android/bluetooth/le/ScanSettings.Builder.html#setCallbackType(int)
          */
        final Observable.Transformer<RxBleInternalScanResult, RxBleInternalScanResult> callbackTypeTransformer
                = scanSettingsEmulator.emulateCallbackType(scanSettings.getCallbackType());

        final ScanSettings newSettings = new ScanSettings.Builder()
                .setScanMode(scanSettings.getScanMode())
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();

        return new ScanSetup(
                new ScanOperationApi21(
                        rxBleAdapterWrapper,
                        internalScanResultCreator,
                        androidScanObjectsConverter,
                        newSettings,
                        matcher,
                        scanFilters),
                new Observable.Transformer<RxBleInternalScanResult, RxBleInternalScanResult>() {
                    @Override
                    public Observable<RxBleInternalScanResult> call(Observable<RxBleInternalScanResult> observable) {
                        return observable.compose(callbackTypeTransformer);
                    }
                }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanSetup buildPure(ScanSettings scanSettings, EmulatedScanFilterMatcher matcher, ScanFilter[] scanFilters) {
        // for now assuming that on Android 6.0+ there are no problems

        if (scanSettings.getCallbackType() != ScanSettings.CALLBACK_TYPE_ALL_MATCHES && scanFilters.length == 0) {
            // native matching does not work with no filters specified - see https://issuetracker.google.com/issues/37127640
            scanFilters = new ScanFilter[] {
                    ScanFilter.empty()
            };
        }
        return new ScanSetup(
                new ScanOperationApi21(
                        rxBleAdapterWrapper,
                        internalScanResultCreator,
                        androidScanObjectsConverter,
                        scanSettings,
                        matcher,
                        scanFilters),
                new Observable.Transformer<RxBleInternalScanResult, RxBleInternalScanResult>() {
                    @Override
                    public Observable<RxBleInternalScanResult> call(Observable<RxBleInternalScanResult> observable) {
                        return observable;
                    }
                }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public ScanSetup build(ScanSettings scanSettings, ScanFilter... scanFilters) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || RxBleOptions.isStartLeScanForced()) {
            return buildScan18(scanSettings, scanFilters);
        }

        EmulatedScanFilterMatcher matcher;
        if (scanFilters.length > 0 && RxBleOptions.isScanFilterEmulationForced()) {
            matcher = new EmulatedScanFilterMatcher(scanFilters);
            scanFilters = new ScanFilter[] {};
        } else {
            matcher = new EmulatedScanFilterMatcher();
        }

        final boolean emulateCallbackType = RxBleOptions.isCallbackTypeEmulationForced()
                || (scanSettings.getCallbackType() != ScanSettings.CALLBACK_TYPE_ALL_MATCHES
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.M);

        if (emulateCallbackType) {
            return buildEmulated(scanSettings, matcher, scanFilters);
        } else {
            return buildPure(scanSettings, matcher, scanFilters);
        }
    }
}
