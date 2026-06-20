/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.pinyin;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.android.inputmethod.latin.R;

import java.io.File;
import java.io.IOException;

/**
 * Thin in-process wrapper over the native Pinyin decoder
 * ({@link PinyinDecoderService}). Owns the dictionary lifecycle and exposes the
 * subset of decoder calls the IME needs.
 *
 * <p>The native engine is NOT reentrant; all methods must be called from the
 * IME main thread.
 */
public class PinyinDecoder {
    private static final String TAG = "PinyinDecoder";
    private static final String USR_DICT_FILE = "usr_dict.dat";

    private boolean mInited;

    /** Opens the built-in dictionary. Idempotent; safe to call repeatedly. */
    public boolean open(final Context context) {
        if (mInited) {
            return true;
        }
        final File usrDict = new File(context.getFilesDir(), USR_DICT_FILE);
        final byte[] usrDictPath = toCString(usrDict.getAbsolutePath());

        AssetFileDescriptor afd = null;
        try {
            afd = context.getResources().openRawResourceFd(R.raw.dict_pinyin);
            mInited = PinyinDecoderService.nativeImOpenDecoderFd(
                    afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength(), usrDictPath);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Pinyin native library not available", e);
        } finally {
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        if (!mInited) {
            Log.e(TAG, "Failed to open Pinyin decoder");
        }
        return mInited;
    }

    public void close() {
        if (mInited) {
            try {
                PinyinDecoderService.nativeImCloseDecoder();
            } catch (UnsatisfiedLinkError e) {
                // ignore
            }
            mInited = false;
        }
    }

    public boolean isInited() {
        return mInited;
    }

    public int search(final byte[] pyBuf, final int pyLen) {
        return PinyinDecoderService.nativeImSearch(pyBuf, pyLen);
    }

    public void resetSearch() {
        PinyinDecoderService.nativeImResetSearch();
    }

    public String getChoice(final int candId) {
        return PinyinDecoderService.nativeImGetChoice(candId);
    }

    private static byte[] toCString(final String s) {
        final byte[] raw = s.getBytes();
        final byte[] out = new byte[raw.length + 1];
        System.arraycopy(raw, 0, out, 0, raw.length);
        out[raw.length] = 0;
        return out;
    }
}
