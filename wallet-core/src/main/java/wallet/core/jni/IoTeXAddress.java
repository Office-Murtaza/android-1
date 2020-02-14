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

public class IoTeXAddress {
    private long nativeHandle;

    private IoTeXAddress() {
        nativeHandle = 0;
    }

    static IoTeXAddress createFromNative(long nativeHandle) {
        IoTeXAddress instance = new IoTeXAddress();
        instance.nativeHandle = nativeHandle;
        IoTeXAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithKeyHash(byte[] keyHash);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(IoTeXAddress lhs, IoTeXAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();
    public native byte[] keyHash();

    public IoTeXAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        IoTeXAddressPhantomReference.register(this, nativeHandle);
    }

    public IoTeXAddress(byte[] keyHash) {
        nativeHandle = nativeCreateWithKeyHash(keyHash);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        IoTeXAddressPhantomReference.register(this, nativeHandle);
    }

    public IoTeXAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        IoTeXAddressPhantomReference.register(this, nativeHandle);
    }

}

class IoTeXAddressPhantomReference extends java.lang.ref.PhantomReference<IoTeXAddress> {
    private static java.util.Set<IoTeXAddressPhantomReference> references = new HashSet<IoTeXAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<IoTeXAddress> queue = new java.lang.ref.ReferenceQueue<IoTeXAddress>();
    private long nativeHandle;

    private IoTeXAddressPhantomReference(IoTeXAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(IoTeXAddress referent, long nativeHandle) {
        references.add(new IoTeXAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        IoTeXAddressPhantomReference ref = (IoTeXAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (IoTeXAddressPhantomReference) queue.poll()) {
            IoTeXAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
