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

public class RippleXAddress {
    private long nativeHandle;

    private RippleXAddress() {
        nativeHandle = 0;
    }

    static RippleXAddress createFromNative(long nativeHandle) {
        RippleXAddress instance = new RippleXAddress();
        instance.nativeHandle = nativeHandle;
        RippleXAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey, int tag);
    static native void nativeDelete(long handle);

    public static native boolean equals(RippleXAddress lhs, RippleXAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native int tag();

    public RippleXAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        RippleXAddressPhantomReference.register(this, nativeHandle);
    }

    public RippleXAddress(PublicKey publicKey, int tag) {
        nativeHandle = nativeCreateWithPublicKey(publicKey, tag);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        RippleXAddressPhantomReference.register(this, nativeHandle);
    }

}

class RippleXAddressPhantomReference extends java.lang.ref.PhantomReference<RippleXAddress> {
    private static java.util.Set<RippleXAddressPhantomReference> references = new HashSet<RippleXAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<RippleXAddress> queue = new java.lang.ref.ReferenceQueue<RippleXAddress>();
    private long nativeHandle;

    private RippleXAddressPhantomReference(RippleXAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(RippleXAddress referent, long nativeHandle) {
        references.add(new RippleXAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        RippleXAddressPhantomReference ref = (RippleXAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (RippleXAddressPhantomReference) queue.poll()) {
            RippleXAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
