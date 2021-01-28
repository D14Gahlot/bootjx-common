package com.boot.jx.postman.nlp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.model.TagDocument;
import com.boot.utils.ArgUtil;
import com.boot.utils.FileUtil;
import com.boot.utils.SysConfigUtil;

import opennlp.tools.doccat.BagOfWordsFeatureGenerator;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.doccat.FeatureGenerator;
import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

@Component
@ConditionalOnProperty("postman.nlp.opennlp.enabled")
public class OpenNLPService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenNLPService.class);

	private SentenceModel sentenceModel;

	private DoccatModel docCatModel;

	private TokenizerModel tokenizerModel;

	private POSModel posModel;

	private LemmatizerModel lemmatizerModel;

	private TokenNameFinderModel tokenNameFinderModelPerson;
	private TokenNameFinderModel tokenNameFinderModelLocation;
	private TokenNameFinderModel tokenNameFinderModelOrganization;

	private LanguageDetectorModel languageDetectorModel;

	@PostConstruct
	public void init() throws FileNotFoundException, IOException {

		try (InputStream modelIn = FileUtil
				.getExternalOrInternalResourceAsStream("ext-resources/apache-open-nlp/en-sent.bin")) {
			this.sentenceModel = new SentenceModel(modelIn);
		}

		try (InputStream modelIn = FileUtil
				.getExternalOrInternalResourceAsStream("ext-resources/apache-open-nlp/en-token.bin")) {
			this.tokenizerModel = new TokenizerModel(modelIn);
		}

		try (InputStream modelIn = FileUtil
				.getExternalOrInternalResourceAsStream("ext-resources/apache-open-nlp/en-pos-maxent.bin")) {
			this.posModel = new POSModel(modelIn);
		}

		try (InputStream modelIn = FileUtil
				.getExternalOrInternalResourceAsStream("ext-resources/apache-open-nlp/en-lemmatizer.bin")) {
			this.lemmatizerModel = new LemmatizerModel(modelIn);
		}

		try (InputStream modelIn = FileUtil
				.getExternalOrInternalResourceAsStream("ext-resources/apache-open-nlp/en-ner-person.bin")) {
			this.tokenNameFinderModelPerson = new TokenNameFinderModel(modelIn);
		}

		try (InputStream modelIn = FileUtil
				.getExternalOrInternalResourceAsStream("ext-resources/apache-open-nlp/en-ner-location.bin")) {
			this.tokenNameFinderModelLocation = new TokenNameFinderModel(modelIn);
		}

		try (InputStream modelIn = FileUtil
				.getExternalOrInternalResourceAsStream("ext-resources/apache-open-nlp/en-ner-organization.bin")) {
			this.tokenNameFinderModelOrganization = new TokenNameFinderModel(modelIn);
		}

		try (InputStream modelIn = FileUtil
				.getExternalOrInternalResourceAsStream("ext-resources/apache-open-nlp/langdetect-183-fromApache.bin")) {
			this.languageDetectorModel = new LanguageDetectorModel(modelIn);
		}

		trainCategorizerModelFromFile();
	}

	public TagDocument addTags(String userInput, TagDocument nlpDocument) throws FileNotFoundException, IOException {
		String[] sentences = breakSentences(userInput);

		LanguageDetectorME languageDetectorME = new LanguageDetectorME(languageDetectorModel);
		Language[] langs = languageDetectorME.predictLanguages(userInput);

		if (ArgUtil.is(langs)) {
			java.util.stream.IntStream.range(0, Math.min(3, langs.length)).filter(i -> langs[i].getConfidence() > 0.01)
					.mapToObj(i -> langs[i].getLang()).collect(Collectors.toCollection(() -> nlpDocument.langs()));
		}

		for (String sentence : sentences) {
			// Separate words from each sentence using tokenizer.
			String[] tokens = tokenizeSentence(sentence);

			// Tag separated words with POS tags to understand their gramatical structure.
			String[] posTags = detectPOSTags(tokens);

			// Lemmatize each word so that its easy to categorize.
			String[] lemmas = lemmatizeTokens(tokens, posTags);

			// Determine BEST category using lemmatized tokens used a mode that we trained
			// at start.

			nlpDocument.categories().add(detectCategory(lemmas));

			String[] simpleTokens = SimpleTokenizer.INSTANCE.tokenize(sentence);

			NameFinderME personFinderME = new NameFinderME(tokenNameFinderModelPerson);
			String[] persons = Span.spansToStrings(personFinderME.find(simpleTokens), simpleTokens);
			if (ArgUtil.is(persons)) {
				nlpDocument.persons().addAll(Arrays.asList(persons));
			}

			NameFinderME locationFinderME = new NameFinderME(tokenNameFinderModelLocation);
			String[] locations = Span.spansToStrings(locationFinderME.find(simpleTokens), simpleTokens);
			if (ArgUtil.is(locations)) {
				nlpDocument.locations().addAll(Arrays.asList(locations));
			}

			NameFinderME orgFinderME = new NameFinderME(tokenNameFinderModelOrganization);
			String[] organizations = Span.spansToStrings(orgFinderME.find(simpleTokens), simpleTokens);
			if (ArgUtil.is(organizations)) {
				nlpDocument.organizations().addAll(Arrays.asList(organizations));
			}
		}

		return nlpDocument;
	}

	public String[] breakSentences(String data) throws FileNotFoundException, IOException {
		if (ArgUtil.is(this.sentenceModel)) {
			SentenceDetectorME myCategorizer = new SentenceDetectorME(this.sentenceModel);
			String[] sentences = myCategorizer.sentDetect(data);
			printItems("Sentence Detection", sentences);
			return sentences;
		}
		return null;
	}

	public String detectCategory(String[] finalTokens) throws IOException {
		// Initialize document categorizer tool
		DocumentCategorizerME myCategorizer = new DocumentCategorizerME(docCatModel);
		// Get best possible category.
		double[] probabilitiesOfOutcomes = myCategorizer.categorize(finalTokens);
		String category = myCategorizer.getBestCategory(probabilitiesOfOutcomes);
		printItems("Category", category);
		return category;
	}

	public String[] tokenizeSentence(String sentence) throws FileNotFoundException, IOException {
		// Better to read file once at start of program & store model in instance
		// variable. but keeping here for simplicity in understanding.
		if (ArgUtil.is(this.tokenizerModel)) {
			// Initialize tokenizer tool
			TokenizerME myCategorizer = new TokenizerME(this.tokenizerModel);
			// Tokenize sentence.
			String[] tokens = myCategorizer.tokenize(sentence);
			printItems("Tokenizer", tokens);
			return tokens;
		}
		return null;
	}

	public String[] detectPOSTags(String[] tokens) throws IOException {
		// Better to read file once at start of program & store model in instance
		// variable. but keeping here for simplicity in understanding.
		if (ArgUtil.is(this.posModel)) {
			// Initialize POS tagger tool
			POSTaggerME myCategorizer = new POSTaggerME(this.posModel);
			// Tag sentence.
			String[] posTokens = myCategorizer.tag(tokens);
			printItems("POS Tags", posTokens);
			return posTokens;
		}
		return null;
	}

	public String[] lemmatizeTokens(String[] tokens, String[] posTags) throws InvalidFormatException, IOException {
		// Better to read file once at start of program & store model in instance
		// variable. but keeping here for simplicity in understanding.
		if (ArgUtil.is(this.lemmatizerModel)) {
			// Tag sentence.
			LemmatizerME myCategorizer = new LemmatizerME(this.lemmatizerModel);
			String[] lemmaTokens = myCategorizer.lemmatize(tokens, posTags);
			printItems("Lemmatizer", lemmaTokens);
			return lemmaTokens;

		}
		return null;
	}

	public void trainCategorizerModelFromFile() throws FileNotFoundException, IOException {
		// faq-categorizer.txt is a custom training data with categories as per our chat
		// requirements.
		File file = FileUtil.getExternalOrInternalFile("ext-resources/apache-open-nlp/en-categorizer.text");

		InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(file);
		ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
		ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

		DoccatFactory factory = new DoccatFactory(new FeatureGenerator[] { new BagOfWordsFeatureGenerator() });

		TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
		params.put(TrainingParameters.CUTOFF_PARAM, 0);

		// Train a model with classifications from above file.
		this.docCatModel = DocumentCategorizerME.train("en", sampleStream, params, factory);
	}

	public static void printItems(String name, String... lemmaTokens) {
		// System.out.println(name + " : " +
		// Arrays.stream(lemmaTokens).collect(Collectors.joining(" | ")));
	}

}
