import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    public void train(String fileName) 
    {
        In in = new In(fileName);
        String text = in.readAll();
        for (int i = 0; i < text.length() - windowLength; i++) 
        {
            String window = text.substring(i, i + windowLength);
            char nextChar = text.charAt(i + windowLength);
            List probs = CharDataMap.get(window);
            if (probs == null) 
            {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(nextChar);
        }
        
        for (List probs : CharDataMap.values()) 
        {
            calculateProbabilities(probs);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) 
    {               
        int totalCount = 0;
        for (int i = 0; i < probs.getSize(); i++) 
        {
            totalCount += probs.get(i).count;
        }   
        double cumulativeCount = 0.0;  
        for (int i = 0; i < probs.getSize(); i++) 
        {
            CharData curr = probs.get(i);
            curr.p = (double) curr.count / totalCount;
            cumulativeCount += curr.count;
            curr.cp = cumulativeCount / totalCount; // חישוב מדויק
        }
    }
    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) 
    {
        double r = randomGenerator.nextDouble();
        for (int i = 0; i < probs.getSize(); i++) 
        {
            CharData curr = probs.get(i);
            if (curr.cp >= r) 
            {
                return curr.chr;
            }
        }
        return probs.get(probs.getSize() - 1).chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) 
    {
        if (initialText.length() >= textLength || initialText.length() < windowLength) 
        {
            return initialText;
        }
        StringBuilder sb = new StringBuilder(initialText);
        int charactersToGenerate = textLength - initialText.length();
        for (int i = 0; i < charactersToGenerate; i++) 
        {
            String currentWindow = sb.substring(sb.length() - windowLength); 
            List probs = CharDataMap.get(currentWindow);
            if (probs != null) 
            {
                char nextChar = getRandomChar(probs);
                sb.append(nextChar);
            } 
            else 
            {
                break; 
            }
        }

        return sb.toString();
    }

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) 
    {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int textLength = Integer.parseInt(args[2]);
        boolean random = args[3].equals("random");
        String fileName = args[4];
        LanguageModel lm;
        if (random) 
        {
            lm = new LanguageModel(windowLength);
        } 
        else 
        {
            int seed = (args.length > 5) ? Integer.parseInt(args[5]) : 20;
            lm = new LanguageModel(windowLength, seed);
        }
        lm.train(fileName);
        String generatedText = lm.generate(initialText, textLength);
        System.out.println(generatedText);
    }
}
