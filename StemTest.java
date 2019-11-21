package fitz;


import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StemTest {
    @Test
    public void porterstemmer() {

        List<String> words = Arrays.asList("John","payment","pay","payee","payer","deposit");
        PorterStemmer porterStemmer = new PorterStemmer();

        words.forEach(word -> {
            String stemmed = porterStemmer.stem(word);
            System.out.println(stemmed);
        });

    }
    @Test
    public  void sentencedetection() throws IOException {
        String paragraph = "This is a statement. This is another statement. "
                + "Now is an abstract word for time, "
                + "that is always flying. And my email address is google@gmail.com.";

        InputStream is = getClass().getResourceAsStream("en-sent.bin");
        SentenceModel model = new SentenceModel(is);

        SentenceDetectorME sdetector = new SentenceDetectorME(model);

        String[] sentences = sdetector.sentDetect(paragraph);
        for (String sentence: sentences
             ) {
            System.out.println(sentence);
        }

    }
    @Test
    public void tokenizerModel() throws Exception {

        InputStream inputStream = getClass()
                .getResourceAsStream("en-token.bin");
        TokenizerModel model = new TokenizerModel(inputStream);
        TokenizerME tokenizer = new TokenizerME(model);
        String[] tokens = tokenizer.tokenize("Baeldung is a Spring Resource 1111.");

        for (String token: tokens
             ) {
            System.out.println(token);
        }
    }
    @Test
    public  void tokenizerSimple() throws Exception{
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer
                .tokenize("John is 26 years old. His best friend's "
                        + "name is Leonard. He has a sister named Penny.");
        for (String token: tokens
        ) {
            System.out.println(token);
        }
    }
    @Test
    public void
    givenEnglishPersonModel_whenNER_thenPersonsAreDetected()
            throws Exception {

        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer
                .tokenize("John is 26 years old. His best friend's "
                        + "name is Leonard. He has a sister named Penny.");

        InputStream inputStreamNameFinder = getClass()
                .getResourceAsStream("en-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(
                inputStreamNameFinder);
        NameFinderME nameFinderME = new NameFinderME(model);
        Span[] spans = nameFinderME.find(tokens);

        for (Span span: spans
             ) {
            System.out.println(span.toString());
        }
    }
    @Test
    public void givenPOSModel_whenPOSTagging_thenPOSAreDetected()
            throws Exception {
        /*SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize("John has a sister named Penny.");*/

        String newDetails = "";

        String details = "John is 26 years old. His best friend's "
                + "name is Leonard. He has a sister named Penny.";
        String[] tokens = details.split("[^a-zA-Z]");

        InputStream inputStreamPOSTagger = getClass()
                .getResourceAsStream("en-pos-maxent.bin");
        POSModel posModel = new POSModel(inputStreamPOSTagger);
        POSTaggerME posTagger = new POSTaggerME(posModel);
        String[] tags = posTagger.tag(tokens);

        ArrayList<String> exTag = new ArrayList<>();
        exTag.addAll(Arrays.asList("NNP","NNPS"));

        String tag;

        for(int i = 0 ; i < tokens.length; i++){
            if(tokens[i].equals("")) continue;
            tag = tags[i];
            if(exTag.contains(tag)){
                newDetails += tokens[i] + " ";
                //names.add(tokens[i]);
            }else{
                if(tokens[i].length() > 4){
                    newDetails += tokens[i] + " ";
                    //validWords.add(tokens[i]);
                }
            }
        }
        /*for (String w : validWords
             ) {
            System.out.println(w);
        }*/
        System.out.println(newDetails);

    }
    @Test
    public void givenEnglishDictionary_whenLemmatize_thenLemmasAreDetected()
            throws Exception {

        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize("John has a sister named Penny.");

        InputStream inputStreamPOSTagger = getClass()
                .getResourceAsStream("en-pos-maxent.bin");
        POSModel posModel = new POSModel(inputStreamPOSTagger);
        POSTaggerME posTagger = new POSTaggerME(posModel);
        String[] tags = posTagger.tag(tokens);
        InputStream dictLemmatizer = getClass()
                .getResourceAsStream("en-lemmatizer.dict");
        DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(
                dictLemmatizer);
        String[] lemmas = lemmatizer.lemmatize(tokens, tags);
        for (String lemma: lemmas
             ) {
            System.out.println(lemma);
        }
    }
    @Test
    public    void similarity(){

        JaroWinklerDistance distance = new JaroWinklerDistance();
        System.out.println(distance.apply("angela usman","head bank usa inter offic"));

        CosineSimilarity dist = new CosineSimilarity();

        String c1 = "angela usman";
        String c2 = "head bank usa inter offic";

        Map<CharSequence, Integer> leftVector =
                Arrays.stream(c1.split(""))
                        .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));
        Map<CharSequence, Integer> rightVector =
                Arrays.stream(c2.split(""))
                        .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));

        System.out.println(1 - dist.cosineSimilarity(leftVector,rightVector));
    }
    @Test
    public  void enumTest(){


        System.out.println(Color.RED);
    }


}
