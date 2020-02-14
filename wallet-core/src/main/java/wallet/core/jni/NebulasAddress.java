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

public class NebulasAddress {
    private long nativeHandle;

    private NebulasAddress() {
        nativeHandle = 0;
    }

    static NebulasAddress createFromNative(long nativeHandle) {
        NebulasAddress instance = new NebulasAddress();
        instance.nativeHandle = nativeHandle;
        NebulasAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(NebulasAddress lhs, NebulasAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native byte[] keyHash();

    public NebulasAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        NebulasAddressPhantomReference.register(this, nativeHandle);
    }

    public NebulasAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        NebulasAddressPhantomReference.register(this, nativeHandle);
    }

}

class NebulasAddressPhantomReference extends java.lang.ref.PhantomReference<NebulasAddress> {
    private static java.util.Set<NebulasAddressPhantomReference> references = new HashSet<NebulasAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<NebulasAddress> queue = new java.lang.ref.ReferenceQueue<NebulasAddress>();
    private long nativeHandle;

    private NebulasAddressPhantomReference(NebulasAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(NebulasAddress referent, long nativeHandle) {
        references.add(new NebulasAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        NebulasAddressPhantomReference ref = (NebulasAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (NebulasAddressPhantomReference) queue.poll()) {
            NebulasAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
