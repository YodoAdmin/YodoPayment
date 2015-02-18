package co.yodo.mobile.component;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import co.yodo.mobile.helper.AppUtils;

/**
 * Created by luis on 29/01/15.
 * Display an image from url
 */
public class ImageLoader {
    /** DEBUG */
    private static final String TAG = ImageLoader.class.getSimpleName();

    /** GUI components */
    private ImageView imageView;

    /** Singleton instance */
    private static ImageLoader instance = null;

    /**
     * Gets the instance of the service
     * @return instance
     */
    public static ImageLoader getInstance() {
        if( instance == null )
            instance = new ImageLoader();
        return instance;
    }

    public void DisplayImage(String url, ImageView imageView) {
        this.imageView = imageView;
        new LoaderTask().execute( url );
    }

    private Bitmap getBitmap(String url) {
        AppUtils.Logger( TAG, url );
        //from web
        try {
            Bitmap bitmap;
            URL imageUrl = new URL( url );
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout( 30000 );
            conn.setReadTimeout( 30000 );
            conn.setInstanceFollowRedirects( true );
            conn.connect();
            bitmap = decodeFile( conn.getInputStream() );
            return bitmap;
        } catch( Exception ex ){
            ex.printStackTrace();
            return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(InputStream in) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while( (len = in.read( buffer ) ) > -1 ) {
                baos.write( buffer, 0, len );
            }
            baos.flush();
            InputStream is1 = new ByteArrayInputStream( baos.toByteArray() );
            InputStream is2 = new ByteArrayInputStream( baos.toByteArray() );

            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream( is1, null, o );

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 170;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while( true ) {
                if( width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE )
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream( is2, null, o2 );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public class LoaderTask extends AsyncTask<String, Void, Bitmap> {
        private final String IMG_EXT[]   = {".jpg", ".jpeg", ".png", ".gif"};

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            if( !url.contains( "." ) )
                return null;

            String extension = url.substring( url.lastIndexOf( "." ) );
            if( !Arrays.asList( IMG_EXT ).contains( extension ) )
                return null;

            return getBitmap( url );
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if( result != null )
                imageView.setImageBitmap( result );
        }
    }
}
