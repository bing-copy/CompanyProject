package com.caiyi.financial.nirvana.bill.util.mail;

import com.caiyi.financial.nirvana.bill.util.BankHelper;
import com.caiyi.financial.nirvana.bill.util.BillConstant;
import com.caiyi.financial.nirvana.ccard.bill.bean.Channel;
import com.caiyi.financial.nirvana.core.util.CheckUtil;
import com.hsk.cardUtil.HttpClientHelper;
import com.hsk.cardUtil.HttpResult;
import org.apache.http.client.CookieStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lichuanshun on 16/7/13.
 */
public class NtesMail163 extends BaseMail{

    /**
     *
     * @param bean
     * @return
     */
    public static int login(Channel bean,Logger logger) {
        HttpClientHelper hc = new HttpClientHelper(true);
        String mailaddress = bean.getLoginname();
        String mailPwd = bean.getPassword();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN");
        headers.put("Cache-Control", "no-cache");
        headers.put("Connection", "Keep-Alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "text/html, application/xhtml+xml, */*");
        Map<String,String> data = new HashMap<String,String>();
        data.put("url2", "https://mail.163.com/errorpage/err_163.htm");
        data.put("savelogin", "0");
        data.put("username", mailaddress);
        data.put("password", mailPwd);
        headers.put("Host","ssl.mail.163.com");
        headers.put("Referer","https://mail.163.com/");
        String errContent="";
        try{
            HttpResult hr = hc.post("https://ssl.mail.163.com/entry/coremail/fcg/ntesdoor2?df=webmail163&from=web&funcid=loginone&iframe=1&language=-1&net=t&passtype=1&product=mail163&race=-2_-2_-2_db&style=-1&uid=", data,headers);// 执行登录
            errContent = hr.getHtml();
            Document doc = Jsoup.parse(errContent);
            String href = doc.select("script").html();
            String title = doc.select("title").text();
            String codeFlag;
            CookieStore cookieStore = hc.getBasicCookieStore();
            if(href.contains("mail.163.com/errorpage/err_163.htm")){//登录错误页面
                String paraStr = href.substring(href.indexOf("?"));
                String[] paras = paraStr.split("\\&");
                String errCode = paras[0].split("\\=")[1];
                String errMsg;
                switch(errCode){
                    case "Login_Timeout":
                        errMsg = "当前邮箱登录状态已失效, 请重新登录";
                        break;
                    case "1":
                        errMsg = "帐号为空";
                        break;
                    case "3":
                        errMsg = "密码为空";
                        break;
                    case "460":
                    case "401":
                    case "420":
                        errMsg = "帐号或密码错误";
                        break;
                    case "422":
                        errMsg = "抱歉,您的帐号被锁定";
                        break;
                    case "414":
                    case "415":
                    case "416":
                    case "417":
                    case "418":
                    case "419":
                        errMsg = "您的登录过于频繁，请稍后再试";
                        break;
                    default:
                        errMsg = "繁忙的系统暂时需要停下歇歇，请您稍后再试";
                }
                bean.setBusiErrCode(0);
                bean.setBusiErrDesc(errMsg);
                return 0;
            }else if(title!=null && title.equals("relogin")){//安全验证页面
                codeFlag = "rand_1";
                cc.set(bean.getCuserId()+"wangyi163CodeFlag"+mailaddress, codeFlag, 1000*60*30);
                cc.set(bean.getCuserId()+"wangyi163CodeHtml"+mailaddress, errContent, 1000*60*30);
                cc.set(bean.getCuserId()+"wangyi163MailCookie"+mailaddress, cookieStore, BillConstant.TIME_TEN);
                logger.info(bean.getCuserId()+ " 验证码验证  codeFlag=="+codeFlag+" mailaddress["+mailaddress+"]");
                bean.setBusiErrCode(3);
                bean.setBusiErrDesc("安全登录验证");
                bean.setCurrency(getVerifycode(bean,logger));
                return 0;
            }else if(errContent.contains("登录验证码")){
                codeFlag = "rand_2";
                cc.set(bean.getCuserId()+"wangyi163CodeFlag"+mailaddress, codeFlag, 1000*60*30);
                cc.set(bean.getCuserId()+"wangyi163CodeHtml"+mailaddress, errContent, 1000*60*30);
                cc.set(bean.getCuserId()+"wangyi163MailCookie"+mailaddress, cookieStore, BillConstant.TIME_TEN);
                logger.info(bean.getCuserId()+ " 验证码验证["+errContent+"]  codeFlag=="+codeFlag+" mailaddress["+mailaddress+"]");
                bean.setBusiErrCode(3);
                bean.setBusiErrDesc("验证码验证");
                bean.setCurrency(getVerifycode(bean,logger));
                return 0;
            }else if(errContent.contains("msas.mail.163.com/msas/vcode/showvcode.do")){
                codeFlag = "rand_3";
                bean.setBusiErrCode(3);
                bean.setBusiErrDesc("验证码验证");
                bean.setCurrency(getVerifycode(bean,logger));
                cc.set(bean.getCuserId()+"wangyi163CodeFlag"+mailaddress, codeFlag, 1000*60*30);
                cc.set(bean.getCuserId()+"wangyi163CodeHtml"+mailaddress, errContent, 1000*60*30);
                cc.set(bean.getCuserId()+"wangyi163MailCookie"+mailaddress, cookieStore, BillConstant.TIME_TEN);
                logger.info(bean.getCuserId()+ " 验证码验证["+errContent+"]  codeFlag=="+codeFlag+" mailaddress["+mailaddress+"]");
                return 0;
            }else{
                String sessionId=doc.select("script").html().split("=")[2];
                sessionId = sessionId.substring(0,sessionId.length()-2);
                bean.setBankSessionId(sessionId);
                bean.setBusiErrCode(1);
            }
            cc.delete(bean.getCuserId()+"wangyi163MailCookie"+mailaddress);
            cc.set(bean.getCuserId()+"wangyi163MailCookie"+mailaddress, cookieStore, BillConstant.TIME_HALFHOUR);
        }catch(Exception e){
            logger.error(bean.getCuserId()+" wangyi163Email异常"+errContent, e);
            bean.setBusiErrCode(0);
            bean.setBusiErrDesc("邮箱帐号登录失败,请稍后......");
        }

        return 1;
    }
    /**
     *
     * @param bean
     * @return
     * @throws IOException
     */
    public static String getVerifycode(Channel bean,Logger logger) throws IOException {
        String base64Str = null;
        BufferedImage localBufferedImage=null;
        String mailaddress=bean.getLoginname();
        String mailPwd=bean.getPassword();
        String errorContent="";
        try {

            Object object=cc.get(bean.getCuserId()+"wangyi163MailCookie"+mailaddress);
            Object object2=cc.get(bean.getCuserId()+"wangyi163CodeFlag"+mailaddress);
            Object object3=cc.get(bean.getCuserId()+"wangyi163CodeHtml"+mailaddress);

            if (object==null||object2==null||object3==null) {
                bean.setBusiErrCode(0);
                bean.setBusiErrDesc("请求已失效请重新操作");
                return null;
            }
            CookieStore cookieStore=(CookieStore) object;
            String codeFlag = (String)object2;
            errorContent = (String)object3;
            HttpClientHelper hc = new HttpClientHelper(true);
            Map<String,String> headers = getBasicHeader();
            Map<String, String> data = new HashMap<String, String>();
            hc.setBasicCookieStore(cookieStore);
            hc.instance();
            Document codeDoc = Jsoup.parse(errorContent);
            if("rand_1".equals(codeFlag)){
                org.jsoup.nodes.Element reform = codeDoc.select("form").get(0);
                String action = reform.attr("action");
                data.clear();
                data.put("username",mailaddress );
                data.put("password", mailPwd);
                HttpResult hr = hc.post(action, data,headers);//重新登录
                errorContent = hr.getText();
                Document yanzDoc = Jsoup.parse(hr.getHtml());
                String title = yanzDoc.select("title").text();
                if(title.equals("跳转提示")){
                    String getId = "https://reg.163.com/services/getid";
                    headers.put("Host","reg.163.com");
                    headers.put("Referer","https://reg.163.com/logins.jsp");
                    headers.put("Accept","*/*");
                    hr = hc.get(getId,headers);
                    errorContent = hr.getText();
                    String id = hr.getText();
                    headers.put("Accept","image/webp,image/*,*/*;q=0.8");
                    headers.put("Accept-Encoding","gzip, deflate, sdch");
                    String getImage = "https://reg.163.com/services/getimg?id="+id;
                    localBufferedImage = hc.getRandomImageOfJPEG("GET",getImage,null,headers);
                    cc.set(bean.getCuserId()+"wangyi163CodeSessionId"+mailaddress, id);
                }else{
                    logger.info(bean.getCuserId()+" get163EmailVcode获取验证码失败 codeFlag==rand_1 errorContent["+errorContent+"]");
                    bean.setBusiErrCode(0);
                    bean.setBusiErrDesc("系统正忙,请稍后再试...");
                    return null;
                }
            }else if("rand_2".equals(codeFlag)){
                org.jsoup.nodes.Element randImg = codeDoc.getElementById("w-lc-hint-checkpic");
                String randUrl = randImg.attr("src");
                headers.put("Accept","image/webp,image/*,*/*;q=0.8");
                headers.put("Accept-Encoding","gzip, deflate, sdch");
                headers.put("Host","msas.mail.163.com");
                headers.put("Referer","http://msas.mail.163.com/msas/vcode/showvcode.do?uid="+mailaddress+"&returl=http%3A%2F%2Fmail.163.com%2Fentry%2Fcgi%2Fntesdoor%3Fdf%3Dmail163_letter%26from%3Dweb%26funcid%3Dlogin%26iframe%3D1%26language%3D-1%26passtype%3D1%26product%3Dmail163%26net%3Dt%26style%3D-1%26race%3D-2_22_-2_hz%26uid%3D"+mailaddress);
                localBufferedImage = hc.getRandomImageOfJPEG("GET",randUrl,null,headers);
            }else if("rand_3".equals(codeFlag)){
                String hrefUrl = "http://msas.mail.163.com/msas/vcode/showvcode.do";
                String returl = "http://entry.mail.163.com/coremail/fcg/ntesdoor2?"
                        + "df=webmail163&from=web&funcid=login&iframe=1&language=-1"
                        + "&net=t&passtype=1&product=mail163&race=-2_-2_-2_db&style=-1&uid=";
                hrefUrl=hrefUrl+"?uid="+mailaddress+"&returl="+ URLEncoder.encode(returl, "utf-8");
                HttpResult hr = hc.get(hrefUrl,headers);
                errorContent = hr.getHtml();
                headers.put("Accept","image/webp,image/*,*/*;q=0.8");
                headers.put("Accept-Encoding","gzip, deflate, sdch");
                headers.put("Host","msas.mail.163.com");
                headers.put("Referer",hrefUrl);
                String url = "http://msas.mail.163.com/msas/vcode/getvcode.do?uid="+mailaddress;
                localBufferedImage = hc.getRandomImageOfJPEG("GET",url,null,headers);
            }else{
                logger.info(bean.getCuserId()+" get163EmailVcode未知错误 errorContent["+errorContent+"]");
                bean.setBusiErrCode(0);
                bean.setBusiErrDesc("系统正忙,请稍后再试...");
                return null;
            }
            cookieStore = hc.getBasicCookieStore();
            cc.set(bean.getCuserId()+"wangyi163MailCookie"+mailaddress, cookieStore, 1000*60*30);
            cc.set(bean.getCuserId()+"wangyi163CodeFlag"+mailaddress, codeFlag);
            base64Str = BankHelper.GetImageBase64(localBufferedImage,"jpeg");
        } catch (Exception e) {
            logger.error(bean.getCuserId()+" get163EmailVcode异常 errorContent["+errorContent+"]", e);
            bean.setBusiErrCode(0);
        }
        if (CheckUtil.isNullString(base64Str)){
            bean.setBusiErrCode(0);
            bean.setBusiErrDesc("获取验证码失败");
        }
        return base64Str;
    }

    /**
     *
     * @param bean
     * @param logger
     * @return
     */
    public static int checkEmailCode(Channel bean,Logger logger){
        String mailaddress=bean.getLoginname();
        String errorContent="";
        String codeFlag = "";

        try {
            if (CheckUtil.isNullString(bean.getCode())) {
                bean.setBusiErrCode(0);
                bean.setBusiErrDesc("验证码不能为空");
                return 0;
            }
            Object object=cc.get(bean.getCuserId()+"wangyi163MailCookie"+mailaddress);
            Object object2=cc.get(bean.getCuserId()+"wangyi163CodeFlag"+mailaddress);

            if (object==null||object2==null) {
                bean.setBusiErrCode(0);
                bean.setBusiErrDesc("请求已失效请重新操作");
                return 0;
            }
            CookieStore cookieStore=(CookieStore) object;
            codeFlag = (String) object2;
            HttpClientHelper hc = new HttpClientHelper(true);
            Map<String,String> headers = getBasicHeader();
            Map<String, String> data = new HashMap<String, String>();
            hc.setBasicCookieStore(cookieStore);
            hc.instance();
            if("rand_1".equals(codeFlag)){
                Object object3=cc.get(bean.getCuserId()+"wangyi163CodeSessionId"+mailaddress);
                if(object3==null){
                    bean.setBusiErrCode(0);
                    bean.setBusiErrDesc("请求已失效请重新操作");
                    return 0;
                }
                String id = (String)object3;
                headers.put("Host","reg.163.com");
                headers.put("Referer","https://reg.163.com/logins.jsp");
                headers.put("Accept-Encoding","gzip, deflate, sdch");
                headers.put("Accept","*/*");
                headers.put("X-Requested-With","XMLHttpRequest");
                String checkCode = "https://reg.163.com/services/checkcode?isLoginException=1";//filledVerifyID=skf94&sysVerifyID=ee4c12557730f4f7a9e9bb7f05a8d717475fa236
                data.clear();
                data.put("filledVerifyID", bean.getCode());
                data.put("sysVerifyID", id);

                HttpResult hr = hc.post(checkCode,data,headers);
                errorContent = hr.getText();
                if (errorContent.equals("461")) {
                    bean.setBusiErrCode(0);
                    bean.setBusiErrDesc("验证码错误，请重试");
                    logger.info(bean.getCuserId()+" 验证码错误，请重试");
                    return 0;
                } else {
                    data.clear();
                    String nextUrl = errorContent;
                    if(nextUrl.indexOf("http")!=-1){
                        nextUrl = nextUrl.substring(nextUrl.indexOf("http"));
                    }else if(nextUrl.indexOf("https")!=-1){
                        nextUrl = nextUrl.substring(nextUrl.indexOf("https"));
                    }else{
                        bean.setBusiErrCode(0);
                        bean.setBusiErrDesc("访问错误，请重试");
                        logger.info(bean.getCuserId()+" 访问错误错误，请重试["+errorContent+"]");
                        return 0;
                    }
                    nextUrl = nextUrl.replaceAll("\\s*", "");
                    headers.put("Host", "reg.youdao.com");
                    headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    headers.put("Referer", "");
                    hr = hc.get(nextUrl, headers);
                    String mainUrl = "http://reg.163.com/Main.jsp?username="+mailaddress;
                    data.clear();
                    // 进入网易中心页 面
                    headers.put("Host", "reg.163.com");
                    headers.put("Referer", "");
                    hr = hc.post(mainUrl, data,headers);
                    //进入邮箱
                    headers.put("Host", "reg.163.com");

                    headers.put("Referer", "http://reg.163.com/Main.jsp?username="+mailaddress);
                    String enterMail = "http://reg.163.com/click.jsp?click_in=Main&v=" + new Date().getTime() + "&click_count_spec=&_ahref=http://entry.mail.163.com/coremail/fcg/ntesdoor2?verifycookie=1&lightweight=1&from=urs&_at=%3Cspan%3E%E8%BF%9B%E5%85%A5%E9%82%AE%E7%AE%B1%3C/span%3E";
                    hr = hc.post(enterMail, data, headers);
                    errorContent = hr.getHtml();
                    logger.info(bean.getCuserId()+" codeFlag=="+codeFlag+"  check163EmailCode success");
                }
            }else if("rand_2".equals(codeFlag)){
                String checkCode = "http://msas.mail.163.com/msas/vcode/checkvcode.do?uid="+mailaddress+"&vcode="+bean.getCode().toLowerCase();
                HttpResult hr = hc.get(checkCode,headers);
                errorContent = hr.getHtml();
                String loginUrl = "http://mail.163.com/entry/cgi/ntesdoor?df=mail163_letter&from=web&funcid=login&iframe=1&language=-1&passtype=1&product=mail163&net=t&style=-1&race=-2_22_-2_hz&uid="+mailaddress+"&vcode=" +bean.getCode().toLowerCase();
                hr = hc.get(loginUrl,headers);
                errorContent = hr.getHtml();
            }else if("rand_3".equals(codeFlag)){
                String checkCode = "http://msas.mail.163.com/msas/vcode/checkvcode.do?uid="+mailaddress+"&vcode="+bean.getCode().toLowerCase();
                HttpResult hr = hc.get(checkCode,headers);
                errorContent = hr.getHtml();
                String loginUrl = "http://entry.mail.163.com/coremail/fcg/ntesdoor2?df=webmail163"
                        + "&from=web&funcid=login&iframe=1&language=-1&net=t&passtype=1&product=mail163"
                        + "&race=-2_-2_-2_db&style=-1&uid="+"&vcode=" +bean.getCode().toLowerCase();
                hr = hc.get(loginUrl,headers);
                errorContent = hr.getHtml();
            }else{
                logger.info(bean.getCuserId()+" codeFlag=="+codeFlag+" check163EmailCode异常 errorContent["+errorContent+"]");
                bean.setBusiErrCode(0);
                bean.setBusiErrDesc("请求已失效请重新操作");
                return 0;
            }
            String coremailSession = hc.getCookie("Coremail","mail.163.com");
            if(CheckUtil.isNullString(coremailSession)){
                bean.setBusiErrCode(3);
                bean.setBusiErrDesc("验证失败,请重试.....");
                bean.setCurrency(getVerifycode(bean,logger));
                return 0;
            }
            String sid = coremailSession.split("\\%")[1];
            cookieStore = hc.getBasicCookieStore();
            cc.set(bean.getCuserId()+"wangyi163MailCookie"+mailaddress, cookieStore, BillConstant.TIME_TEN);
            bean.setBankSessionId(sid);
            return 1;
        } catch (Exception e) {
            logger.error(bean.getCuserId()+" codeFlag=="+codeFlag+" check163EmailCode异常 errorContent["+errorContent+"]", e);
            bean.setBusiErrCode(0);
            bean.setBusiErrDesc("请求已失效请重新操作");
            return 0;
        }
    }

}
