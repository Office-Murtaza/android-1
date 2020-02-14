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

public class WavesAddress {
    private long nativeHandle;

    private WavesAddress() {
        nativeHandle = 0;
    }

    static WavesAddress createFromNative(long nativeHandle) {
        WavesAddress instance = new WavesAddress();
        instance.nativeHandle = nativeHandle;
        WavesAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(WavesAddress lhs, WavesAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native byte[] keyHash();

    public WavesAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        WavesAddressPhantomReference.register(this, nativeHandle);
    }

    public WavesAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        WavesAddressPhantomReference.register(this, nativeHandle);
    }

}

class WavesAddressPhantomReference extends java.lang.ref.PhantomReference<WavesAddress> {
    private static java.util.Set<WavesAddressPhantomReference> references = new HashSet<WavesAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<WavesAddress> queue = new java.lang.ref.ReferenceQueue<WavesAddress>();
    private long nativeHandle;

    private WavesAddressPhantomReference(WavesAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(WavesAddress referent, long nativeHandle) {
        references.add(new WavesAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        WavesAddressPhantomReference ref = (WavesAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (WavesAddressPhantomReference) queue.poll()) {
            WavesAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
