package com.caiyi.nirvana.analyse.token;

import com.mina.rbc.logger.Logger;
import com.mina.rbc.logger.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * token生成器.
 *
 * @author huzhiqiang
 */
public class TokenGenerator {
    public static Logger logger = LoggerFactory.getLogger(TokenGenerator.class);
    // 24位密钥
    public static final String SECRET = "a60e73ec8c752ac89b521777";

    /**
     * 检查密钥长度,如不足24位,则加0x00补齐.
     *
     * @param keyString
     * @return
     */
    public static byte[] getKeyBytes(String keyString) {
        byte[] b = new byte[24];
        byte[] key = keyString.getBytes();
        int count = keyString.getBytes().length;
        for (int i = 0; i < count; i++) {
            b[i] = key[i];
        }
        for (int i = count; i < 24; i++) {
            b[i] = 0x20;
        }
        return b;
    }


    /***
     * RTUAPID加密.
     * @param src
     * @return
     */
    public static String createDigest(String src) {
        String ret = "";
        try {
            // Hash算法
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(src.getBytes());
            // Base64加密
            ret = new BASE64Encoder().encode(sha.digest());
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return ret;
    }


    public static void main(String[] args) {
        String[] authToken = TokenGenerator.authToken("+NEflO3uj02eOaWPdCVSbDiORgxuKQuyVLKkfCeHMvuCoSwTSXn4KystCjGBKzUJ+E8LoiDpnHeHU0IXb7koJZE83F8KUhSnTjFaJr45wenXaVUjbDn+xjl/86Erb2wncS2eT43BNDyBZ+pXE+A/yJD5ry6i8YYTzIJglDPB6vEHPv4z5tmqsw==",
                "lt201N60LO12A010Q3AQ5VDR2F5H64ZO5");
        for (int i = 0; i < authToken.length; i++) {
            System.out.println(authToken[i]);
        }
    }

    /***
     * 验证认证码.
     *
     * @param authentication
     * @return
     */
    public static String[] authToken(String authentication, String appid) {
        // 输出参数
        String[] str = new String[2];
        // 拆分认证码验证

        try {
            // 24字节密钥key,3倍DES密钥长度
            byte[] tripleKey = getKeyBytes(SECRET);

            // 生成密钥
            SecretKey secretKey = new SecretKeySpec(tripleKey, "DESede");

            String transformation = "DESede/CBC/PKCS5Padding";
            Cipher cipher = Cipher.getInstance(transformation);

            // CBC方式的初始化向量
            byte[] iv = new byte[]{93, 81, (byte) 122, (byte) 233, 47, 50, 17, (byte) 103};
            IvParameterSpec ivparam = new IvParameterSpec(iv);

            // 解密
            byte[] auth = new BASE64Decoder().decodeBuffer(authentication);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparam);
            String text = new String(cipher.doFinal(auth));

            // 验证
            String[] textValue = text.split("\\$");
            if (textValue.length == 2) {
                // 拆分值并存入Map中
                HashMap<String, String> map = new HashMap<String, String>();
                String[] tokenValue = textValue[0].split(";");
                for (int i = 0; i < tokenValue.length; i++) {
                    String[] value = tokenValue[i].split("=");
                    map.put(value[0], value[1]);
                }
                // 判断值是否相等
                if ("9188".equals(map.get("company")) && "LOTTERY".equals(map.get("appType"))
                        && appid.equals(map.get("appid"))) {
                    str[0] = "1";
                    str[1] = "验证成功!";
                } else {
                    str[0] = "0";
                    str[1] = "验证失败,数据不匹配!";
                }
            } else {
                str[0] = "0";
                str[1] = "验证失败,格式错误!";
            }
        } catch (Exception ex) {
            str[0] = "-1";
            str[1] = "验证失败,出异常!";
            logger.info("验证失败,出异常" + authentication);
        }
        return str;
    }


    /**
     * 认证码.
     *
     * @return
     * @throws Exception
     */
    public static String createToken(String appid) {
        //String authenticator = "access_token:";
        String authenticator = "";
        try {
            // 当前时间
            Date now = new Date();
            SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Digest
            String digest = createDigest(appid);

            String orgToken = "company=9188;appType=LOTTERY;appid=" + appid
                    + ";currentTime=" + d1.format(now)
                    + "$" + digest;

            // 24字节密钥key,3倍DES密钥长度
            byte[] tripleKey = getKeyBytes(SECRET);

            // 生成密钥
            SecretKey secretKey = new SecretKeySpec(tripleKey, "DESede");

            String transformation = "DESede/CBC/PKCS5Padding";   // DES,DESede,Blowfish，

            Cipher cipher = Cipher.getInstance(transformation);

            // CBC方式的初始化向量
            byte[] iv = new byte[]{93, 81, (byte) 122, (byte) 233, 47, 50, 17, (byte) 103};
            IvParameterSpec ivparam = new IvParameterSpec(iv);

            // 加密
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparam);

            byte[] encriptText = cipher.doFinal(orgToken.getBytes("utf-8"));

            // 加密
            authenticator += new BASE64Encoder().encode(encriptText);

            //去掉token中的空格和换行符
            authenticator = getStringNoBlank(authenticator);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return authenticator;
    }

    //正则表达式替换空格和换行符
    public static String getStringNoBlank(String str) {
        if (str != null && !"".equals(str)) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            String strNoBlank = m.replaceAll("");
            return strNoBlank;
        } else {
            return str;
        }
    }

}
