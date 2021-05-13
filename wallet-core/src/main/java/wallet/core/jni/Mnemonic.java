// Copyright © 2017-2020 Trust Wallet.
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

public class Mnemonic {
    private long nativeHandle;

    private Mnemonic() {
        nativeHandle = 0;
    }

    static Mnemonic createFromNative(long nativeHandle) {
        Mnemonic instance = new Mnemonic();
        instance.nativeHandle = nativeHandle;
        return instance;
    }


    public static native boolean isValid(String mnemonic);
    public static native boolean isValidWord(String word);
    public static native String suggest(String prefix);

}

