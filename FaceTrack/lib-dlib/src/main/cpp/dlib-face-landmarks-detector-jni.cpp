#include <android/log.h>
#include <android/bitmap.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/image_io.h>
#include <dlib/dnn.h>
#include <dlib/clustering.h>

using namespace dlib;
using namespace std;

// ----------------------------------------------------------------------------------------
template<template<int, template<typename> class, int, typename> class block, int N,
        template<typename> class BN, typename SUBNET>
using residual = add_prev1<block<N, BN, 1, tag1<SUBNET>>>;

template<template<int, template<typename> class, int, typename> class block, int N,
        template<typename> class BN, typename SUBNET>
using residual_down = add_prev2<avg_pool<2, 2, 2, 2, skip1<tag2<block<N, BN, 2, tag1<SUBNET>>>>>>;

template<int N, template<typename> class BN, int stride, typename SUBNET>
using block  = BN<con<N, 3, 3, 1, 1, relu<BN<con<N, 3, 3, stride, stride, SUBNET>>>>>;

template<int N, typename SUBNET> using ares      = relu<residual<block, N, affine, SUBNET>>;
template<int N, typename SUBNET> using ares_down = relu<residual_down<block, N, affine, SUBNET>>;

template<typename SUBNET> using alevel0 = ares_down<256, SUBNET>;
template<typename SUBNET> using alevel1 = ares<256, ares<256, ares_down<256, SUBNET>>>;
template<typename SUBNET> using alevel2 = ares<128, ares<128, ares_down<128, SUBNET>>>;
template<typename SUBNET> using alevel3 = ares<64, ares<64, ares<64, ares_down<64, SUBNET>>>>;
template<typename SUBNET> using alevel4 = ares<32, ares<32, ares<32, SUBNET>>>;

using anet_type = loss_metric<fc_no_bias<128, avg_pool_everything<
        alevel0<alevel1<alevel2<alevel3<alevel4<max_pool<3, 3, 2, 2, relu<affine<con<32, 7, 7, 2, 2,
                input_rgb_image_sized<150>>>>>>>>>>>>>;

// ----------------------------------------------------------------------------------------

void throwException(JNIEnv *env, const char *message) {
    jclass Exception = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(Exception, message);
}

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "dlib-jni:", __VA_ARGS__))

#define JNI_METHOD(NAME) \
    Java_com_my_jni_dlib_DLibLandmarks68Detector_##NAME

// FIXME: Create a class inheriting from dlib::array2d<dlib::rgb_pixel>.
void convertBitmapToArray2d(JNIEnv *env, jobject bitmap, dlib::array2d<dlib::rgb_pixel> &out) {
    AndroidBitmapInfo bitmapInfo;
    void *pixels;
    int state;

    if (0 > (state = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo))) {
        LOGI("L%d: AndroidBitmap_getInfo() failed! error=%d", __LINE__, state);
        throwException(env, "AndroidBitmap_getInfo() failed!");
        return;
    } else if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGI("L%d: Bitmap format is not RGB_565!", __LINE__);
        throwException(env, "Bitmap format is not RGB_565!");
    }

    // Lock the bitmap for copying the pixels safely.
    if (0 > (state = AndroidBitmap_lockPixels(env, bitmap, &pixels))) {
        LOGI("L%d: AndroidBitmap_lockPixels() failed! error=%d", __LINE__, state);
        throwException(env, "AndroidBitmap_lockPixels() failed!");
        return;
    }

    LOGI("L%d: info.width=%d, info.height=%d", __LINE__, bitmapInfo.width, bitmapInfo.height);
    out.set_size((long) bitmapInfo.height, (long) bitmapInfo.width);

    char *line = (char *) pixels;
    for (int h = 0; h < bitmapInfo.height; ++h) {
        for (int w = 0; w < bitmapInfo.width; ++w) {
            uint32_t *color = (uint32_t *) (line + 4 * w);

            out[h][w].red = (unsigned char) (0xFF & ((*color) >> 24));
            out[h][w].green = (unsigned char) (0xFF & ((*color) >> 16));
            out[h][w].blue = (unsigned char) (0xFF & ((*color) >> 8));
        }

        line = line + bitmapInfo.stride;
    }

    // Unlock the bitmap.
    AndroidBitmap_unlockPixels(env, bitmap);
}

long jStringToLong(JNIEnv *env, jstring str) {
    const char *resultStr = env->GetStringUTFChars(str, JNI_FALSE);
    long result = std::stol(resultStr);
    env->ReleaseStringUTFChars(str, resultStr);
    return result;
}

float jStringToFloat(JNIEnv *env, jstring str) {
    const char *resultStr = env->GetStringUTFChars(str, JNI_FALSE);
    float result = std::stof(resultStr);
    env->ReleaseStringUTFChars(str, resultStr);
    return result;
}

//void convertJavaFaceEncode(JNIEnv *env,
//                           jobjectArray faces,
//                           std::vector<matrix<float, 0, 1>> &out) {
//    long facesSize = (long) env->GetArrayLength(faces);
//    for (long position = 0; position < facesSize; ++position) {
//        jobjectArray faceElement = (jobjectArray) env->GetObjectArrayElement(faces, position);
//        jobjectArray firstSettingItem = (jobjectArray) env->GetObjectArrayElement(faceElement, 0);
//
//        long nr = jStringToLong(env, (jstring) env->GetObjectArrayElement(firstSettingItem, 0));
//        long nc = jStringToLong(env, (jstring) env->GetObjectArrayElement(firstSettingItem, 1));
//
//        matrix<float, 0, 1> m_matrix;
//        m_matrix.set_size(nr, nc);
//
//        long encodeSize = (long) env->GetArrayLength(faceElement);
//        for (long index = 1; index < encodeSize; ++index) {
//            jobjectArray faceEncodeElement = (jobjectArray) env->GetObjectArrayElement(faceElement, index);
//
//            long iNr = jStringToLong(env, (jstring) env->GetObjectArrayElement(faceEncodeElement, 0));
//            long iNc = jStringToLong(env, (jstring) env->GetObjectArrayElement(faceEncodeElement, 1));
//            float iValue = jStringToFloat(env, (jstring) env->GetObjectArrayElement(faceEncodeElement, 2));
//
//            m_matrix(iNr, iNc) = iValue;
//
//            env->DeleteLocalRef(faceEncodeElement);
//        }
//        out.push_back(m_matrix);
//
//        env->DeleteLocalRef(firstSettingItem);
//        env->DeleteLocalRef(faceElement);
//    }
//}

void convertJavaFaceEncodeToDlibVectorMatrix(JNIEnv *env,
                                             jobjectArray encode,
                                             std::vector<matrix<float, 0, 1>>  &out){
    long encodeSize = (long) env->GetArrayLength(encode);
    jobjectArray firstSettingItem = (jobjectArray) env->GetObjectArrayElement(encode, 0);

    long nr = jStringToLong(env, (jstring) env->GetObjectArrayElement(firstSettingItem, 0));
    long nc = jStringToLong(env, (jstring) env->GetObjectArrayElement(firstSettingItem, 1));

    matrix<float, 0, 1> m_matrix;
    m_matrix.set_size(nr, nc);
    for (long index = 1; index < encodeSize; ++index) {
        jobjectArray element = (jobjectArray) env->GetObjectArrayElement(encode, index);

        long iNr = jStringToLong(env, (jstring) env->GetObjectArrayElement(element, 0));
        long iNc = jStringToLong(env, (jstring) env->GetObjectArrayElement(element, 1));
        float iValue = jStringToFloat(env, (jstring) env->GetObjectArrayElement(element, 2));

        m_matrix(iNr, iNc) = iValue;

        env->DeleteLocalRef(element);
    }
    out.push_back(m_matrix);
    env->DeleteLocalRef(firstSettingItem);
}

// JNI ////////////////////////////////////////////////////////////////////////

dlib::shape_predictor sFaceLandmarksDetector;
anet_type sFaceRecognition;
std::vector<std::pair<string, std::vector<matrix<float, 0, 1>>>> knowFaces;

extern "C" JNIEXPORT jboolean JNICALL
JNI_METHOD(isFaceLandmarksDetectorReady)(JNIEnv *env, jobject thiz) {
    if (sFaceLandmarksDetector.num_parts() > 0) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(prepareUserFaces)(JNIEnv *env,
                             jobject thiz,
                             jstring userName,
                             jobjectArray faceEncode) {
    const char *name = env->GetStringUTFChars(userName, JNI_FALSE);
    std::string user_name = std::string(name);

    LOGI("L%d: search in knowFaces loop start", __LINE__);
    for (long index = 0; index < knowFaces.size(); ++index) {
        std::pair<string, std::vector<matrix<float, 0, 1>>> item = knowFaces[index];
        if (item.first == user_name) {
            LOGI("L%d: found in knowFaces", __LINE__);
            convertJavaFaceEncodeToDlibVectorMatrix(env, faceEncode, item.second);
            return;
        }
    }
    LOGI("L%d: NOT FOUND in knowFaces", __LINE__);

    std::pair<std::string, std::vector<matrix<float, 0, 1>>> newItem;
    string str(name);
    newItem.first = str;
    convertJavaFaceEncodeToDlibVectorMatrix(env, faceEncode, newItem.second);

    knowFaces.push_back(newItem);

    env->ReleaseStringUTFChars(userName, name);

}

extern "C" JNIEXPORT void JNICALL
JNI_METHOD(prepareLandmark)(JNIEnv *env, jobject thiz, jstring landmarkPath) {
    const char *path = env->GetStringUTFChars(landmarkPath, JNI_FALSE);
    dlib::deserialize(path) >> sFaceLandmarksDetector;
    env->ReleaseStringUTFChars(landmarkPath, path);
}


extern "C" JNIEXPORT void JNICALL
JNI_METHOD(prepareRecognition)(JNIEnv *env, jobject thiz, jstring recognitionPath) {
    const char *path = env->GetStringUTFChars(recognitionPath, JNI_FALSE);
    dlib::deserialize(path) >> sFaceRecognition;
    env->ReleaseStringUTFChars(recognitionPath, path);
}

extern "C" JNIEXPORT jstring JNICALL
JNI_METHOD(recognitionContains)(JNIEnv *env,
                                jobject thiz,
                                jobject bitmap,
                                jlong left,
                                jlong top,
                                jlong right,
                                jlong bottom) {
    dlib::array2d<dlib::rgb_pixel> img;
    convertBitmapToArray2d(env, bitmap, img);

    dlib::rectangle bound(left, top, right, bottom);
    dlib::full_object_detection shape = sFaceLandmarksDetector(img, bound);

    std::vector<matrix<rgb_pixel>> mrFaces;
    matrix<rgb_pixel> face_chip;
    extract_image_chip(img, get_face_chip_details(shape, 150, 0.25), face_chip);
    mrFaces.push_back(move(face_chip));

    std::vector<matrix<float, 0, 1>> face_descriptors = sFaceRecognition(mrFaces);

    std::string user_name = "-1";

    if (face_descriptors.size() > 0) {
        double maxAccuracy = 1.0;
        int containFacesCount = 0;

        for (long r = 0; r < knowFaces.size(); ++r) {
            std::pair<string, std::vector<matrix<float, 0, 1>>> pairItem = knowFaces[r];

            double uMaxAccuracy = 1.0;
            int uContainFacesCount = 0;

            for (long i1 = 0; i1 < pairItem.second.size(); ++i1) {
                for (long i2 = 0; i2 < face_descriptors.size(); ++i2) {

                    double faceCompareAccuracy = length(pairItem.second[i1] - face_descriptors[i2]);

                    if (faceCompareAccuracy <= 0.5) {
                        if (uMaxAccuracy > faceCompareAccuracy) {
                            uMaxAccuracy = faceCompareAccuracy;
                        }
                        uContainFacesCount = uContainFacesCount + 1;
                    }
                }
            }

            if (uMaxAccuracy != 1.0 &&
                (maxAccuracy > uMaxAccuracy || containFacesCount < uContainFacesCount)) {
                user_name = pairItem.first;
                maxAccuracy = uMaxAccuracy;
                containFacesCount = uContainFacesCount;
            }

        }
    }
    return env->NewStringUTF(user_name.c_str());
}


extern "C" JNIEXPORT jstring JNICALL
JNI_METHOD(recognitionFace)(JNIEnv *env,
                            jobject thiz,
                            jobject bitmap,
                            jlong left,
                            jlong top,
                            jlong right,
                            jlong bottom) {
    dlib::array2d<dlib::rgb_pixel> img;
    convertBitmapToArray2d(env, bitmap, img);

    dlib::rectangle bound(left, top, right, bottom);
    dlib::full_object_detection shape = sFaceLandmarksDetector(img, bound);

    std::vector<matrix<rgb_pixel>> mrFaces;
    matrix<rgb_pixel> face_chip;
    extract_image_chip(img, get_face_chip_details(shape, 150, 0.25), face_chip);
    mrFaces.push_back(move(face_chip));

    std::vector<matrix<float, 0, 1>> face_descriptors = sFaceRecognition(mrFaces);

    if (face_descriptors.size() != 1) {
        return env->NewStringUTF("[]");
    }

    matrix<float, 0, 1> item = face_descriptors[0];

    std::string result_json = "[\"" + std::to_string(item.nr()) +
            "\",\"" + std::to_string(item.nc()) +
            "\",[";


    bool first_cycler = false;
    for (long r = 0; r < item.nr(); ++r) {
        for (long c = 0; c < item.nc(); ++c) {
            if (first_cycler) {
                result_json += ",";
            }
            first_cycler = true;

            result_json += "[\"" + std::to_string(r) + "\",\""
                           + std::to_string(c) + "\",\""
                           + std::to_string(item(r, c)) + "\"]";
        }
    }


    result_json += "]]";
    return env->NewStringUTF(result_json.c_str());
}