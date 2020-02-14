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

public class ZcashTAddress {
    private long nativeHandle;

    private ZcashTAddress() {
        nativeHandle = 0;
    }

    static ZcashTAddress createFromNative(long nativeHandle) {
        ZcashTAddress instance = new ZcashTAddress();
        instance.nativeHandle = nativeHandle;
        ZcashTAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithData(byte[] data);
    static native long nativeCreateWithPublicKey(PublicKey publicKey, byte prefix);
    static native void nativeDelete(long handle);

    public static native boolean equals(ZcashTAddress lhs, ZcashTAddress rhs);
    public static native boolean isValid(byte[] data);
    public static native boolean isValidString(String string);
    public native String description();

    public ZcashTAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        ZcashTAddressPhantomReference.register(this, nativeHandle);
    }

    public ZcashTAddress(byte[] data) {
        nativeHandle = nativeCreateWithData(data);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        ZcashTAddressPhantomReference.register(this, nativeHandle);
    }

    public ZcashTAddress(PublicKey publicKey, byte prefix) {
        nativeHandle = nativeCreateWithPublicKey(publicKey, prefix);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        ZcashTAddressPhantomReference.register(this, nativeHandle);
    }

}

class ZcashTAddressPhantomReference extends java.lang.ref.PhantomReference<ZcashTAddress> {
    private static java.util.Set<ZcashTAddressPhantomReference> references = new HashSet<ZcashTAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<ZcashTAddress> queue = new java.lang.ref.ReferenceQueue<ZcashTAddress>();
    private long nativeHandle;

    private ZcashTAddressPhantomReference(ZcashTAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(ZcashTAddress referent, long nativeHandle) {
        references.add(new ZcashTAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        ZcashTAddressPhantomReference ref = (ZcashTAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (ZcashTAddressPhantomReference) queue.poll()) {
            ZcashTAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
