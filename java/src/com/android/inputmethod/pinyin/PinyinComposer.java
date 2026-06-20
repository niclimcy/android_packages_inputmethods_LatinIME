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

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the raw pinyin being typed and the Chinese candidates decoded from it.
 * The raw pinyin is the source of truth; every keystroke re-decodes the whole
 * string in a fresh engine session.
 */
public class PinyinComposer {
    private static final int PY_STRING_MAX = 27;
    private static final int MAX_CANDIDATES = 18;

    private final PinyinDecoder mDecoder;
    private final StringBuilder mSurface = new StringBuilder();
    private final byte[] mPyBuf = new byte[PY_STRING_MAX + 1];
    private final List<String> mCandidates = new ArrayList<>();

    public PinyinComposer(final PinyinDecoder decoder) {
        mDecoder = decoder;
    }

    public void reset() {
        mSurface.setLength(0);
        mCandidates.clear();
        if (mDecoder.isInited()) {
            mDecoder.resetSearch();
        }
    }

    public boolean isEmpty() {
        return mSurface.length() == 0;
    }

    public List<String> getCandidates() {
        return mCandidates;
    }

    public String getComposingText() {
        return mSurface.toString();
    }

    // Candidate 0, or the raw pinyin if there are no candidates.
    public String getBestCandidate() {
        return mCandidates.isEmpty() ? mSurface.toString() : mCandidates.get(0);
    }

    public void addLetter(final char ch) {
        if (mSurface.length() >= PY_STRING_MAX) {
            return;
        }
        mSurface.append(ch);
        research();
    }

    public void deleteLast() {
        if (mSurface.length() == 0) {
            return;
        }
        mSurface.deleteCharAt(mSurface.length() - 1);
        research();
    }

    private void research() {
        mCandidates.clear();
        if (!mDecoder.isInited() || mSurface.length() == 0) {
            return;
        }
        // Fresh session each keystroke, so the result never depends on prior state.
        mDecoder.resetSearch();
        final int len = mSurface.length();
        for (int i = 0; i < len; i++) {
            mPyBuf[i] = (byte) mSurface.charAt(i);
        }
        mPyBuf[len] = 0;
        final int total = mDecoder.search(mPyBuf, len);
        final int fetch = Math.min(total, MAX_CANDIDATES);
        for (int i = 0; i < fetch; i++) {
            final String s = mDecoder.getChoice(i);
            if (s != null && !s.isEmpty()) {
                mCandidates.add(s);
            }
        }
    }
}
