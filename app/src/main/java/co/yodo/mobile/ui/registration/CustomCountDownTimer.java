package co.yodo.mobile.ui.registration;

import android.os.CountDownTimer;

abstract class CustomCountDownTimer {
    private final long millisInFuture;
    private final long countDownInterval;
    private CountDownTimer countDownTimer;

    CustomCountDownTimer(long millisInFuture, long countDownInterval) {
        this.millisInFuture = millisInFuture;
        this.countDownInterval = countDownInterval;
        this.countDownTimer = create(millisInFuture, countDownInterval);
    }

    public void update(long millisInFuture) {
        countDownTimer.cancel();
        countDownTimer = create(millisInFuture, countDownInterval);
        countDownTimer.start();
    }

    void renew() {
        update(millisInFuture);
    }

    private CountDownTimer create(long millisInFuture, long countDownInterval) {
        return new CountDownTimer(millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                CustomCountDownTimer.this.onTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                CustomCountDownTimer.this.onFinish();
            }
        };
    }

    public void cancel() {
        countDownTimer.cancel();
    }

    public void start() {
        countDownTimer.start();
    }

    protected abstract void onFinish();

    protected abstract void onTick(long millisUntilFinished);
}