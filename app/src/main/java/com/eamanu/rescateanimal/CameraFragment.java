package com.eamanu.rescateanimal;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

public class CameraFragment extends Fragment {
    private File mFile;
    private SurfaceView mSV;
    private android.hardware.Camera mC;
    private PreviewPic preview;
    private static String TAG = "CameraFragment";
    private Button btnTakePicture;
    /**Imagen para ser enviada.*/
    private Bitmap imageBitmap;

    private static final int REQUEST_GET_ACCOUNT = 112;
    private static final int PERMISSION_REQUEST_CODE = 200;

    public CameraFragment() {
        //empty constructor
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2, container, false);
    }
    /**
     * Método que informa la presencia de memoria externa
     * @return True if External Memory Card is available.
     */
    @NonNull
    private Boolean checkExternalStorageWritable ( ){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            return true; // External storage is available for write

        return false;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Path for picture
            File path;

            if (checkExternalStorageWritable()){
                //path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MyPictures/");
                // En la memoria externa
                path = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/MyPictures/");
                Log.w("TEST",path.toString() );
            }else{
                // En la memoria Interna
                path = getContext().getFilesDir();
            }

            /**Extraigo la foto del intent.*/
            imageBitmap = null;

            // Creo un string para ponerle nombre al la imagen
            String nameImage = "RescateAnimal_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg" ;

            // creo el file
            //File imageFile = new File(path, nameImage);
            File imageFile = new File(path, nameImage);

            try{
                // Me aseguro que la carpeta existe
                path.mkdirs();

                imageFile.createNewFile();
                FileOutputStream os = new FileOutputStream(imageFile);
                os.write(data);
                //imageBitmap.compress(Bitmap.CompressFormat.JPEG,100, os);
                // cierro
                os.close();

            }catch (IOException e){
                Log.w("ExternalStorage", "Error Writing" + imageFile, e);
            }

        }
    };

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mSV = (SurfaceView) view.findViewById(R.id.texture);
        btnTakePicture = (Button) view.findViewById(R.id.BtnCamera);

        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mC.takePicture(null,null,mPicture);
            }
        });
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*
        mFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");
        preview = new PreviewPic(getContext(), mSV,getCameraInstance());
        preview.setKeepScreenOn(true);*/
        //mC = getCameraInstance();
    }

    @Override
    public void onStart(){
        super.onStart();
        mC = getCameraInstance();
        preview = new PreviewPic(getContext(), mSV, mC);

    }

    @Override
    public void onResume(){
        super.onResume();
        //mC = getCameraInstance();

        //preview = new PreviewPic(getContext(), mSV, getCameraInstance());
        //preview.setKeepScreenOn(true);

        //mC.startPreview();


    }

    @Override
    public void onPause(){
        if(mC != null){
            //mC.startPreview();
           // preview.setCamera(null);
           // mC.release();
           // mC = null;
        }
        super.onPause();
    }


    public static Camera getCameraInstance(){
        Camera c = null;

        try {
            c = android.hardware.Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Error en la instanciación de la Cámara");
        }
        return c; // returns null if camera is unavailable
    }



    /**
     * Class Preview
     */
    public class PreviewPic extends ViewGroup implements SurfaceHolder.Callback {
        private final String TAG = "PreviewPic";

        SurfaceView mSurfaceView;
        SurfaceHolder mHolder;
        Camera mCamera;
        Camera.Size mPreviewSize;
        List<Camera.Size> mSupportedPreviewSizes;

        PreviewPic(Context context, SurfaceView sv, Camera cam) {
            super(context);
            setCamera(cam);
            mSurfaceView = sv;
            //addView(mSurfaceView);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        }

        public void setCamera ( Camera camera ){
            if (camera == null){return;}
            mCamera = camera;

            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();

            // get Camera params
            Camera.Parameters params = mCamera.getParameters();

            List<String> focusMode = params.getSupportedFocusModes();
            if(focusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                //set Camera parameters
                mCamera.setParameters(params);
            }
        }

        /**
         * When this function returns, mCamera will be null.
         */
        private void stopPreviewAndFreeCamera() {

            if (mCamera != null) {
                // Call stopPreview() to stop updating the preview surface.
                mCamera.stopPreview();

                // Important: Call release() to release the camera for use by other
                // applications. Applications should release the camera immediately
                // during onPause() and re-open() it during onResume()).
                mCamera.release();

                mCamera = null;
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We purposely disregard child measurements because act as a
            // wrapper to a SurfaceView that centers the camera preview instead
            // of stretching it.
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try{
                if (mCamera != null){
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                }
            }catch (IOException e){
                Log.e(TAG, "Exception caused by setPreviewDisplay()", e);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if(mHolder.getSurface() == null){
                //doesn't exist
                return;
            }

            try {
                mCamera.stopPreview();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }

            /**
            try{
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
            */


            if (mCamera != null){
                Camera.Parameters params = mCamera.getParameters();

                Display display = ((WindowManager)getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

                if(display.getRotation() == Surface.ROTATION_0)
                {
                    params.setPreviewSize(height, width);
                    mCamera.setDisplayOrientation(90);
                }

                if(display.getRotation() == Surface.ROTATION_90)
                {
                    params.setPreviewSize(width, height);
                }

                if(display.getRotation() == Surface.ROTATION_180)
                {
                    params.setPreviewSize(height, width);
                }

                if(display.getRotation() == Surface.ROTATION_270)
                {
                    params.setPreviewSize(width, height);
                    mCamera.setDisplayOrientation(180);
                }
                //params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                requestLayout();

                mCamera.setParameters(params);
                mCamera.startPreview();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null)
                mCamera.stopPreview();
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) w / h;
            if (sizes == null) return null;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            // Try to find an size match aspect ratio and size
            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            // Cannot find the one match the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
            return optimalSize;
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (changed && getChildCount() > 0){
                final View child = getChildAt(0);

                final int width = r - 1;
                final int height = b - t;

                int previewWidth = width;
                int previewHeight = height;
                if (mPreviewSize != null){
                    previewWidth = mPreviewSize.width;
                    previewHeight = mPreviewSize.height;
                }

                // center the child SurfaceView within the parent
                if (width * previewHeight > height * previewWidth){
                    final int scaledChildWidth = previewWidth * height / previewHeight;
                    child.layout((width - scaledChildWidth) / 2, 0,
                            (width + scaledChildWidth) / 2, height);
                }else{
                    final int scaledChildHeight = previewHeight * width / previewWidth;
                    child.layout(0, (height - scaledChildHeight) / 2,
                            width, (height + scaledChildHeight) / 2);
                }
            }
        }
    }
}