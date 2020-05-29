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

public class StoredKey {
    private long nativeHandle;

    private StoredKey() {
        nativeHandle = 0;
    }

    static StoredKey createFromNative(long nativeHandle) {
        StoredKey instance = new StoredKey();
        instance.nativeHandle = nativeHandle;
        StoredKeyPhantomReference.register(instance, nativeHandle);
        return instance;
    }

    static native long nativeCreate(String name, byte[] password);
    static native void nativeDelete(long handle);

    public static native StoredKey load(String path);
    public static native StoredKey importPrivateKey(byte[] privateKey, String name, byte[] password, CoinType coin);
    public static native StoredKey importHDWallet(String mnemonic, String name, byte[] password, CoinType coin);
    public static native StoredKey importJSON(byte[] json);
    public native String identifier();
    public native String name();
    public native boolean isMnemonic();
    public native int accountCount();
    public native Account account(int index);
    public native Account accountForCoin(CoinType coin, HDWallet wallet);
    public native void removeAccountForCoin(CoinType coin);
    public native void addAccount(String address, String derivationPath, String extetndedPublicKey);
    public native boolean store(String path);
    public native byte[] decryptPrivateKey(byte[] password);
    public native String decryptMnemonic(byte[] password);
    public native PrivateKey privateKey(CoinType coin, byte[] password);
    public native HDWallet wallet(byte[] password);
    public native byte[] exportJSON();
    public native boolean fixAddresses(byte[] password);

    public StoredKey(String name, byte[] password) {
        nativeHandle = nativeCreate(name, password);
        if (nativeHandle == 0) {
            throw new InvalidParameterException();
        }

        StoredKeyPhantomReference.register(this, nativeHandle);
    }

}

class StoredKeyPhantomReference extends java.lang.ref.PhantomReference<StoredKey> {
    private static java.util.Set<StoredKeyPhantomReference> references = new HashSet<StoredKeyPhantomReference>();
    private static java.lang.ref.ReferenceQueue<StoredKey> queue = new java.lang.ref.ReferenceQueue<StoredKey>();
    private long nativeHandle;

    private StoredKeyPhantomReference(StoredKey referent, long nativeHandle) {
        super(referent, queue);
        this.nativeHandle = nativeHandle;
    }

    static void register(StoredKey referent, long nativeHandle) {
        references.add(new StoredKeyPhantomReference(referent, nativeHandle));
    }

    public static void doDeletes() {
        StoredKeyPhantomReference ref = (StoredKeyPhantomReference) queue.poll();
        for (; ref != null; ref = (StoredKeyPhantomReference) queue.poll()) {
            StoredKey.nativeDelete(ref.nativeHandle);
            references.remove(ref);
        }
    }
}
