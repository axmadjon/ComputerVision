// Copyright (c) 2017-present boyw165
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.my.jni.dlib;

import android.graphics.Bitmap;
import android.util.Log;

public class DLibLandmarks68Detector {

    private static final Object mObject = new Object();

    private static DLibLandmarks68Detector detectorInstance;

    public static DLibLandmarks68Detector getDetectorInstance(String landmarkPath, String recognitionPath) {
        if (detectorInstance == null) {
            synchronized (mObject) {
                if (detectorInstance == null) {
                    detectorInstance = new DLibLandmarks68Detector();
                    detectorInstance.prepareLandmark(landmarkPath);
                    detectorInstance.prepareRecognition(recognitionPath);
                }
            }
        }
        return detectorInstance;
    }

    private DLibLandmarks68Detector() {
        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("c++_shared");
            Log.d("jni", "libc++_shared.so is loaded");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                    "\"c++_shared\" not found; check that the correct native " +
                            "libraries are present in the APK.");
        }

        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("dlib");
            Log.d("jni", "libdlib.so is loaded");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                    "\"dlib\" not found; check that the correct native libraries " +
                            "are present in the APK.");
        }

        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("dlib_jni");
            Log.d("jni", "libdlib_jni.so is loaded");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                    "\"dlib_jni\" not found; check that the correct native " +
                            "libraries are present in the APK.");
        }
    }

    public native boolean isFaceLandmarksDetectorReady();

    public native void prepareUserFaces(String userName, String[][] encode);

    public native void prepareLandmark(String path);

    public native void prepareRecognition(String path);

    public native String recognitionContains(Bitmap bitmap, long left, long top, long right, long bottom);

    public native String[] recognitionFace(Bitmap bitmap, long left, long top, long right, long bottom);
}
