package com.example.mentalwellnessbot.service;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final RazorpayClient client;

    public PaymentService(
        @Value("${razorpay.key_id}") String keyId,
        @Value("${razorpay.key_secret}") String keySecret
    ) throws RazorpayException {
        this.client = new RazorpayClient(keyId, keySecret);
    }

    public String createPaymentLink(String chatId, int amountInRupees) {
        try {
            JSONObject request = new JSONObject();
            request.put("amount", amountInRupees * 100);
            request.put("currency", "INR");
            request.put("accept_partial", false);
            request.put("description", "Mental Wellness Bot Subscription");

            JSONObject customer = new JSONObject();
            customer.put("name", "TelegramUser_" + chatId);
            customer.put("email", "user-" + chatId + "@example.com");
            request.put("customer", customer);

            request.put("notify", new JSONObject().put("sms", false).put("email", false));
            request.put("reminder_enable", true);

            PaymentLink link = client.paymentLink.create(request);
            return link.get("short_url").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** 🔍 Auto-check if user has paid */
    public boolean checkPayment(String chatId) {
        try {
            List<PaymentLink> links = client.paymentLink.fetchAll();

            for (PaymentLink link : links) {
                JSONObject obj = link.toJson();

                JSONObject customer = obj.optJSONObject("customer");
                if (customer != null &&
                        ("user-" + chatId + "@example.com").equals(customer.optString("email"))) {

                    if ("paid".equalsIgnoreCase(obj.optString("status"))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
