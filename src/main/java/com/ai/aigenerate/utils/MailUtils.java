package com.ai.aigenerate.utils;

import com.ai.aigenerate.model.request.mail.EmailRequest;
import com.ai.aigenerate.model.request.mail.ImageMail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class MailUtils {
    /**
     * 发送邮件
     * @param emailRequest email配置信息
     */
    public static void sendMail(EmailRequest emailRequest) {
        Transport transport = null;
        try {
            Properties prop = new Properties();
            prop.setProperty("mail.host", emailRequest.getServerName());  //邮箱发送
            prop.setProperty("mail.transport.protocol", "smtp"); // 邮件发送协议
            prop.setProperty("mail.smtp.auth", "true"); // 需要验证用户名密码
            //1、创建定义整个应用程序所需的环境信息的 Session 对象
            Session session = Session.getDefaultInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    //传入发件人的姓名和授权码
                    return new PasswordAuthentication(emailRequest.getUserName(), emailRequest.getPassword());
                }
            });
            //2、通过session获取transport对象
            transport = session.getTransport();
            //3、通过transport对象邮箱用户名和授权码连接邮箱服务器
            transport.connect(emailRequest.getServerName(), emailRequest.getUserName(), emailRequest.getPassword());
            //4、创建邮件,传入session对象
            MimeMessage mimeMessage = getMimeMessage(session, emailRequest, emailRequest.getImageMailList());
            //5、发送邮件
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 组装发送信息
     * @param session session
     * @param emailRequest 邮件配置信息
     * @return MimeMessage
     * @throws Exception  Exception
     */
    private static MimeMessage getMimeMessage(Session session, EmailRequest emailRequest, List<ImageMail> imageMail) throws Exception {
        //消息的固定信息
        MimeMessage mimeMessage = new MimeMessage(session);
        //发件人
        mimeMessage.setFrom(new InternetAddress(emailRequest.getEmailAddress()));
        //收件人
        if (StringUtils.isNotBlank(emailRequest.getTo())) {
            String[] tos = emailRequest.getTo().split(",");
            for (String to : tos) {
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }
        }
        //抄送人
        if (StringUtils.isNotBlank(emailRequest.getCC())) {
            String[] ccs = emailRequest.getTo().split(",");
            for (String cc : ccs) {
                mimeMessage.setRecipient(Message.RecipientType.CC, new InternetAddress(cc));
            }
        }
        //邮件标题
        mimeMessage.setSubject(emailRequest.getSubject());
        MimeMultipart multipart = new MimeMultipart();
//        if (filesPath != null){
//            //附件
//            for (Object pathObj : filesPath) {
//                MimeBodyPart attachPart = new MimeBodyPart();
//                File file = new File((String) pathObj);
//                attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
//                //避免中文乱码的处理
//                attachPart.setFileName(MimeUtility.encodeWord(file.getName()));
//                multipart.addBodyPart(attachPart);//附件
//            }
//        }
        //正文
        MimeBodyPart contentPart = new MimeBodyPart();
        contentPart.setContent(emailRequest.getContent(), "text/html;charset=utf-8");
        multipart.addBodyPart(contentPart);
        if (!CollectionUtils.isEmpty(imageMail)) {
            for (ImageMail dto : imageMail) {
                MimeBodyPart image = new MimeBodyPart();
                URL url = new URL(dto.getUrl());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(ImageIO.read(url), "png", outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                image.setDataHandler(new DataHandler(new ByteArrayDataSource(imageBytes, "image/jpeg")));  //javamail jaf
                image.setContentID(dto.getCid());
                image.setFileName(dto.getCid()+".png");
                multipart.addBodyPart(image);
            }
        }
        //放到Message消息中
        mimeMessage.setContent(multipart);
        mimeMessage.saveChanges();//保存修改
        return mimeMessage;
    }
}