package oxygen;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.util.Arrays;
import java.util.List;

/**
 * Base version of Oxygen Custom Analyzer
 */
public class OxygenAnalyzerBase extends StopwordAnalyzerBase {
    public static final CharArraySet OXYGEN_EXCLUSION_SET;

    /**
     * List of stemming exclusions
     */
    static {
        final List<String> exclusionSet = Arrays.asList(
                "u.s.a", "u.s.a.", "u.s", "u.s."
        );
        final CharArraySet stopSet = new CharArraySet(exclusionSet, false);
        OXYGEN_EXCLUSION_SET = CharArraySet.unmodifiableSet(stopSet);
    }

    protected final CharArraySet stemExclusionSet;
    protected final CharArraySet stopwords;

    /**
     * Creates default Oxygen Analyzer
     */
    public OxygenAnalyzerBase() {
        this(getDefaultStopSet());
    }

    /**
     * Builds an analyzer with the given stop words. If a non-empty stem exclusion set is
     * provided this analyzer will add a {@link SetKeywordMarkerFilter} before
     * stemming.
     *
     * @param stopWords        a stopword set
     * @param stemExclusionSet a set of terms not to be stemmed
     */
    public OxygenAnalyzerBase(CharArraySet stopWords, CharArraySet stemExclusionSet) {
        super(stopWords);
        this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
        this.stopwords = CharArraySet.unmodifiableSet(CharArraySet.copy(stopWords));
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopWords a stopword set
     */
    public OxygenAnalyzerBase(CharArraySet stopWords) {
        this(stopWords, OXYGEN_EXCLUSION_SET);
    }

    /**
     * Returns an unmodifiable instance of the default stop words set.
     *
     * @return default stop words set.
     */
    public static CharArraySet getDefaultStopSet() {
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }

    /**
     * @return Oxygen Analyzer type
     */
    public static String getShingleInfo() {
        return new String("without shingles");
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new StandardFilter(in);
        result = new LowerCaseFilter(result);
        return result;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new StandardFilter(source);    // Basic initialization
        result = new EnglishPossessiveFilter(result);       // Removes ' symbol (exmpl: Harry's book -> Harry book)
        result = new LowerCaseFilter(result);               // Self explanatory
        result = new StopFilter(result, stopwords);         // Stop words
        if (!stemExclusionSet.isEmpty()) {
            result = new SetKeywordMarkerFilter(result, stemExclusionSet); // Stemming exclusions
        }
        result = new PorterStemFilter(result);              // Common algo, results are as good as any other filter
        return new TokenStreamComponents(source, result);
    }

    /**
     * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer class
     * accesses the static final set the first time.;
     */
    private static class DefaultSetHolder {
        static final CharArraySet DEFAULT_STOP_SET = Constants.OXYGEN_STOP_SET;
    }
}
