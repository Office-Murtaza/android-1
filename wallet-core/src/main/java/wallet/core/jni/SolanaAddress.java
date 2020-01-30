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

public class SolanaAddress {
    private long nativeHandle;

    private SolanaAddress() {
        nativeHandle = 0;
    }

    static SolanaAddress createFromNative(long nativeHandle) {
        SolanaAddress instance = new SolanaAddress();
        instance.nativeHandle = nativeHandle;
        SolanaAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(SolanaAddress lhs, SolanaAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public SolanaAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        SolanaAddressPhantomReference.register(this, nativeHandle);
    }

    public SolanaAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        SolanaAddressPhantomReference.register(this, nativeHandle);
    }

}

class SolanaAddressPhantomReference extends java.lang.ref.PhantomReference<SolanaAddress> {
    private static java.util.Set<SolanaAddressPhantomReference> references = new HashSet<SolanaAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<SolanaAddress> queue = new java.lang.ref.ReferenceQueue<SolanaAddress>();
    private long nativeHandle;

    private SolanaAddressPhantomReference(SolanaAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(SolanaAddress referent, long nativeHandle) {
        references.add(new SolanaAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        SolanaAddressPhantomReference ref = (SolanaAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (SolanaAddressPhantomReference) queue.poll()) {
            SolanaAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
