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

public class IOSTAccount {
    private long nativeHandle;

    private IOSTAccount() {
        nativeHandle = 0;
    }

    static IOSTAccount createFromNative(long nativeHandle) {
        IOSTAccount instance = new IOSTAccount();
        instance.nativeHandle = nativeHandle;
        IOSTAccountPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreateWithString(String string);
    static native void nativeDelete(long handle);

    public static native boolean equals(IOSTAccount lhs, IOSTAccount rhs);
    public static native boolean isValidString(String string);
    public native String description();

    public IOSTAccount(String string) {
        nativeHandle = nativeCreateWithString(string);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        IOSTAccountPhantomReference.register(this, nativeHandle);
    }

}

class IOSTAccountPhantomReference extends java.lang.ref.PhantomReference<IOSTAccount> {
    private static java.util.Set<IOSTAccountPhantomReference> references = new HashSet<IOSTAccountPhantomReference>();
    private static java.lang.ref.ReferenceQueue<IOSTAccount> queue = new java.lang.ref.ReferenceQueue<IOSTAccount>();
    private long nativeHandle;

    private IOSTAccountPhantomReference(IOSTAccount referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(IOSTAccount referent, long nativeHandle) {
        references.add(new IOSTAccountPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        IOSTAccountPhantomReference ref = (IOSTAccountPhantomReference) queue.poll();
        for (; ref != null; ref = (IOSTAccountPhantomReference) queue.poll()) {
            IOSTAccount.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
