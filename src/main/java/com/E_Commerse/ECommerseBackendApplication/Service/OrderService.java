package com.E_Commerse.ECommerseBackendApplication.Service;

import com.E_Commerse.ECommerseBackendApplication.Enum.ProductStatus;
import com.E_Commerse.ECommerseBackendApplication.Exception.CustomerNotFoundException;
import com.E_Commerse.ECommerseBackendApplication.Exception.ProductNotFoundException;
import com.E_Commerse.ECommerseBackendApplication.Models.*;
import com.E_Commerse.ECommerseBackendApplication.Repository.CustomerRepository;
import com.E_Commerse.ECommerseBackendApplication.Repository.ProductRepository;
import com.E_Commerse.ECommerseBackendApplication.RequestDto.OrderRequestDto;
import com.E_Commerse.ECommerseBackendApplication.ResponseDto.OrderResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {

    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ProductRepository productRepository;

   @Autowired
   JavaMailSender emailSender;

    public OrderResponseDto placeOrder(OrderRequestDto orderRequestDto) throws Exception {
        Customer customer;
        try{
            customer = customerRepository.findById(orderRequestDto.getCustomerId()).get();
        }
        catch(Exception e){
            throw new CustomerNotFoundException("Invalid Customer id !!!");
        }

        Product product;
        try{
            product = productRepository.findById(orderRequestDto.getProductId()).get();
        }
        catch (Exception e){
            throw new ProductNotFoundException("Invalid Product Id");
        }

        if(product.getQuantity()<orderRequestDto.getRequiredQuantity()){
            throw new Exception("Sorry! Required quantity not available");
        }

        Ordered order = new Ordered();
        order.setTotalCost(orderRequestDto.getRequiredQuantity()* product.getPrice());
        order.setDeliveryCharge(40);
        Card card = customer.getCards().get(0);
        String cardNo = "";
        for(int i=0;i<card.getCardNo().length()-4;i++)
            cardNo += 'X';
        cardNo += card.getCardNo().substring(card.getCardNo().length()-4);
        order.setCardUsedForPayment(cardNo);

        Item item = new Item();
        item.setRequiredQuantity(orderRequestDto.getRequiredQuantity());
        item.setProduct(product);
        item.setOrder(order);
        order.getOrderedItems().add(item);
        order.setCustomer(customer);

        int leftQuantity = product.getQuantity()-orderRequestDto.getRequiredQuantity();
        if(leftQuantity<=0)
            product.setProductStatus(ProductStatus.OUT_OF_STOCK);
        product.setQuantity(leftQuantity);

        customer.getOrders().add(order);
        Customer savedCustomer = customerRepository.save(customer);
        Ordered savedOrder = savedCustomer.getOrders().get(savedCustomer.getOrders().size()-1);

        //prepare response DTO
        OrderResponseDto orderResponseDto = OrderResponseDto.builder()
                .productName(product.getProductName())
                .orderDate(savedOrder.getOrderDate())
                .quantityOrdered(orderRequestDto.getRequiredQuantity())
                .cardUsedForPayment(cardNo)
                .itemPrice(product.getPrice())
                .totalCost(order.getTotalCost())
                .deliveryCharge(40)
                .build();

        // send an email
        String text = "Congrats your order with total value "+order.getTotalCost()+" has been placed"+
                "\nOrder Details are mentioned below"+
                "\nCustomer name "+order.getCustomer()+
                "\nTotal Cost : "+order.getTotalCost()+
                "\nTotal Orders "+order.getOrderedItems()+
                "\nOrder date "+order.getOrderDate();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("backendspringrocks@gmail.com");
        message.setTo("akashhajared11@gmail.com");
        message.setSubject("Order Placed Notification");
        message.setText(text);
        emailSender.send(message);

        return orderResponseDto;
    }

}
