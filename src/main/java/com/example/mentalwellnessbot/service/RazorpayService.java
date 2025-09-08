// package com.example.mentalwellnessbot.service;

// import com.razorpay.PaymentLink;
// import com.razorpay.RazorpayClient;
// import com.razorpay.RazorpayException;
// import org.json.JSONObject;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// @Service
// public class RazorpayService {

//     private final RazorpayClient client;

//     public RazorpayService(
//             @Value("${razorpay.key_id}") String keyId,
//             @Value("${razorpay.key_secret}") String keySecret
//     ) throws RazorpayException {
//         this.client = new RazorpayClient(keyId, keySecret);
//     }

//     public String createPaymentLink(Long telegramId, String username) throws RazorpayException {
//         JSONObject request = new JSONObject();
//         request.put("amount", 49900); // Amount in paise (₹499)
//         request.put("currency", "INR");
//         request.put("description", "Mental Wellness Bot Access for " + username);

//         JSONObject customer = new JSONObject();
//         customer.put("name", username != null ? username : "Telegram User");
//         customer.put("email", "user" + telegramId + "@example.com"); // placeholder
//         request.put("customer", customer);

//         JSONObject notify = new JSONObject();
//         notify.put("sms", false);
//         notify.put("email", false);
//         request.put("notify", notify);

//         request.put("callback_url", "https://yourdomain.com/razorpay/callback?user=" + telegramId);
//         request.put("callback_method", "get");

//         PaymentLink link = client.paymentLink.create(request);
//         return link.get("short_url");
//     }
// }
