/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.util.Log;

import java.io.FileDescriptor;

/**
 * Native binding for the Pinyin decoder engine ported from AOSP PinyinIME.
 *
 * <p>This class only exists to hold the {@code native} method declarations and
 * load {@code libjni_pinyinime}. The JNI bridge
 * ({@code com_android_inputmethod_pinyin_PinyinDecoderService.cpp}) hardcodes
 * this fully-qualified class name in its {@code RegisterNatives} call, so the
 * name and package must not change, and every method listed in the native
 * {@code gMethods[]} table must remain declared here.
 *
 * <p>Unlike the original PinyinIME, this is NOT an Android {@link
 * android.app.Service} bound over AIDL; the decoder runs in-process and is
 * driven directly by {@link PinyinDecoder}. The decoder is not reentrant (it
 * uses file-scope static buffers), so all calls must come from a single thread
 * (the IME main thread).
 */
public class PinyinDecoderService {
    static native boolean nativeImOpenDecoder(byte fn_sys_dict[],
            byte fn_usr_dict[]);

    static native boolean nativeImOpenDecoderFd(FileDescriptor fd,
            long startOffset, long length, byte fn_usr_dict[]);

    static native void nativeImSetMaxLens(int maxSpsLen, int maxHzsLen);

    static native boolean nativeImCloseDecoder();

    static native int nativeImSearch(byte pyBuf[], int pyLen);

    static native int nativeImDelSearch(int pos, boolean is_pos_in_splid,
            boolean clear_fixed_this_step);

    static native void nativeImResetSearch();

    static native int nativeImAddLetter(byte ch);

    static native String nativeImGetPyStr(boolean decoded);

    static native int nativeImGetPyStrLen(boolean decoded);

    static native int[] nativeImGetSplStart();

    static native String nativeImGetChoice(int choiceId);

    static native int nativeImChoose(int choiceId);

    static native int nativeImCancelLastChoice();

    static native int nativeImGetFixedLen();

    static native boolean nativeImCancelInput();

    static native boolean nativeImFlushCache();

    static native int nativeImGetPredictsNum(String fixedStr);

    static native String nativeImGetPredictItem(int predictNo);

    // Sync-related natives. Unused by the in-process decoder, but they are part
    // of the native gMethods[] registration table and must stay declared so
    // RegisterNatives succeeds.
    static native boolean nativeSyncBegin(byte[] user_dict);

    static native boolean nativeSyncFinish();

    static native int nativeSyncPutLemmas(String tomerge);

    static native String nativeSyncGetLemmas();

    static native int nativeSyncGetLastCount();

    static native int nativeSyncGetTotalCount();

    static native boolean nativeSyncClearLastGot();

    static native int nativeSyncGetCapacity();

    static {
        try {
            System.loadLibrary("jni_pinyinime");
        } catch (UnsatisfiedLinkError ule) {
            Log.e("PinyinDecoderService",
                    "WARNING: Could not load jni_pinyinime natives");
        }
    }

    private PinyinDecoderService() {
    }
}
