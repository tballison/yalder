Yalder
=============================

Overview
--------
Yalder (**y**et **a**nother **l**anguage **de**tecto**r**) is a Java-based language detector. You give it a CharacterSequence and a set of language "models", and it returns a score and a confidence value for each model which has at least a minimum confidence in matching, where a higher score means that it's more likely the text provided is written in that model's language.

Yalder supports the top 200 languages found in text online (based on Wikipedia statistics for page counts in various languages). 

Goal
----
The goal of Yalder is to support fast, accurate language detection across the most popular 200 languages.

"Popular" langauges are those with sufficient quantities of text in Wikipedia to build an appropriate model.

Design (attempt #1)
------
The approach I'm using is to use LLR (Log-Likelihood Ratio) and DF (document frequency) scores for 1-4 ngrams to pick the "best" set of these that is of a reasonable size. This is typically 500-2000, depending on the tradeoff of speed versus accuracy.

For each such set of the "best" ngrams extract from training data, I construct a feature vector where the weight of each ngram is based on the length; longer ngrams have higher weight (so no term frequency information). I then normalize the vector to have a length of one. This feature vector is the model for that specific language.

When given an arbitrary chunk of text to classify, I build a similar feature vector based on all ngrams from any of the active models that occurs in the target text. The dot product of this vector with each model vector is that model's score. Note that because of the use of LLR, the model's score is really a prediction of probability that the text uses the model's language versus any of the other languages in the corpus.

Current results are promising. It takes roughly 2.3 seconds to classify all 17000 chunks of text (1000 lines/language * 17 languages) that were used in Mike McCandless's [language detection speed test](http://blog.mikemccandless.com/2011/10/accuracy-and-performance-of-googles.html), which means it's faster than language-detection (and much faster than Tika). It's still much slower than Google's [Compact Language Detector](https://code.google.com/p/cld2/), which is written in C, but has a [Python binding](http://code.google.com/p/chromium-compact-language-detector) that Mike used to test.

Accuracy is 99.4% across all 17 languages. I can improve this by using special pair-wise language models (e.g. 'pt' and 'es', which only contain ngrams useful for disambiguating between those two languages). This impacts performance, but can be a worth-while tradeoff.

In general the more ngrams for a model the higher the accuracy, especially for short snippets of text, as it's more likely that the snippet will contain a reasonable number of the model's ngrams. This comes at the expense of speed, and to a lesser extent the size of the model data.

Design (attempt #2)
------
As part of comparing the original design to language-detector, I realized there was a [forked version](https://github.com/optimaize/language-detector) with better performance.

This fork also cleaned up some of the data loading issues with Nakatani Shuyo's [original version](https://github.com/shuyo/language-detection).

I clocked the new version at 1.2 seconds for 21,000 text samples (21 European Parlimentary proceedings languages, 1000 chunks per language), with an error rate of 0.29% (so 99.71% accuracy).

After spending too much time tweaking settings to improve on my initial design, I decided to revisit the approach used by the original [language-detection](https://github.com/shuyo/language-detection) project.

The basic idea is to assign a relative probability for every "known" ngram, which is any ngram found in any loaded model. As ngrams are generated by the tokenizer, the probability for each language is updated by multiplying it by the probability for that language, given that ngram. If a language model doesn't contain the target ngram, then it is given an arbitrary "unknown" probability. Every so often the langauge probabilities are normalized such that they once again sum to 1.0.

By minimizing allocations during tokenization, and a few other minor tweaks, I was able to get peformance down to about 550ms, with an error rate of 0.06% (99.94% accuracy) for the same set of 21,000 text samples.

I also mined Wikipedia data to build models for 200 languages.

The other advantage to this approach is that each model can be built independently, without needing to know details of the other languages. So additional languges can easily be added.

Implementation Details
----------------------

TBD

Model Format
------------
Each model contains the target language code, the tokenizer version, the detector version that the model was built for, and then a list of ngram hashes and normalized counts.


Known Issues
------------

Wikipedia data isn't conversational in nature. This meant, for example, that there were only two occurrences of the word "merci" in the French training text. This tends to skew probabilities, and thus increase the error rate when attempting to detect conversational text (like tweets). I compensated for this by mixing in a random sample of the European Parlimentary text, but that's only for the 21 languages of the EU.

Tweets require special pre-processing to avoid having hashtags, URLs, repeats (hahaha) etc. skew results.

Data Sets
---------

The bulk of the training data comes from a random crawl of the most important topic pages found on Wikipedia. These are the pages that are most likely to occur across many languages, and thus provide consistent data for the models. For details on this, see the Javadocs in the WikipediaCrawlTool.

We also have a random sampling of text chunks from Euro Parl.

