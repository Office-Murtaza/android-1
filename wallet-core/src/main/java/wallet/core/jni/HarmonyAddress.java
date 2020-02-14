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

public class HarmonyAddress {
    private long nativeHandle;

    private HarmonyAddress() {
        nativeHandle = 0;
    }

    static HarmonyAddress createFromNative(long nativeHandle) {
        HarmonyAddress instance = new HarmonyAddress();
        instance.nativeHandle = nativeHandle;
        HarmonyAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithKeyHash(byte[] keyHash);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(HarmonyAddress lhs, HarmonyAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native byte[] keyHash();

    public HarmonyAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        HarmonyAddressPhantomReference.register(this, nativeHandle);
    }

    public HarmonyAddress(byte[] keyHash) {
        nativeHandle = nativeCreateWithKeyHash(keyHash);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        HarmonyAddressPhantomReference.register(this, nativeHandle);
    }

    public HarmonyAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        HarmonyAddressPhantomReference.register(this, nativeHandle);
    }

}

class HarmonyAddressPhantomReference extends java.lang.ref.PhantomReference<HarmonyAddress> {
    private static java.util.Set<HarmonyAddressPhantomReference> references = new HashSet<HarmonyAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<HarmonyAddress> queue = new java.lang.ref.ReferenceQueue<HarmonyAddress>();
    private long nativeHandle;

    private HarmonyAddressPhantomReference(HarmonyAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(HarmonyAddress referent, long nativeHandle) {
        references.add(new HarmonyAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        HarmonyAddressPhantomReference ref = (HarmonyAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (HarmonyAddressPhantomReference) queue.poll()) {
            HarmonyAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
