package ycble.runchinaup.ota.callback;

import ycble.runchinaup.ota.OTAState;

/**
 * ota 回调
 */
public abstract class OTACallback {


    /**
     * 失败
     *
     * @param message
     */
    public abstract void onFailure(int code, String message);

    /**
     * 成功
     */
    public abstract void onSuccess();

    /**
     * 进度
     *
     * @param progress
     */
    public abstract void onProgress(int progress);

    /**
     * 当前状态
     */
    public void onCurrentState(OTAState otaState) {

    }


}
