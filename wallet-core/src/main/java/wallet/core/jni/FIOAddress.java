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

public class FIOAddress {
    private long nativeHandle;

    private FIOAddress() {
        nativeHandle = 0;
    }

    static FIOAddress createFromNative(long nativeHandle) {
        FIOAddress instance = new FIOAddress();
        instance.nativeHandle = nativeHandle;
        FIOAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(FIOAddress lhs, FIOAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public FIOAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        FIOAddressPhantomReference.register(this, nativeHandle);
    }

    public FIOAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        FIOAddressPhantomReference.register(this, nativeHandle);
    }

}

class FIOAddressPhantomReference extends java.lang.ref.PhantomReference<FIOAddress> {
    private static java.util.Set<FIOAddressPhantomReference> references = new HashSet<FIOAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<FIOAddress> queue = new java.lang.ref.ReferenceQueue<FIOAddress>();
    private long nativeHandle;

    private FIOAddressPhantomReference(FIOAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(FIOAddress referent, long nativeHandle) {
        references.add(new FIOAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        FIOAddressPhantomReference ref = (FIOAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (FIOAddressPhantomReference) queue.poll()) {
            FIOAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
