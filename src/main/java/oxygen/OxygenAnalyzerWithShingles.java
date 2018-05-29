package oxygen;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.shingle.ShingleFilter;

public class OxygenAnalyzerWithShingles extends OxygenAnalyzerBase {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        /* This is the main point of the analyzer - Tegra */
        //TODO Change the analyzer
        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new StandardFilter(source);    // Basic initialization
        result = new EnglishPossessiveFilter(result);       // Removes ' symbol (exmpl: Harry's book -> Harry book)
        result = new LowerCaseFilter(result);               // Self explanatory

        result = new StopFilter(result, stopwords);         // Stop words
        if (!stemExclusionSet.isEmpty()) {
            result = new SetKeywordMarkerFilter(result, stemExclusionSet); // Stemming exclusions
        }

        result = new ShingleFilter(result);                 // min shingle is by default 2
        ((ShingleFilter) result).setOutputUnigrams(true);
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(".*_.*");
        result = new PatternReplaceFilter(result, regex, "", true);
        result = new PorterStemFilter(result);              // Common algo, results are as good as any other filter


        return new TokenStreamComponents(source, result);
    }

}