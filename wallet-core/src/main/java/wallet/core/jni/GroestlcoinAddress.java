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

public class GroestlcoinAddress {
    private long nativeHandle;

    private GroestlcoinAddress() {
        nativeHandle = 0;
    }

    static GroestlcoinAddress createFromNative(long nativeHandle) {
        GroestlcoinAddress instance = new GroestlcoinAddress();
        instance.nativeHandle = nativeHandle;
        GroestlcoinAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey, byte prefix);
    static native void nativeDelete(long handle);

    public static native boolean equals(GroestlcoinAddress lhs, GroestlcoinAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public GroestlcoinAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        GroestlcoinAddressPhantomReference.register(this, nativeHandle);
    }

    public GroestlcoinAddress(PublicKey publicKey, byte prefix) {
        nativeHandle = nativeCreateWithPublicKey(publicKey, prefix);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        GroestlcoinAddressPhantomReference.register(this, nativeHandle);
    }

}

class GroestlcoinAddressPhantomReference extends java.lang.ref.PhantomReference<GroestlcoinAddress> {
    private static java.util.Set<GroestlcoinAddressPhantomReference> references = new HashSet<GroestlcoinAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<GroestlcoinAddress> queue = new java.lang.ref.ReferenceQueue<GroestlcoinAddress>();
    private long nativeHandle;

    private GroestlcoinAddressPhantomReference(GroestlcoinAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(GroestlcoinAddress referent, long nativeHandle) {
        references.add(new GroestlcoinAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        GroestlcoinAddressPhantomReference ref = (GroestlcoinAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (GroestlcoinAddressPhantomReference) queue.poll()) {
            GroestlcoinAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
