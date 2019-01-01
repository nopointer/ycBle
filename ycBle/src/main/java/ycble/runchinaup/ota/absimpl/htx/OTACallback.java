package ycble.runchinaup.ota.absimpl.htx;

public interface OTACallback {

    void onSuccess();

    void onFailure();

    void onProgress(int progress);
}
