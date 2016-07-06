package xyz.imxqd.zuiwifi;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private WifiAdmin wifiAdmin;
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.image);

        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        intentIntegrator.setPrompt(getString(R.string.qrcode));
        intentIntegrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
        wifiAdmin = new WifiAdmin(getApplicationContext());
        wifiAdmin.openWifi();
        wifiAdmin.startScan();
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
