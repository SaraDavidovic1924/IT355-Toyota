/*
 * *
 *  * @author sarita
 *
 */

/*
 * *
 *  * @author sarita
 *
 */

/*
 * *
 *  * @author sarita
 *
 */

/*
 * *
 *  * @author sarita
 *
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it355.pz.controller;

import it355.pz.model.Item;
import it355.pz.model.Orders;
import it355.pz.model.OrdersDetail;
import it355.pz.service.CustomerService;
import it355.pz.service.OrdersDetailService;
import it355.pz.service.ProductService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import it355.pz.service.OrdersService;
import java.security.Principal;

@Controller
@RequestMapping("/customer/cart")
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrdersService orderService;

    @Autowired
    private OrdersDetailService ordersDetailService;

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap model) {
        model.put("title", "Cart");
        model.put("action", "cart");
        return "cart";

    }

    @RequestMapping(value = "/buy/{id}", method = RequestMethod.GET)
    public String buy(@PathVariable("id") int id, HttpSession session, ModelMap model) {
        model.put("title", "Cart");
        if (session.getAttribute("cart") == null) {
            List<Item> cart = new ArrayList<Item>();
            cart.add(new Item(productService.getProductById(id), 1));
            session.setAttribute("cart", cart);

        } else {
            List<Item> cart = (List<Item>) session.getAttribute("cart");
            int index = ifProductInCartExists(id, session);
            if (index == -1) {
                cart.add(new Item(productService.getProductById(id), 1));
            } else {
                int quantity = cart.get(index).getQunatity() + 1;
                cart.get(index).setQunatity(quantity);
                session.setAttribute("cart", cart);
            }

        }
        return "cart";

    }

    private int ifProductInCartExists(int id, HttpSession session) {
        List<Item> cart = (List<Item>) session.getAttribute("cart");
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProduct().getProductId() == id) {
                return i;
            }

        }
        return -1;
    }

    @RequestMapping(value = "/delete/{index}", method = RequestMethod.GET)
    public String deleteItem(@PathVariable("index") int index, HttpSession session) {
        List<Item> cart = (List<Item>) session.getAttribute("cart");
        cart.remove(index);
        return "redirect:/customer/cart";
    }

    @RequestMapping(value = "/checkout", method = RequestMethod.GET)
    public String checkout(HttpSession session, ModelMap model, Principal principal) {
        model.put("title", "Cart");

        if (session.getAttribute("cart") == null) {
            model.addAttribute("error", "Please insert products to cart");
            return "cart";
        } else {
            // save Orders
            Orders orders = new Orders();
            orders.setUsername(principal.getName());
            orders.setDateCreation(new Date());
            orders.setOrdersName("New Order");

            Orders newOrder = orderService.addOrders(orders);
            //orderService.addOrders(orders);

            // save OrdersDetail
            List<Item> cart = (List<Item>) session.getAttribute("cart");
            for (Item item : cart) {
                OrdersDetail ordersDetail = new OrdersDetail();
                ordersDetail.setProductId(item.getProduct().getProductId());
                ordersDetail.setOrdersId(newOrder.getOrdersId());
                ordersDetail.setPrice(item.getProduct().getProductPrice());
                ordersDetail.setQuantity(item.getQunatity());
                ordersDetailService.addOrdersDetail(ordersDetail);
            }

            //Clear cart
            session.removeAttribute("cart");

            return "thanks";
        }

    }

}
