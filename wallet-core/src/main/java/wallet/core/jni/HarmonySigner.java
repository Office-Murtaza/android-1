// Copyright © 2017-2019 Trust Wallet.
//
// This file is part of Trust. The full Trust copyright notice, including
// terms governing use, modification, and redistribution, is contained in the
// file LICENSE at the root of the source code distribution tree.
//
// This is a GENERATED FILE, changes made here WILL BE LOST.
//

package wallet.core.jni;

import java.security.InvalidParameterException;
import java.util.HashSet;

public class HarmonySigner {
    private long nativeHandle;

    private HarmonySigner() {
        nativeHandle = 0;
    }

    static HarmonySigner createFromNative(long nativeHandle) {
        HarmonySigner instance = new HarmonySigner();
        instance.nativeHandle = nativeHandle;
        return instance;
    }


    public static native wallet.core.jni.proto.Harmony.SigningOutput sign(wallet.core.jni.proto.Harmony.SigningInput input);

}

