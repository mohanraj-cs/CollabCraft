package com.CollabCraft.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MailUtil {


    public String sendMail(String text, String subject, String emailAdress,
                           JavaMailSender javaMailSender, Boolean type, String location) {

        MimeMessage mMessage = javaMailSender.createMimeMessage();// 创建邮件对象
        MimeMessageHelper mMessageHelper;
        Properties prop = new Properties();
        try {
            prop.load(this.getClass().getResourceAsStream("/mail.properties"));
            String from = prop.get("mail.smtp.username") + "";
            mMessageHelper = new MimeMessageHelper(mMessage, true, "UTF-8");
            mMessageHelper.setFrom(from);
            mMessageHelper.setTo(emailAdress);
            mMessageHelper.setSubject(subject);
            if (type) {
                mMessageHelper.setText(text, true);
            } else {
                mMessageHelper.setText(text, false);
            }
            if (!location.equals("false")) {
                String filename = StringUtils.getFilename(location);
                File file = new File(location);
                FileSystemResource resource = new FileSystemResource(file);
                mMessageHelper.addAttachment(filename, resource);
            }
            javaMailSender.send(mMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "success";
    }

    public String sendMailFreeMarker(String subject, String location, String emailAdress, JavaMailSender javaMailSender,
                                     Configuration freeMarkerConfiguration, Integer type,
                                     String[] invitation_info) {
        MimeMessage mMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mMessageHelper;
        Properties prop = new Properties();
        try {

            prop.load(this.getClass().getResourceAsStream("/mail.properties"));
            String from = prop.get("mail.smtp.username") + "";

            mMessageHelper = new MimeMessageHelper(mMessage, true);
            mMessageHelper.setFrom(from);
            mMessageHelper.setTo(emailAdress);
            mMessageHelper.setSubject(subject);

            switch (type) {
                case Constants.INVITATION_EMAIL:
                    mMessageHelper.setText(getInviteText(freeMarkerConfiguration,invitation_info[0],
                            invitation_info[1],invitation_info[2],invitation_info[3]), true);
                    break;
                case Constants.NOTIFICATION_EMAIL:

                    break;

                default:

            }


            String filename = StringUtils.getFilename(location);
            if (!location.equals("false")) {

                File file = new File(location);
                FileSystemResource resource = new FileSystemResource(file);
                mMessageHelper.addAttachment(filename, resource);
            }
            javaMailSender.send(mMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "success";
    }
    private String getInviteText(Configuration freeMarkerConfiguration, String user, String invite_user,
                                 String invite_link, String privilege) {
        String txt = "";
        try {
            Template template = freeMarkerConfiguration.getTemplate("/invitationemail.ftl");


            Map<String, Object> map = new HashMap<String, Object>();
            map.put("user", user);
            map.put("invite_user", invite_user);
            map.put("invite_link", invite_link);
            map.put("privilege", privilege);

            txt = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            System.out.println("getText()->>>>>>>>>");
            System.out.println(txt);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }

        return txt;
    }


}
