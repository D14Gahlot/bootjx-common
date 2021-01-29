package com.boot.jx.postman.doc;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class TemplatesReplies {

	@Autowired
	MongoTemplate mongoTemplate;

	@PostConstruct
	public void init() {
		List<TemplateReply> list = new ArrayList<TemplateReply>();
		TemplateReply temp1 = new TemplateReply();
		temp1.setName("GIRL_AND_BIKE");
		temp1.setTitle("Girl and bike");
		temp1.setType("IMAGE");
		temp1.setCategory("Gallery1");
		temp1.setUrl("https://res.cloudinary.com/www-mehery-com/image/upload/v1611688334/samples/bike.jpg");
		temp1.setContent("See this Nice Pic");
		mongoTemplate.save(temp1);

		TemplateReply temp2 = new TemplateReply();
		temp2.setName("OFFICE_N_WORK");
		temp2.setTitle("Office & Work");
		temp2.setType("IMAGE");
		temp2.setCategory("Gallery1");
		temp2.setUrl("https://res.cloudinary.com/www-mehery-com/image/upload/v1611688339/samples/imagecon-group.jpg");
		temp2.setContent("Work environment");
		mongoTemplate.save(temp2);

		TemplateReply temp3 = new TemplateReply();
		temp3.setName("KITTEN_PLAYING");
		temp3.setTitle("Kitten Playing");
		temp3.setType("IMAGE");
		temp3.setCategory("Animals");
		temp3.setUrl(
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688341/samples/animals/kitten-playing.gif");
		temp3.setContent("Happy Kitten");
		mongoTemplate.save(temp3);

		TemplateReply temp4 = new TemplateReply();
		temp4.setName("THREE_DOGS");
		temp4.setTitle("Three Dogs");
		temp4.setType("IMAGE");
		temp4.setCategory("Animals");
		temp4.setUrl(
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688335/samples/animals/three-dogs.jpg");
		temp4.setContent("Gang of Dogs");
		mongoTemplate.save(temp4);

		mongoTemplate.save(create("REINDEER", "Reindeer", "Animals", "In Snow",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688331/samples/animals/reindeer.jpg"));

		mongoTemplate.save(create("CAT", "Cat", "Animals", "Bad Cat",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688330/samples/animals/cat.jpg"));

		mongoTemplate.save(create("ACCESSORIES BAG", "Accessories Bag", "Ecommerce", "Cool bag",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688338/samples/ecommerce/accessories-bag.jpg"));

		mongoTemplate.save(create("LEATHER BAG GRAY", "Leather Bag Gray", "Ecommerce", "Formal bag",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688338/samples/ecommerce/leather-bag-gray.jpg"));

		mongoTemplate.save(create("SHOES", "Shoes", "Ecommerce", "Purple Shoes",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688333/samples/ecommerce/shoes.png"));

	}

	private TemplateReply create(String name, String title, String category, String content, String url) {
		TemplateReply temp5 = new TemplateReply();
		temp5.setName(name);
		temp5.setTitle(title);
		temp5.setType("IMAGE");
		temp5.setCategory(category);
		temp5.setUrl(url);
		temp5.setContent(content);
		return temp5;
	}

}
