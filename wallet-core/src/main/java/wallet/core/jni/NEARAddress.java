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

public class NEARAddress {
    private long nativeHandle;

    private NEARAddress() {
        nativeHandle = 0;
    }

    static NEARAddress createFromNative(long nativeHandle) {
        NEARAddress instance = new NEARAddress();
        instance.nativeHandle = nativeHandle;
        NEARAddressPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native long nativeCreateWithPublicKey(PublicKey publicKey);
    static native void nativeDelete(long handle);

    public static native boolean equals(NEARAddress lhs, NEARAddress rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public NEARAddress(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        NEARAddressPhantomReference.register(this, nativeHandle);
    }

    public NEARAddress(PublicKey publicKey) {
        nativeHandle = nativeCreateWithPublicKey(publicKey);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        NEARAddressPhantomReference.register(this, nativeHandle);
    }

}

class NEARAddressPhantomReference extends java.lang.ref.PhantomReference<NEARAddress> {
    private static java.util.Set<NEARAddressPhantomReference> references = new HashSet<NEARAddressPhantomReference>();
    private static java.lang.ref.ReferenceQueue<NEARAddress> queue = new java.lang.ref.ReferenceQueue<NEARAddress>();
    private long nativeHandle;

    private NEARAddressPhantomReference(NEARAddress referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(NEARAddress referent, long nativeHandle) {
        references.add(new NEARAddressPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        NEARAddressPhantomReference ref = (NEARAddressPhantomReference) queue.poll();
        for (; ref != null; ref = (NEARAddressPhantomReference) queue.poll()) {
            NEARAddress.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
