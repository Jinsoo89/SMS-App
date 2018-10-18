package com.twilio;

import static spark.Spark.post;

import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;
import com.twilio.twiml.MessagingResponse;

import java.sql.SQLOutput;

public class ReceiveSms {

    public static void main(String[] args) {

        post("/receive-sms", (req, res) -> {
            System.out.println(req.params("SmsSid"));
            System.out.println(req.params("Body"));
            System.out.println(req.body());
            System.out.println(req.params());


            res.type("application/xml");

            Body body = new Body.Builder("Hello world")
                    .build();

            Message sms = new Message.Builder()
                    .body(body)
                    .build();

            MessagingResponse twiml = new MessagingResponse.Builder()
                    .message(sms)
                    .build();

            return twiml.toXml();
        });
    }
}
