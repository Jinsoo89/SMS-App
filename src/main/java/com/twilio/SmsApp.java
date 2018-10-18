package com.twilio;

// import libraries that being used in this program
// message APIs from Twilio, and phone number to be an object
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.type.PhoneNumber;

// import libraries from java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// import library of post from Spark, which is used for server side
// service implementation
import static spark.Spark.post;

// the class of this program
public class SmsApp {
    // declare global variables
    private static final String SYSTEM_NUMBER = "+12537331015";
    private static final String ACCOUNT_SID =
            "AC93bc4ea3d2dd1bda39fbc31ad8a3baa9";
    private static final String AUTH_TOKEN =
            "2bca2d9352f331d42492e850c524d2d1";
    private static final PhoneNumber TWILIO_PHONE_N =
            new PhoneNumber(SYSTEM_NUMBER);
    private static final PhoneNumber MY_PHONE_N =
            new PhoneNumber("+12067192186");
    private static final String INFO_MESSAGE =
            "This is IDH monitoring service." +
                    " Record any symptom you had today, reply 'record' and " +
                    "followed by names of symptoms" +
                    " Ex: record chest pain indigestion blacking out";

    // use collections to store data to use
    private static Map<String, HashMap<String, Integer>> clients_data =
            new HashMap<>();
    private static HashMap<String, Integer> personal_data =
            new HashMap<>();
    private static HashMap<String, ArrayList<String>> hospitals =
            new HashMap<>();
    private static ArrayList<String> hospital_info =
            new ArrayList<>();

    // a formatter to format date and time
    private static DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    // main function to run
    public static void main(String[] args) {
        // initial setup
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        // initialize client's data
        clients_data.put("+12067192186", personal_data);
        clients_data.get("+12067192186").put("chest pain", 0);
        clients_data.get("+12067192186").put("chest pressure", 0);
        clients_data.get("+12067192186").put("indigestion", (0));
        clients_data.get("+12067192186").put("shortness of breath", (0));
        clients_data.get("+12067192186").put("dizzy sensation", (0));
        clients_data.get("+12067192186").put("blacking out", (0));

        // initialize hospital data
        hospitals.put("98115", hospital_info);
        hospitals.get("98115").add("Wonsan Hospital");
        hospitals.get("98115").add("4536 16th Ave, Wonsan, North Korea");
        hospitals.get("98115").add("253-000-0000");

        // send initial message to the client
        Message message = Message.creator(MY_PHONE_N, TWILIO_PHONE_N,
                INFO_MESSAGE).create();

        // print the log of message out
        System.out.println(LocalDateTime.now().format(formatter) +
                ": System " + SYSTEM_NUMBER + " sent a message to " +
                message.getTo());

        // main part to handle cases and data processing via server and clients
        // using server
        post("/receive-sms", (req, res) -> {
            // extract user phone number from body of request
            String user_num = extract_num(req.body());
            // extract content of text from body of request
            String text_body = extract_content(req.body()).toLowerCase()
                    .replace("+", " ");
            // print the log of message out
            System.out.println("\n" + LocalDateTime.now()
                    .format(formatter) + ": Client " + user_num +
                    " sent a message to " + SYSTEM_NUMBER);

            res.type("application/xml");

            // handle case of user to record symptoms
            if (text_body.contains("record")) {
                int total = user_record_symptom(user_num, text_body);

                Body body = new Body.Builder("You have experienced symptoms " +
                        "of Ischemic Heart Disease " + total +
                        " time(s) during this month. If you want to talk " +
                        "to a doctor, reply 'doctor'.")
                        .build();

                com.twilio.twiml.messaging.Message sms =
                        new com.twilio.twiml.messaging.Message.Builder()
                        .body(body)
                        .build();

                MessagingResponse twiml = new MessagingResponse.Builder()
                        .message(sms)
                        .build();

                // print the log of message out
                System.out.println("\n" + LocalDateTime.now()
                        .format(formatter) + ": System " + SYSTEM_NUMBER +
                        " sent a message to " + user_num);

                return twiml.toXml();

            // handle case of user to want to talk with doctor
            } else if (text_body.contains("doctor")) {
                // extract zip code of the client
                String zipCode = extract_zipCode(req.body());
                // temporary array to store information of the hospital
                String[] temp = new String[3];

                // store information of hospital that is near
                // the client's zip code
                for (int i = 0; i < hospitals.get(zipCode).size(); i++) {
                    temp[i] = hospitals.get(zipCode).get(i);
                }

                Body body = new Body.Builder(temp[0] + " (" + temp[1] +
                        ") is a IHD specialized hospital near you. " +
                        "The phone number is " + temp[2] +
                        " for reservation or medical consultation.")
                        .build();

                com.twilio.twiml.messaging.Message sms =
                        new com.twilio.twiml.messaging.Message.Builder()
                        .body(body)
                        .build();

                MessagingResponse twiml = new MessagingResponse.Builder()
                        .message(sms)
                        .build();

                // print the log of message out
                System.out.println("\n" + LocalDateTime.now()
                        .format(formatter) + ": System " +
                        SYSTEM_NUMBER + " sent a message to " + user_num);

                return twiml.toXml();

            // user send wrong instruction text
            } else {
                Body body = new Body.Builder("Sorry, you typed wrong " +
                        "instruction, type 'record' or 'doctor'")
                        .build();

                com.twilio.twiml.messaging.Message sms =
                        new com.twilio.twiml.messaging.Message.Builder()
                        .body(body)
                        .build();

                MessagingResponse twiml = new MessagingResponse.Builder()
                        .message(sms)
                        .build();

                // print the log of message out
                System.out.println("\n" + LocalDateTime.now().
                        format(formatter) + ": System " + SYSTEM_NUMBER +
                        " sent a message to " + user_num);

                return twiml.toXml();
            }
        });
    }

    // user_record_symptom check user's text and modify data of the client
    // param: user_num user's phone number
    //        sms_body the context of the text
    private static int user_record_symptom(String user_num, String sms_body) {
        HashMap<String, Integer> temp = clients_data.get(user_num);
        int total = 0;

        if (sms_body.contains("chest pain")) {
            Integer count = temp.get("chest pain");

            clients_data.get(user_num).put("chest pain", count + 1);
        }

        if (sms_body.contains("chest pressure")) {
            Integer count = temp.get("chest pressure");

            clients_data.get(user_num).put("chest pressure", count + 1);
        }

        if (sms_body.contains("indigestion")) {
            Integer count = temp.get("indigestion");

            clients_data.get(user_num).put("indigestion", count + 1);
        }

        if (sms_body.contains("shortness of breath")) {
            Integer count = temp.get("shortness of breath");

            clients_data.get(user_num).put("shortness of breath", count + 1);
        }

        if (sms_body.contains("dizzy sensation")) {
            Integer count = temp.get("dizzy sensation");

            clients_data.get(user_num).put("dizzy sensation", count + 1);
        }

        if (sms_body.contains("blacking out")) {
            Integer count = temp.get("blacking out");

            clients_data.get(user_num).put("blacking out", count+ 1);
        }

        Map<String, Integer> tmp = clients_data.get(user_num);
        Set<String> symptoms = tmp.keySet();

        // print the log of modified data out
        System.out.println("\n--- Client " + user_num + "'s record ---");

        for (String str : symptoms) {
            System.out.println(str + " = " + tmp.get(str));
            total += tmp.get(str);
        }

        return total;
    }

    // extract_num extracts phone number of the client from
    // the body(meta data) of text
    // param: str a string of the body of text
    private static String extract_num(String str) {
        // extract the index of parameter From
        int param_from_index = str.indexOf("From=%2B");
        // extract the number of user
        String temp = str.substring(
                param_from_index + 8, str.indexOf("&ApiVersion"));

        String client_num = "+" + temp;

        return client_num;
    }

    // extract_content extracts content of text from
    // the body(meta data) of text
    // param: str a string of the body of text
    private static String extract_content(String str) {
        // extract the index of content
        int param_body_index = str.indexOf("Body=");
        // extract the content
        String text = str.substring(
                param_body_index + 5, str.indexOf("&FromCountry"));

        return text;
    }

    // extract_zipCode extracts zip code of the client from
    // the body(meta data) of text
    // param: str a string of the body of text
    private static String extract_zipCode(String str) {
        // extract the area of user
        int param_zip_index = str.indexOf("FromZip=");
        // extract the body text
        String text = str.substring(
                param_zip_index + 8, str.indexOf("&SmsSid"));

        return text;
    }
}