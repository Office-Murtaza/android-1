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

public class ARKAddress {
    private long nativeHandle;

    private ARKAddress() {
        nativeHandle = 0;
    }

    static ARKAddress createFromNative(long nativeHandle) {
        ARKAddress instance = new ARKAddress();
        instance.nativeHandle = nativeHandle;
        ARKAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(ARKAddress lhs, ARKAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public ARKAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        ARKAddressPhantomReference.register(this, nativeHandle);
    }

    public ARKAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        ARKAddressPhantomReference.register(this, nativeHandle);
    }

}

class ARKAddressPhantomReference extends java.lang.ref.PhantomReference<ARKAddress> {
    private static java.util.Set<ARKAddressPhantomReference> references = new HashSet<ARKAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<ARKAddress> queue = new java.lang.ref.ReferenceQueue<ARKAddress>();
    private long nativeHandle;

    private ARKAddressPhantomReference(ARKAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(ARKAddress referent, long nativeHandle) {
        references.add(new ARKAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        ARKAddressPhantomReference ref = (ARKAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (ARKAddressPhantomReference) queue.poll()) {
            ARKAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
