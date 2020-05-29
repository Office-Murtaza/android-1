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

public class SegwitAddress {
    private long nativeHandle;

    private SegwitAddress() {
        nativeHandle = 0;
    }

    static SegwitAddress createFromNative(long nativeHandle) {
        SegwitAddress instance = new SegwitAddress();
        instance.nativeHandle = nativeHandle;
        SegwitAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(HRP hrp, PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(SegwitAddress lhs, SegwitAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native HRP hrp();
    public native byte[] witnessProgram();

    public SegwitAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        SegwitAddressPhantomReference.register(this, nativeHandle);
    }

    public SegwitAddress(HRP hrp, PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(hrp, publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        SegwitAddressPhantomReference.register(this, nativeHandle);
    }

}

class SegwitAddressPhantomReference extends java.lang.ref.PhantomReference<SegwitAddress> {
    private static java.util.Set<SegwitAddressPhantomReference> references = new HashSet<SegwitAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<SegwitAddress> queue = new java.lang.ref.ReferenceQueue<SegwitAddress>();
    private long nativeHandle;

    private SegwitAddressPhantomReference(SegwitAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(SegwitAddress referent, long nativeHandle) {
        references.add(new SegwitAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        SegwitAddressPhantomReference ref = (SegwitAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (SegwitAddressPhantomReference) queue.poll()) {
            SegwitAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
