package com.twilio;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SendSms {

    public static final String ACCOUNT_SID = "AC93bc4ea3d2dd1bda39fbc31ad8a3baa9";
    public static final String AUTH_TOKEN = "2bca2d9352f331d42492e850c524d2d1";
    public static final PhoneNumber TWILIO_PHONE_N = new PhoneNumber("+12537331015");
    public static final PhoneNumber MY_PHONE_N = new PhoneNumber("+12067192186");

    public static void main(String[] args) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message.creator(MY_PHONE_N, TWILIO_PHONE_N,
                "test message").create();

        System.out.println(message.getSid());
    }
}
