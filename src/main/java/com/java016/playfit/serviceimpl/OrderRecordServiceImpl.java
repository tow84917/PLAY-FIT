package com.java016.playfit.serviceimpl;

import com.java016.playfit.controller.Pay;
import com.java016.playfit.dao.OrderRecordRepository;
import com.java016.playfit.dao.UserRepository;
import com.java016.playfit.model.OrderRecord;
import com.java016.playfit.model.User;
import com.java016.playfit.service.OrderRecordService;
import com.java016.playfit.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Service
public class OrderRecordServiceImpl implements OrderRecordService {
    private static final Logger logger = LogManager.getLogger(OrderRecordServiceImpl.class);

    @Autowired
    UserService userService;

    @Autowired
    OrderRecordRepository orderRecordRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public OrderRecord saveOrderRecord(Map<String, Object> paramsMap, Integer userId) {

        Integer RtnCode = Integer.parseInt((String)paramsMap.get("RtnCode"));
        logger.info(RtnCode);

        OrderRecord orderRecord = new OrderRecord();
        Date date;
        Calendar calendar;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        for (String s : paramsMap.keySet()) {
            System.out.println("----------------");
            System.out.println(s);
            System.out.println(paramsMap.get(s));
            System.out.println("----------------");
            switch (s){
                case "MerchantTradeNo": // 訂單編號
//                        model.addAttribute("MerchantTradeNo", paramsMap.get(s));
                    orderRecord.setMerchantTradeNo((String)paramsMap.get(s));
                    break;
                case "PaymentDate": // 付款時間
//                        model.addAttribute("PaymentDate", paramsMap.get(s));
                    try {
                        date = dateFormat.parse((String)paramsMap.get(s));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        break;
                    }
                    calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    orderRecord.setPaymentDate(calendar);
                    break;
                case "PaymentType": // 付款方式
//                        model.addAttribute("PaymentType", paramsMap.get(s));
                    orderRecord.setPaymentType((String)paramsMap.get(s));
                    break;
                case "PaymentTypeChargeFee": // 手續費
//                        model.addAttribute("PaymentTypeChargeFee", paramsMap.get(s));
                    orderRecord.setPaymentTypeChargeFee(Integer.parseInt((String)paramsMap.get(s)));
                    break;
                case "TradeAmt": // 交易金額
//                        model.addAttribute("TradeAmt", paramsMap.get(s));
                    orderRecord.setTradeAmt(Integer.parseInt((String)paramsMap.get(s)));
                    break;
                case "TradeDate": // 訂單成立時間
//                        model.addAttribute("TradeDate", paramsMap.get(s));

                    try {
                        date = dateFormat.parse((String)paramsMap.get(s));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        break;
                    }
                    calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    orderRecord.setTradeDate(calendar);
                    break;
                case "TradeNo": // 綠界的交易編號
//                        model.addAttribute("TradeNo", paramsMap.get(s));
                    orderRecord.setTradeNo((String)paramsMap.get(s));
                    break;
            }
        }
//        User loginUser = userService.getLoginUser().getUser();

        User loginUser = userRepository.getById(userId);
        orderRecord.setUserId(loginUser);
        logger.info(orderRecord);
        System.out.println(orderRecord);

        orderRecord.setUserId(loginUser);
        orderRecordRepository.save(orderRecord);

        return orderRecord;
    }
}
