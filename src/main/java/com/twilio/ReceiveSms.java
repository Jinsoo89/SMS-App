package com.twilio;

import static spark.Spark.post;

import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;
import com.twilio.twiml.MessagingResponse;

public class ReceiveSms {

    public static void main(String[] args) {

        post("/receive-sms", (req, res) -> {
            res.type("aplication/xml");

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
