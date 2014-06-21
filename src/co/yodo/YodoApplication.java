package co.yodo;

import org.acra.*;
import org.acra.annotation.*;

import android.app.Application;

@ReportsCrashes(formKey = "", 
                formUri = "http://bytegolem.com/MAB-LAB/report/report.php", 
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = org.acra.sender.HttpSender.Method.POST,
                reportType = org.acra.sender.HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.crash_toast_text)
public class YodoApplication extends Application {
	@Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}