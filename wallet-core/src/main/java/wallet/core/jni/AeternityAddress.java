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

public class AeternityAddress {
    private long nativeHandle;

    private AeternityAddress() {
        nativeHandle = 0;
    }

    static AeternityAddress createFromNative(long nativeHandle) {
        AeternityAddress instance = new AeternityAddress();
        instance.nativeHandle = nativeHandle;
        AeternityAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(AeternityAddress lhs, AeternityAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public AeternityAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        AeternityAddressPhantomReference.register(this, nativeHandle);
    }

    public AeternityAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        AeternityAddressPhantomReference.register(this, nativeHandle);
    }

}

class AeternityAddressPhantomReference extends java.lang.ref.PhantomReference<AeternityAddress> {
    private static java.util.Set<AeternityAddressPhantomReference> references = new HashSet<AeternityAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<AeternityAddress> queue = new java.lang.ref.ReferenceQueue<AeternityAddress>();
    private long nativeHandle;

    private AeternityAddressPhantomReference(AeternityAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(AeternityAddress referent, long nativeHandle) {
        references.add(new AeternityAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        AeternityAddressPhantomReference ref = (AeternityAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (AeternityAddressPhantomReference) queue.poll()) {
            AeternityAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
