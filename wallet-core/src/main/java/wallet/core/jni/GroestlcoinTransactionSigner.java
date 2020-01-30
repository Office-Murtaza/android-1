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

public class GroestlcoinTransactionSigner {
    private long nativeHandle;

    private GroestlcoinTransactionSigner() {
        nativeHandle = 0;
    }

    static GroestlcoinTransactionSigner createFromNative(long nativeHandle) {
        GroestlcoinTransactionSigner instance = new GroestlcoinTransactionSigner();
        instance.nativeHandle = nativeHandle;
        GroestlcoinTransactionSignerPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreate(wallet.core.jni.proto.Bitcoin.SigningInput input);
    static native long nativeCreateWithPlan(wallet.core.jni.proto.Bitcoin.SigningInput input, wallet.core.jni.proto.Bitcoin.TransactionPlan plan);
    static native void nativeDelete(long handle);

    public native wallet.core.jni.proto.Bitcoin.TransactionPlan plan();
    public native wallet.core.jni.proto.Common.Result sign();

    public GroestlcoinTransactionSigner(wallet.core.jni.proto.Bitcoin.SigningInput input) {
        nativeHandle = nativeCreate(input);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        GroestlcoinTransactionSignerPhantomReference.register(this, nativeHandle);
    }

    public GroestlcoinTransactionSigner(wallet.core.jni.proto.Bitcoin.SigningInput input, wallet.core.jni.proto.Bitcoin.TransactionPlan plan) {
        nativeHandle = nativeCreateWithPlan(input, plan);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        GroestlcoinTransactionSignerPhantomReference.register(this, nativeHandle);
    }

}

class GroestlcoinTransactionSignerPhantomReference extends java.lang.ref.PhantomReference<GroestlcoinTransactionSigner> {
    private static java.util.Set<GroestlcoinTransactionSignerPhantomReference> references = new HashSet<GroestlcoinTransactionSignerPhantomReference>();
    private static java.lang.ref.ReferenceQueue<GroestlcoinTransactionSigner> queue = new java.lang.ref.ReferenceQueue<GroestlcoinTransactionSigner>();
    private long nativeHandle;

    private GroestlcoinTransactionSignerPhantomReference(GroestlcoinTransactionSigner referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(GroestlcoinTransactionSigner referent, long nativeHandle) {
        references.add(new GroestlcoinTransactionSignerPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        GroestlcoinTransactionSignerPhantomReference ref = (GroestlcoinTransactionSignerPhantomReference) queue.poll();
        for (; ref != null; ref = (GroestlcoinTransactionSignerPhantomReference) queue.poll()) {
            GroestlcoinTransactionSigner.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
