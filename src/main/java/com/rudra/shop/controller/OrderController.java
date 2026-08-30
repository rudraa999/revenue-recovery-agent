package com.rudra.shop.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rudra.shop.dto.CartItemDto;
import com.rudra.shop.model.Order;
import com.rudra.shop.model.User;
import com.rudra.shop.repository.OrderRepository;
import com.rudra.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/orders")
    public String viewUserOrders(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model,
            Authentication authentication) {

        if (page < 0) page = 0;
        if (size <= 0 || size > 50) size = 5;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage;
        User currentUser = null;

        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String username = getUsernameFromAuthentication(authentication);
            currentUser = userRepository.findByUsername(username);
            if (currentUser == null) {
                currentUser = userRepository.findByEmail(username);
            }

            if (currentUser != null) {
                // Fetch orders for this user by userId or email
                Page<Order> userOrders = orderRepository.findByUserId(currentUser.getId(), pageable);
                if (userOrders.isEmpty() && currentUser.getEmail() != null) {
                    userOrders = orderRepository.findByCustomerEmail(currentUser.getEmail(), pageable);
                }
                orderPage = userOrders;
            } else {
                orderPage = orderRepository.findByStatus("PAID", pageable);
            }
        } else {
            orderPage = orderRepository.findByStatus("PAID", pageable);
        }

        // If searching by orderNumber or email/phone
        if (search != null && !search.trim().isEmpty()) {
            String term = search.trim();
            Optional<Order> searchOrder = orderRepository.findByOrderNumber(term);
            if (searchOrder.isPresent()) {
                orderPage = new PageImpl<>(Collections.singletonList(searchOrder.get()), pageable, 1);
            } else {
                Page<Order> emailOrders = orderRepository.findByCustomerEmail(term, pageable);
                if (!emailOrders.isEmpty()) {
                    orderPage = emailOrders;
                }
            }
        }

        // Fallback: If user has no orders, show all paid orders paginated
        if (orderPage.isEmpty() && (search == null || search.trim().isEmpty())) {
            orderPage = orderRepository.findAll(pageable);
        }

        List<Order> orders = orderPage.getContent();

        // Parse items JSON for each order
        Map<String, List<CartItemDto>> orderItemsMap = new HashMap<>();
        for (Order ord : orders) {
            try {
                if (ord.getItemsJson() != null && !ord.getItemsJson().trim().isEmpty()) {
                    List<CartItemDto> items = objectMapper.readValue(ord.getItemsJson(), new TypeReference<List<CartItemDto>>() {});
                    orderItemsMap.put(ord.getOrderNumber(), items);
                } else {
                    orderItemsMap.put(ord.getOrderNumber(), new ArrayList<>());
                }
            } catch (Exception e) {
                orderItemsMap.put(ord.getOrderNumber(), new ArrayList<>());
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderItemsMap", orderItemsMap);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "orders";
    }

    @GetMapping("/orders/{orderNumber}")
    public String viewOrderDetail(@PathVariable("orderNumber") String orderNumber, Model model) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }

        Order order = orderOpt.get();
        List<CartItemDto> items = new ArrayList<>();
        try {
            if (order.getItemsJson() != null && !order.getItemsJson().trim().isEmpty()) {
                items = objectMapper.readValue(order.getItemsJson(), new TypeReference<List<CartItemDto>>() {});
            }
        } catch (Exception e) {
            items = new ArrayList<>();
        }

        model.addAttribute("order", order);
        model.addAttribute("items", items);

        return "orders";
    }

    @GetMapping("/admin/orders")
    public String adminManageOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAll(pageable);
        List<Order> orders = orderPage.getContent();

        Map<String, List<CartItemDto>> orderItemsMap = new HashMap<>();
        for (Order ord : orders) {
            try {
                if (ord.getItemsJson() != null && !ord.getItemsJson().trim().isEmpty()) {
                    List<CartItemDto> items = objectMapper.readValue(ord.getItemsJson(), new TypeReference<List<CartItemDto>>() {});
                    orderItemsMap.put(ord.getOrderNumber(), items);
                } else {
                    orderItemsMap.put(ord.getOrderNumber(), new ArrayList<>());
                }
            } catch (Exception e) {
                orderItemsMap.put(ord.getOrderNumber(), new ArrayList<>());
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderItemsMap", orderItemsMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());

        return "admin/orders";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
        }
        return "redirect:/admin/orders?success=true";
    }

    private String getUsernameFromAuthentication(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}
