package demo.repository;

import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.googlecode.objectify.ObjectifyService;

import demo.model.Product;

@Repository
public class GoogleDatastoreProductRepository implements ProductRepository {

	@Resource
	private Cache cache;

	@Override
	public Iterable<Product> findAll() {
		return ObjectifyService.ofy().load().type(Product.class).list();
	}

	@Override
	public Product findOne(String sku) {
		Product product = cache.get(sku, Product.class);
		if (product == null) {
			product = ObjectifyService.ofy().load().type(Product.class).id(sku).now();
			if (product != null) {
				cache.put(sku, product);
			}
		}
		return product; 
	}

	@Override
	public Product save(Product product) {
		product.setSku(UUID.randomUUID().toString());
		ObjectifyService.ofy().save().entity(product).now();
		return product;
	}
}
