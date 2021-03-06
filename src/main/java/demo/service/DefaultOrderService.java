package demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import demo.exception.RequestValidationException;
import demo.model.Order;
import demo.model.OrderItem;
import demo.model.Product;
import demo.repository.OrderRepository;
import demo.repository.ProductRepository;

@Service
public class DefaultOrderService implements OrderService {

	@Resource
	private OrderRepository orderRepository;

	@Resource(name = "googleDatastoreProductRepository")
	private ProductRepository productRepository;

	@Override
	public Iterable<Order> getAll() {
		return orderRepository.findAll();
	}

	@Override
	public Order getById(Long id) {
		return orderRepository.findOne(id);
	}

	@Override
	public Long save(Order order) {
		validateOrder(order);

		return orderRepository.save(order);
	}

	@Override
	public void update(Long id, Order order) {
		// TODO : add validation also!
		order.setId(id);
		orderRepository.save(order);
	}

	@Override
	public void delete(Long id) {
		orderRepository.delete(id);
	}

	// TODO : move to separate validator.
	private void validateOrder(Order order) {
		List<OrderItem> orderItems = order.getOrderItems();
		

		List<String> productCodes = new ArrayList<>();
		for (OrderItem orderItem : orderItems) {
			productCodes.add(orderItem.getProduct().getCode());
		}
		
		Map<String, Product> allOrderProducts = productRepository.findAllByCodes(productCodes);
		
		for (OrderItem orderItem : orderItems) {
			double orderPrice = orderItem.getPrice();
			Product product = allOrderProducts.get(orderItem.getProduct().getCode());
			if (product == null) {
				throw new RequestValidationException("Specified product: " + orderItem.getProduct().getCode() + " doesn't exist");
			}

			double productPrice = product.getPrice();

			if (orderPrice < productPrice) {
				throw new RequestValidationException("Order price " + orderPrice + " is below product price" + productPrice);
			}
		}
	}

	@Override
	public Iterable<Order> getByOrderDate(Date orderDate) {
		return orderRepository.findAllByCreatedOn(orderDate);
	}
}
