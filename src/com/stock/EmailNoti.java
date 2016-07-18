package com.stock;
/**
 * Created by hahazhu on 16-7-12.
 */
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;


/**
 * 利用java.mail的邮件发送程序
 */

public class EmailNoti {


    public static void main(String[] args) {
        EmailNoti e = new EmailNoti();
        e.sendBuyNoti("600000");
    }
    public void sendBuyNoti(String stockId){
        sendNoti(stockId,"买入");
    }
    public void sendSellNoti(String stockId){
        sendNoti(stockId,"卖出");
    }
    private void sendNoti(String stockId,String direct){
        String title = stockId+"股票可以"+direct;// 所发送邮件的标题
        String from = "zzzzzmmmmm86@126.com";// 从那里发送
        String sendTo[] = { "zzzzzmmmmm86@126.com", "kyo-gao@163.com" };// 发送到那里
        // 邮件的文本内容，可以包含html标记则显示为html页面
        String content = stockId;
        // 所包含的附件，及附件的重新命名
        try {
            sendmail(title, from, sendTo, content,
                    "text/html;charset=gb2312");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendmail(String subject, String from, String[] to,
                                String text, String mimeType) throws Exception {
        // ResourceBundle mailProps = ResourceBundle.getBundle("mail");
        // 可以从配置文件读取相应的参数
        Properties props = new Properties();

        String smtp = "smtp.126.com"; // 设置发送邮件所用到的smtp
        String servername = "zzzzzmmmmm86@126.com";//发邮件的邮箱名称
        String serverpaswd = "******";//发邮件的邮箱密码

        javax.mail.Session mailSession; // 邮件会话对象
        javax.mail.internet.MimeMessage mimeMsg; // MIME邮件对象

        props = System.getProperties(); // 获得系统属性对象
        props.put("mail.smtp.host", smtp); // 设置SMTP主机
        props.put("mail.smtp.auth", "true"); // 是否到服务器用户名和密码验证
        // 到服务器验证发送的用户名和密码是否正确
        Email_Autherticatorbean myEmailAuther = new Email_Autherticatorbean(
                servername, serverpaswd);
        // 设置邮件会话
        mailSession = javax.mail.Session.getInstance(props,
                (Authenticator) myEmailAuther);

        //设置debug打印信息
        mailSession.setDebug(true);

        // 设置传输协议
        javax.mail.Transport transport = mailSession.getTransport("smtp");
        // 设置from、to等信息
        mimeMsg = new javax.mail.internet.MimeMessage(mailSession);
        if (!from.isEmpty()) {

            InternetAddress sentFrom = new InternetAddress(from);
            mimeMsg.setFrom(sentFrom); // 设置发送人地址
        }

        InternetAddress[] sendTo = new InternetAddress[to.length];
        for (int i = 0; i < to.length; i++) {
            System.out.println("发送到:" + to[i]);
            sendTo[i] = new InternetAddress(to[i]);
        }

        mimeMsg.setRecipients(javax.mail.internet.MimeMessage.RecipientType.TO,
                sendTo);
        mimeMsg.setSubject(subject, "gb2312");

        MimeBodyPart messageBodyPart1 = new MimeBodyPart();
        // messageBodyPart.setText(UnicodeToChinese(text));
        messageBodyPart1.setContent(text, mimeType);

        Multipart multipart = new MimeMultipart();// 附件传输格式
        multipart.addBodyPart(messageBodyPart1);

        mimeMsg.setContent(multipart);
        // 设置信件头的发送日期
        mimeMsg.setSentDate(new Date());
        mimeMsg.saveChanges();
        // 发送邮件
        transport.send(mimeMsg);
        transport.close();
    }

}
