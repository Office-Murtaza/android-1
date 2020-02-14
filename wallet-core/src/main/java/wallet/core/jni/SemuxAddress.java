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

public class SemuxAddress {
    private long nativeHandle;

    private SemuxAddress() {
        nativeHandle = 0;
    }

    static SemuxAddress createFromNative(long nativeHandle) {
        SemuxAddress instance = new SemuxAddress();
        instance.nativeHandle = nativeHandle;
        SemuxAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(SemuxAddress lhs, SemuxAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public SemuxAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        SemuxAddressPhantomReference.register(this, nativeHandle);
    }

    public SemuxAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        SemuxAddressPhantomReference.register(this, nativeHandle);
    }

}

class SemuxAddressPhantomReference extends java.lang.ref.PhantomReference<SemuxAddress> {
    private static java.util.Set<SemuxAddressPhantomReference> references = new HashSet<SemuxAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<SemuxAddress> queue = new java.lang.ref.ReferenceQueue<SemuxAddress>();
    private long nativeHandle;

    private SemuxAddressPhantomReference(SemuxAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(SemuxAddress referent, long nativeHandle) {
        references.add(new SemuxAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        SemuxAddressPhantomReference ref = (SemuxAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (SemuxAddressPhantomReference) queue.poll()) {
            SemuxAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
