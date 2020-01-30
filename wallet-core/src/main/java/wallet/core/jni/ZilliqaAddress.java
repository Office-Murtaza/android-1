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

public class ZilliqaAddress {
    private long nativeHandle;

    private ZilliqaAddress() {
        nativeHandle = 0;
    }

    static ZilliqaAddress createFromNative(long nativeHandle) {
        ZilliqaAddress instance = new ZilliqaAddress();
        instance.nativeHandle = nativeHandle;
        ZilliqaAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithKeyHash(byte[] keyHash);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(ZilliqaAddress lhs, ZilliqaAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native String keyHash();

    public ZilliqaAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        ZilliqaAddressPhantomReference.register(this, nativeHandle);
    }

    public ZilliqaAddress(byte[] keyHash) {
        nativeHandle = nativeCreateWithKeyHash(keyHash);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        ZilliqaAddressPhantomReference.register(this, nativeHandle);
    }

    public ZilliqaAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        ZilliqaAddressPhantomReference.register(this, nativeHandle);
    }

}

class ZilliqaAddressPhantomReference extends java.lang.ref.PhantomReference<ZilliqaAddress> {
    private static java.util.Set<ZilliqaAddressPhantomReference> references = new HashSet<ZilliqaAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<ZilliqaAddress> queue = new java.lang.ref.ReferenceQueue<ZilliqaAddress>();
    private long nativeHandle;

    private ZilliqaAddressPhantomReference(ZilliqaAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(ZilliqaAddress referent, long nativeHandle) {
        references.add(new ZilliqaAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        ZilliqaAddressPhantomReference ref = (ZilliqaAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (ZilliqaAddressPhantomReference) queue.poll()) {
            ZilliqaAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
