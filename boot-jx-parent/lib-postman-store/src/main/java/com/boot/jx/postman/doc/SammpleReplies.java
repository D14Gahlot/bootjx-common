package com.boot.jx.postman.doc;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.utils.ArgUtil;
import com.boot.utils.UniqueID;

@Component
public class SammpleReplies {

	@Autowired
	MongoTemplate mongoTemplate;

	@PostConstruct
	public void init() {

		mongoTemplate.save(createTemplateReply("GIRL_AND_BIKE", "Girl and bike", "Gallery1", "See this Nice Pic",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688334/samples/bike.jpg"));

		mongoTemplate.save(createTemplateReply("OFFICE_N_WORK", "Office & Work", "Gallery1", "Work environment",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688339/samples/imagecon-group.jpg"));

		mongoTemplate.save(createTemplateReply("KITTEN_PLAYING", "Kitten Playing", "Animals", "Happy Kitten",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688341/samples/animals/kitten-playing.gif"));

		mongoTemplate.save(createTemplateReply("THREE_DOGS", "Three Dogs", "Animals", "Gang of Dogs",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688335/samples/animals/three-dogs.jpg"));

		mongoTemplate.save(createTemplateReply("REINDEER", "Reindeer", "Animals", "In Snow",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688331/samples/animals/reindeer.jpg"));

		mongoTemplate.save(createTemplateReply("CAT", "Cat", "Animals", "Bad Cat",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688330/samples/animals/cat.jpg"));

		mongoTemplate.save(createTemplateReply("ACCESSORIES BAG", "Accessories Bag", "Ecommerce", "Cool bag",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688338/samples/ecommerce/accessories-bag.jpg"));

		mongoTemplate.save(createTemplateReply("LEATHER BAG GRAY", "Leather Bag Gray", "Ecommerce", "Formal bag",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688338/samples/ecommerce/leather-bag-gray.jpg"));

		mongoTemplate.save(createTemplateReply("SHOES", "Shoes", "Ecommerce", "Purple Shoes",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688333/samples/ecommerce/shoes.png"));

		// Quick Replies
		mongoTemplate.save(createQuickReply("Hello", "greeting"));
		mongoTemplate.save(createQuickReply("Very Good Morning", "greeting-morning"));
		mongoTemplate.save(createQuickReply("Very Good After Noon", "greeting-afternoon"));
		mongoTemplate.save(createQuickReply("Very Good Evening", "greeting-evening"));
		mongoTemplate.save(createQuickReply("Nice talking too.", "conversation-complete"));
		mongoTemplate.save(createQuickReply("Your welcome.", "conversation-complete"));

	}

	private TemplateReply createTemplateReply(String name, String title, String category, String content, String url) {

		TemplateReply temp5 = mongoTemplate.findById(name, TemplateReply.class);
		if (ArgUtil.isEmpty(temp5)) {
			temp5 = new TemplateReply();
		}

		temp5.setName(name);
		temp5.setTitle(title);
		temp5.setType("IMAGE");
		temp5.setCategory(category);
		temp5.setUrl(url);
		temp5.setContent(content);

		return temp5;
	}

	private QuickReply createQuickReply(String title, String category) {
		QuickReply.CompositeKey id = new QuickReply.CompositeKey();
		id.setCategory(category);
		id.setSubject(title);
		QuickReply temp5 = mongoTemplate.findById(id, QuickReply.class);
		if (ArgUtil.isEmpty(temp5)) {
			temp5 = new QuickReply();
			temp5.setId(id);
			temp5.setUniqueId(UniqueID.generateString());
		}
		return temp5;
	}

}
