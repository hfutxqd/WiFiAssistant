package xyz.imxqd.zuiwifi;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by imxqd on 2016/7/5.
 *
 */
public class QRCode {

    public static String generateQRCodeString(String ssid, String type, String pwd) {
        String str = "WIFI:";
        str = str + "S:" + ssid + ";";
        str = str + "T:" + type + ";";
        str = str + "P:" + pwd + ";;";
        return str;
    }

    public static Bitmap generateQRCode(String qrCodeString){
        Bitmap bmp = null;    //二维码图片

        QRCodeWriter writer = new QRCodeWriter();

        try {
            BitMatrix bitMatrix = writer.encode(qrCodeString, BarcodeFormat.QR_CODE, 256, 256); //参数分别表示为: 条码文本内容，条码格式，宽，高
            int _width = bitMatrix.getWidth();
            int _height = bitMatrix.getHeight();
            bmp = Bitmap.createBitmap(_width, _height, Bitmap.Config.RGB_565);

            //绘制每个像素
            for (int x = 0; x < _width; x++) {
                for (int y = 0; y < _height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

        } catch (WriterException e) {
            e.printStackTrace();
        }

        return bmp;
    }
}
