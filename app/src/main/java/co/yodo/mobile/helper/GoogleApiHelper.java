package co.yodo.mobile.helper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hei on 12/08/17.
 * Wrapper for the Google API
 */
public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    /** Auto increment to manage the google api */
    private static final AtomicInteger SAFE_ID = new AtomicInteger(10);

    /** Provides an entry point for Google Play services. */
    private GoogleApiClient googleApiClient;
    private TaskCompletionSource<Bundle> googleApiConnectionTask = new TaskCompletionSource<>();

    protected GoogleApiHelper(FragmentActivity activity, GoogleApiClient.Builder builder) {
        builder.enableAutoManage(activity, getSafeAutoManageId(), this);
        builder.addConnectionCallbacks(this);
        googleApiClient = builder.build();
    }

    /**
     * @return a safe id for {@link GoogleApiClient.Builder#enableAutoManage(FragmentActivity, int,
     * GoogleApiClient.OnConnectionFailedListener)}
     */
    public static int getSafeAutoManageId() {
        return SAFE_ID.getAndIncrement();
    }

    public Task<Bundle> getConnectedApiTask() {
        return googleApiConnectionTask.getTask();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // onConnected might be called multiple times, but we don't want to unregister listeners
        // because extenders might be relying on each onConnected call. Instead, we just ignore future
        // calls to onConnected or onConnectionFailed by using a `trySomething` strategy.
        googleApiConnectionTask.trySetResult(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Just wait
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        googleApiConnectionTask.trySetException(new ConnectException(result.toString()));
    }

    protected static final class TaskResultCaptor<R extends Result> implements ResultCallback<R> {
        private TaskCompletionSource<R> source;

        public TaskResultCaptor(TaskCompletionSource<R> source) {
            this.source = source;
        }

        @Override
        public void onResult(@NonNull R result) {
            source.setResult(result);
        }
    }

    protected static class ExceptionForwarder<TResult> implements OnCompleteListener<TResult> {
        private TaskCompletionSource source;
        private OnSuccessListener<TResult> listener;

        public ExceptionForwarder(TaskCompletionSource source, OnSuccessListener<TResult> listener) {
            this.source = source;
            this.listener = listener;
        }

        @Override
        public void onComplete(@NonNull Task<TResult> task) {
            if (task.isSuccessful()) {
                listener.onSuccess(task.getResult());
            } else {
                Exception exception = task.getException();
                if (exception != null) {
                    source.setException(exception);
                }
            }
        }
    }
}
