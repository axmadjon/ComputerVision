package uz.greenwhite.facetrack.common;

public interface ICameraMetadata {

    boolean isFacingFront();

    boolean isFacingBack();

    boolean isPortraitMode();

    boolean isLandscapeMode();
}
