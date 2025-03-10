package com.boot.jx.postman.nlp;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.TagDocument;
import com.boot.utils.ArgUtil;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

@Component
@ConditionalOnProperty(value = "postman.nlp.corenlp.enabled", havingValue = "true")
public class CoreNLPService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreNLPService.class);

	StanfordCoreNLP sentimentPipeline;

	StanfordCoreNLP tokenizerPipeline;

	StanfordCoreNLP nerPipeline;

	private boolean initd;

	@Autowired
	PMEnvironment pmEnvironment;

	@PostConstruct
	public void init() {
		try {
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
			this.initd = true;

		} catch (Throwable e) {
			LOGGER.error("CoreNLPService NOT Working", e);
		}

	}

	public TagDocument addTags(String line, TagDocument tagDocument) {

		if (!initd || !pmEnvironment.config().prefsEntry("postman.nlp.corenlp.enabled").asBoolean()) {
			return tagDocument;
		}

		boolean detectSentiment = pmEnvironment.config().prefsEntry("postman.nlp.detect.sentiment").asBoolean();
		boolean detectPersons = pmEnvironment.config().prefsEntry("postman.nlp.detect.persons").asBoolean();
		boolean detectCountries = pmEnvironment.config().prefsEntry("postman.nlp.detect.countries").asBoolean();
		boolean detectCities = pmEnvironment.config().prefsEntry("postman.nlp.detect.cities").asBoolean();

		if (ArgUtil.none(detectSentiment, detectPersons, detectCountries, detectCities)) {
			return tagDocument;
		}

		try {

			if (detectSentiment) {
				LOGGER.debug("SENTIMENT<");
				CoreDocument sentimentDoc = this.tokenizerPipeline.processToCoreDocument(line);
				sentimentPipeline.annotate(sentimentDoc);
				// normal output
				List<String> sencs = sentimentDoc.sentences().stream().map(mapper -> mapper.sentiment())
						.collect(Collectors.toCollection(() -> tagDocument.sentiments()));
				LOGGER.debug("SENTIMENT>");
			}

			if (ArgUtil.is(line)) {
				// return;
			}

			LOGGER.debug("TAGS<");
			CoreDocument doc = nerPipeline.processToCoreDocument(line);
			// pipeline2.annotate(doc);

			if (ArgUtil.is(doc.entityMentions()) && (detectPersons || detectCountries || detectCities)) {
				for (CoreEntityMention em : doc.entityMentions()) {
					switch (em.entityType()) {
					case "PERSON":
						if (detectPersons)
							tagDocument.persons().add(em.text());
						break;
					case "COUNTRY":
						if (detectCountries)
							tagDocument.countries().add(em.text());
						break;
					case "CITY":
						if (detectCities)
							tagDocument.cities().add(em.text());
						break;
					default:
						break;
					}
				}
			}
			LOGGER.debug("TAGS>");

		} catch (Exception e) {
			LOGGER.error("Error While Adding Tag", e);
		}

		return tagDocument;
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
