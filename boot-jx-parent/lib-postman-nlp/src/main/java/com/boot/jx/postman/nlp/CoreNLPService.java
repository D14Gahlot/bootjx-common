package com.boot.jx.postman.nlp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.model.TagDocument;
import com.boot.utils.ArgUtil;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

@Component
@ConditionalOnProperty("postman.nlp.corenlp.enabled")
public class CoreNLPService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreNLPService.class);

	StanfordCoreNLP sentimentPipeline;

	StanfordCoreNLP tokenizerPipeline;

	StanfordCoreNLP nerPipeline;

	@PostConstruct
	public void init() throws FileNotFoundException, IOException {
		// Sentiments
		Properties sentimentProps = new Properties();
		sentimentProps.setProperty("annotators", "parse, sentiment");
		sentimentProps.setProperty("parse.binaryTrees", "true");
		sentimentProps.setProperty("enforceRequirements", "false");
		this.sentimentPipeline = new StanfordCoreNLP(sentimentProps);

		// Tokenizer
		Properties tokenizerProps = new Properties();
		tokenizerProps.setProperty("annotators", "tokenize ssplit");
		this.tokenizerPipeline = new StanfordCoreNLP(tokenizerProps);

		Properties nerProps = new Properties();
		nerProps.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");

		this.nerPipeline = new StanfordCoreNLP(nerProps);

	}

	public void addTags(String line, TagDocument tagDocument) {

		LOGGER.debug("SENTIMENT<");
		CoreDocument sentimentDoc = this.tokenizerPipeline.processToCoreDocument(line);
		sentimentPipeline.annotate(sentimentDoc);
		// normal output
		List<String> sencs = sentimentDoc.sentences().stream().map(mapper -> mapper.sentiment())
				.collect(Collectors.toCollection(() -> tagDocument.sentiments()));
		LOGGER.debug("SENTIMENT>");

		if (ArgUtil.is(line)) {
			// return;
		}

		LOGGER.debug("TAGS<");
		CoreDocument doc = nerPipeline.processToCoreDocument(line);
		// pipeline2.annotate(doc);

		if (ArgUtil.is(doc.entityMentions())) {
			for (CoreEntityMention em : doc.entityMentions()) {
				switch (em.entityType()) {
				case "PERSON":
					tagDocument.persons().add(em.text());
					break;
				case "COUNTRY":
					tagDocument.countries().add(em.text());
					break;
				case "CITY":
					tagDocument.cities().add(em.text());
					break;
				default:
					break;
				}
			}
		}
		LOGGER.debug("TAGS>");
	}
	/**
	 * public static void main(String[] args) throws FileNotFoundException,
	 * IOException { CoreNLPService service = new CoreNLPService(); service.init();
	 * service.addTags("Amazingly grateful beautiful friends are fulfilling an
	 * incredibly joyful accomplishment." + " What an truly terrible idea. John will
	 * kill you");
	 * 
	 * service.addTags("John is 26 years old. His best friend's " + "name is Lalit.
	 * He has a sister named Penny. And he lives in Mumbai india maharashtra"); }
	 **/
}
