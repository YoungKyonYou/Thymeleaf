//package tmoney.co.kr.hxz.common.util;
//
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.qrcode.QRCodeWriter;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.util.Base64;
//
///** ZXing으로 QR PNG를 Base64 Data URI로 만들어 줌 */
//public class QrCodeUtil {
//
//    public static String toDataUriPng(String content, int size) {
//        try {
//            QRCodeWriter writer = new QRCodeWriter();
//            var bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
//            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
//
//            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//                ImageIO.write(image, "png", baos);
//                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
//                return "data:image/png;base64," + base64;
//            }
//        } catch (Exception e) {
//            throw new IllegalStateException("QR 생성 실패", e);
//        }
//    }
//}