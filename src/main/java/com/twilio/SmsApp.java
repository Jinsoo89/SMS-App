package com.twilio;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.type.PhoneNumber;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.post;

public class SmsApp {

    public static final String ACCOUNT_SID =
            "AC93bc4ea3d2dd1bda39fbc31ad8a3baa9";
    public static final String AUTH_TOKEN =
            "2bca2d9352f331d42492e850c524d2d1";
    public static final PhoneNumber TWILIO_PHONE_N =
            new PhoneNumber("+12537331015");
    public static final PhoneNumber MY_PHONE_N =
            new PhoneNumber("+12067192186");
    public static final String INFO_MESSAGE =
            "Hello, have you experienced one or more of lightheadedness, " +
                    "sweating, indigestion, nausea, fast heart rate, " +
                    "shortness of breath, and pain in the chest today?";

    private static Map<String, Integer> clients_data = new HashMap<String, Integer>();


    public static void main(String[] args) {
        // initial setup
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        clients_data.put("12067192186", 0);
        // call a function to send text to the client
        send_text();

        post("/receive-sms", (req, res) -> {
//            System.out.println(req.body());
            System.out.println(extract_num(req.body()));
            System.out.println(extract_text(req.body()));

            res.type("application/xml");

            Body body = new Body.Builder("Hello world")
                    .build();

            com.twilio.twiml.messaging.Message sms = new com.twilio.twiml.messaging.Message.Builder()
                    .body(body)
                    .build();

            MessagingResponse twiml = new MessagingResponse.Builder()
                    .message(sms)
                    .build();

            return twiml.toXml();
        });
    }

    public static void send_text() {
        // different cases of client's symptoms counts
        if (clients_data.get("12067192186") == 0) {
            Message message = Message.creator(MY_PHONE_N, TWILIO_PHONE_N,
                    INFO_MESSAGE).create();

            System.out.println(message.getSid());
        } else {

        }

    }

    public static String extract_num(String str) {
        // extract the index of parameter From
        int param_from_index = str.indexOf("From=%2B");
        // extract the number of user
        String p_number = str.substring(param_from_index + 8, param_from_index + 11);

        return p_number;
    }

    public static String extract_text(String str) {
        // extract the index of content
        int param_body_index = str.indexOf("Body=");
        // extract the body text
        String text = str.substring(param_body_index + 5, str.indexOf("&FromCountry"));

        return text;
    }
}