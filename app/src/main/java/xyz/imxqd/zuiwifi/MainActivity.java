package xyz.imxqd.zuiwifi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class MainActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private WifiAdmin wifiAdmin;
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.image);
        wifiAdmin = new WifiAdmin(getApplicationContext());
        wifiAdmin.openWifi();
        wifiAdmin.startScan();
        checkPermission();
    }

    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initScan();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if( grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            initScan();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_refuse_title)
                    .setMessage(R.string.permission_refuse_message)
                    .setPositiveButton(R.string.confirm, null)
                    .setOnDismissListener(this)
                    .show();
        }
    }



    private void initScan() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        intentIntegrator.setPrompt(getString(R.string.qrcode));
        intentIntegrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(requestCode == IntentIntegrator.REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                IntentResult result =
                        IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                final ResultParser parser = new ResultParser(result);
                System.out.println(parser.getSsid());
                System.out.println(parser.getType());
                System.out.println(parser.getPassword());
                StringBuilder builder = new StringBuilder();
                builder.append(getString(R.string.wifi_ssid)).append(parser.getSsid()).append("\n");
                builder.append(getString(R.string.wifi_type)).append(parser.getTypeString()).append("\n");
                builder.append(getString(R.string.wifi_pwd)).append(parser.getPassword()).append("\n");
                new AlertDialog.Builder(this)
                        .setMessage(builder)
                        .setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String qrStr = QRCode.generateQRCodeString(parser.getSsid()
                                        , parser.getTypeString(), parser.getPassword());
                                Bitmap bitmap = QRCode.generateQRCode(qrStr);
                                image.setImageBitmap(bitmap);
                                image.setVisibility(View.VISIBLE);
                                isShare = true;
                            }
                        })
                        .setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                wifiAdmin.addNetwork(parser.getSsid(), parser.getPassword(), parser.getType());
                                wifiAdmin.connectSpecificAP(parser.getSsid());
                            }
                        })
                        .setOnDismissListener(this)
                        .setNegativeButton(R.string.cancel, null)
                        .show();

            }
        }
        if(resultCode == RESULT_CANCELED) {
            finish();
        }
    }
    boolean isShare = false;
    @Override
    public void onDismiss(DialogInterface dialog) {
        if(!isShare) {
            finish();
        }
    }
}
