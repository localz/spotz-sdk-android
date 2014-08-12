package com.localz.spotz.sdk.api.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import java.security.spec.KeySpec;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class ObscuredSharedPreferences implements SharedPreferences {
    private static final byte[] SALT = {'b','j','2','D','7','G','T','2'};
    private static final byte[] IV = {'b','j','2','D','7','G','T','2','b','j','2','D','7','G','T','2'};
    private static final int ITERATION_COUNT = 1024;
    private static final int KEY_LENGTH = 128;

    private Cipher eCipher;
    private Cipher dCipher;

    protected SharedPreferences delegate;
    protected Context context;

    public ObscuredSharedPreferences(Context context, SharedPreferences delegate) {
        this.delegate = delegate;
        this.context = context;

        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec("L0calzSp0tz937".toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH);
            SecretKey secretKeyTemp = secretKeyFactory.generateSecret(keySpec);
            SecretKey secretKey = new SecretKeySpec(secretKeyTemp.getEncoded(), "AES");

            IvParameterSpec params = new IvParameterSpec(IV);

            eCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            eCipher.init(Cipher.ENCRYPT_MODE, secretKey, params);

            dCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            dCipher.init(Cipher.DECRYPT_MODE, secretKey, params);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize encryption", e);
        }
    }

    public class Editor implements SharedPreferences.Editor {
        protected SharedPreferences.Editor delegate;

        public Editor() {
            this.delegate = ObscuredSharedPreferences.this.delegate.edit();
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            delegate.putString(key, encrypt(Boolean.toString(value)));
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            delegate.putString(key, encrypt(Float.toString(value)));
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            delegate.putString(key, encrypt(Integer.toString(value)));
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            delegate.putString(key, encrypt(Long.toString(value)));
            return this;
        }

        @Override
        public Editor putString(String key, String value) {
            delegate.putString(key, encrypt(value));
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void apply() {
            delegate.apply();
        }

        @Override
        public Editor clear() {
            delegate.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return delegate.commit();
        }

        @Override
        public Editor remove(String s) {
            delegate.remove(s);
            return this;
        }
    }

    public Editor edit() {
        return new Editor();
    }


    @Override
    public Map<String, ?> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? Boolean.parseBoolean(decrypt(v)) : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? Float.parseFloat(decrypt(v)) : defValue;
    }

    @Override
    public int getInt(String key, int defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? Integer.parseInt(decrypt(v)) : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? Long.parseLong(decrypt(v)) : defValue;
    }

    @Override
    public String getString(String key, String defValue) {
        final String v = delegate.getString(key, null);
        return v != null ? decrypt(v) : defValue;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(String s) {
        return delegate.contains(s);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        delegate.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        delegate.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    private String encrypt(String encrypt) {
        if (TextUtils.isEmpty(encrypt)) return null;
        try {
            byte[] bytes = encrypt.getBytes("UTF-8");
            byte[] encrypted = encrypt(bytes);
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not encrypt", e);
        }
    }

    private byte[] encrypt(byte[] plain) {
        try {
            return eCipher.doFinal(plain);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not encrypt", e);
        }

    }

    private String decrypt(String encrypt) {
        try {
            byte[] bytes = Base64.decode(encrypt, Base64.DEFAULT);
            byte[] decrypted = decrypt(bytes);
            return new String(decrypted, "UTF-8");
        }
        catch (Exception e) {
            throw new RuntimeException("Could not decrypt", e);
        }
    }

    private byte[] decrypt(byte[] encrypt) {
        try {
            return dCipher.doFinal(encrypt);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not decrypt", e);
        }
    }
}