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

public class NEARAccount {
    private long nativeHandle;

    private NEARAccount() {
        nativeHandle = 0;
    }

    static NEARAccount createFromNative(long nativeHandle) {
        NEARAccount instance = new NEARAccount();
        instance.nativeHandle = nativeHandle;
        NEARAccountPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native void nativeDelete(long handle);

    public native String description();

    public NEARAccount(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        NEARAccountPhantomReference.register(this, nativeHandle);
    }

}

class NEARAccountPhantomReference extends java.lang.ref.PhantomReference<NEARAccount> {
    private static java.util.Set<NEARAccountPhantomReference> references = new HashSet<NEARAccountPhantomReference>();
    private static java.lang.ref.ReferenceQueue<NEARAccount> queue = new java.lang.ref.ReferenceQueue<NEARAccount>();
    private long nativeHandle;

    private NEARAccountPhantomReference(NEARAccount referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(NEARAccount referent, long nativeHandle) {
        references.add(new NEARAccountPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        NEARAccountPhantomReference ref = (NEARAccountPhantomReference) queue.poll();
        for (; ref != null; ref = (NEARAccountPhantomReference) queue.poll()) {
            NEARAccount.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}