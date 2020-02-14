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

public class NanoAddress {
    private long nativeHandle;

    private NanoAddress() {
        nativeHandle = 0;
    }

    static NanoAddress createFromNative(long nativeHandle) {
        NanoAddress instance = new NanoAddress();
        instance.nativeHandle = nativeHandle;
        NanoAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(NanoAddress lhs, NanoAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native byte[] keyHash();

    public NanoAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        NanoAddressPhantomReference.register(this, nativeHandle);
    }

    public NanoAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        NanoAddressPhantomReference.register(this, nativeHandle);
    }

}

class NanoAddressPhantomReference extends java.lang.ref.PhantomReference<NanoAddress> {
    private static java.util.Set<NanoAddressPhantomReference> references = new HashSet<NanoAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<NanoAddress> queue = new java.lang.ref.ReferenceQueue<NanoAddress>();
    private long nativeHandle;

    private NanoAddressPhantomReference(NanoAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(NanoAddress referent, long nativeHandle) {
        references.add(new NanoAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        NanoAddressPhantomReference ref = (NanoAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (NanoAddressPhantomReference) queue.poll()) {
            NanoAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
