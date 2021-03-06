package org.krugler.yalder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageLocale {

    private static Map<String, LanguageLocale> LOCALES = new HashMap<String, LanguageLocale>();
    
    // Java Locale support uses ISO 639-2 bibliographic codes, versus the terminological codes
    // (which is what we use). So we have a table to normalize from bib to term, and we need
    // a second table to get the names for terminological codes.
    
    // From http://www.loc.gov/standards/iso639-2/php/English_list.php
    private static Map<String, String> BIB_TO_TERM_MAPPING = new HashMap<String, String>();
    
    static {
        BIB_TO_TERM_MAPPING.put("alb", "sqi");
        BIB_TO_TERM_MAPPING.put("arm", "hye");
        BIB_TO_TERM_MAPPING.put("baq", "eus");
        BIB_TO_TERM_MAPPING.put("bur", "mya");
        BIB_TO_TERM_MAPPING.put("chi", "zho");
        BIB_TO_TERM_MAPPING.put("cze", "ces");
        BIB_TO_TERM_MAPPING.put("dut", "nld");
        BIB_TO_TERM_MAPPING.put("geo", "kat");
        BIB_TO_TERM_MAPPING.put("ger", "deu");
        BIB_TO_TERM_MAPPING.put("gre", "ell");
        BIB_TO_TERM_MAPPING.put("ice", "isl");
        BIB_TO_TERM_MAPPING.put("mac", "mkd");
        BIB_TO_TERM_MAPPING.put("may", "msa");
        BIB_TO_TERM_MAPPING.put("mao", "mri");
        BIB_TO_TERM_MAPPING.put("rum", "ron");
        BIB_TO_TERM_MAPPING.put("per", "fas");
        BIB_TO_TERM_MAPPING.put("slo", "slk");
        BIB_TO_TERM_MAPPING.put("tib", "bod");
        BIB_TO_TERM_MAPPING.put("wel", "cym");
        BIB_TO_TERM_MAPPING.put("fre", "fra");
    }

    // Java Locale support uses 
    private static Map<String, String> TERM_CODE_TO_NAME = new HashMap<String, String>();
    static {
        TERM_CODE_TO_NAME.put("sqi", "Albanian");
        TERM_CODE_TO_NAME.put("hye", "Armenian");
        TERM_CODE_TO_NAME.put("eus", "Basque");
        TERM_CODE_TO_NAME.put("zho", "Chinese");
        TERM_CODE_TO_NAME.put("ces", "Czech");
        TERM_CODE_TO_NAME.put("nld", "Dutch");
        TERM_CODE_TO_NAME.put("kat", "Georgian");
        TERM_CODE_TO_NAME.put("deu", "German");
        TERM_CODE_TO_NAME.put("ell", "Greek");
        TERM_CODE_TO_NAME.put("isl", "Icelandic");
        TERM_CODE_TO_NAME.put("mkd", "Macedonian");
        TERM_CODE_TO_NAME.put("msa", "Malay");
        TERM_CODE_TO_NAME.put("mri", "Maori");
        TERM_CODE_TO_NAME.put("ron", "Romanian");
        TERM_CODE_TO_NAME.put("fas", "Persian");
        TERM_CODE_TO_NAME.put("slk", "Slovak");
        TERM_CODE_TO_NAME.put("bod", "Tibetan");
        TERM_CODE_TO_NAME.put("cym", "Welsh");
        TERM_CODE_TO_NAME.put("fra", "French");
    }
    
    private static Map<String, String> IMPLICIT_SCRIPT = new HashMap<String, String>();
    static {
        // TODO add full set of these.
        IMPLICIT_SCRIPT.put("aze", "Latn");
        IMPLICIT_SCRIPT.put("bos", "Latn");
        IMPLICIT_SCRIPT.put("uzb", "Latn");
        IMPLICIT_SCRIPT.put("tuk", "Latn");
        IMPLICIT_SCRIPT.put("msa", "Latn");
        
        IMPLICIT_SCRIPT.put("mon", "Cyrl");
        IMPLICIT_SCRIPT.put("srp", "Cyrl");
        
        IMPLICIT_SCRIPT.put("pan", "Guru");
    }
    
    private static Map<String, String> IMPLICIT_COUNTRY_SCRIPT = new HashMap<String, String>();
    static {
        // TODO add full set of these.
        IMPLICIT_COUNTRY_SCRIPT.put("zhoTW", "Hant");
        IMPLICIT_COUNTRY_SCRIPT.put("zhoCN", "Hans");
    }
    
    
    
    private static Map<String, String> SPECIFIC_TO_MACRO = new HashMap<String, String>();
    static {
        // TODO add full set of these.
        SPECIFIC_TO_MACRO.put("nno", "nor");
        SPECIFIC_TO_MACRO.put("nob", "nor");
    }

    private String _language;   // ISO 639-2/T language code (3 letters)
    private String _script;     // ISO 15924 script name (4 letters) or empty
    
    public static LanguageLocale fromString(String languageName) {
        if (!LOCALES.containsKey(languageName)) {

            synchronized (LOCALES) {
                // Make sure nobody added it after we did our check.
                if (!LOCALES.containsKey(languageName)) {
                    LOCALES.put(languageName, new LanguageLocale(languageName));
                }
            }
        }
        
        return LOCALES.get(languageName);
    }
    
    /**
     * Return a language name (suitable for passing into the fromString method) from
     * a language (2 or 3 character) and script (always four character, e.g. "Latn").
     * 
     * @param language
     * @param script
     * @return standard format for language name
     */
    public static String makeLanguageName(String language, String script) {
        return String.format("%s-%s", language, script);
    }
    
    private LanguageLocale(String languageTag) {
        Locale locale = Locale.forLanguageTag(languageTag);
        
        // First convert the language code to ISO 639-2/T
        _language = locale.getISO3Language();
        if (_language.isEmpty()) {
            throw new IllegalArgumentException("A valid language code must be provided");
        }
        
        // See if we need to convert from 639-2/B to 639-2/T
        if (BIB_TO_TERM_MAPPING.containsKey(_language)) {
            _language = BIB_TO_TERM_MAPPING.get(_language);
        }
        
        // Now see if we have a script.
        _script = locale.getScript();
        // TODO - do we want to verify it's a valid script? What happens if you pass
        // in something like en-latin?
        
        // If the script is empty, and we have a country, then we might want to
        // explicitly set the script as that's how it was done previously (e.g. zh-TW
        // to set the Hant script, versus zh-CN for Hans script)
        if (_script.isEmpty()) {
            String country = locale.getCountry();
            // TODO convert UN M.39 code into two letter country 
            if (!country.isEmpty()) {
                // TODO look up language + country
                String languagePlusCountry = _language + country;
                if (IMPLICIT_COUNTRY_SCRIPT.containsKey(languagePlusCountry)) {
                    _script = IMPLICIT_COUNTRY_SCRIPT.get(languagePlusCountry);
                }
            }
        }
        
        // If the script isn't empty, and it's the default script for the language,
        // then remove it so we don't include it in comparisons.
        if (!_script.isEmpty() && _script.equals(IMPLICIT_SCRIPT.get(_language))) {
            _script = "";
        }
    }

    // We add a special "weakly equal" method that's used to
    // decide if the target locale matches us. The "target" in
    // this case is what we're looking for (what we want it to be)
    // versus what we got from detection
    // versus what we expect it to be. In this case,
    //  target      result      match?
    //  zho         zho         yes
    //  zho-Hant    zho-Hant    yes
    //  zho         zho-Hant    yes
    //  zho-Hant    zho         no (want explicitly trad chinese, got unspecified)
    //  zho-Hant    zho-Hans    no
    
    // FUTURE support country code for dialects?
    //  target  result  match?
    //  en      en-GB   yes
    //  en-GB   en-US   no
    //  en-GB   en      no
    
    public boolean weaklyEqual(LanguageLocale target) {
        if (!_language.equals(target._language)) {
            // But wait - maybe target is a macro language, and we've got
            // a more specific result...if so then they're still (maybe)
            // equal.
            if (!SPECIFIC_TO_MACRO.containsKey(_language) || !SPECIFIC_TO_MACRO.get(_language).equals(target._language)) {
                return false;
            }
        }
        
        if (target._script.isEmpty()) {
            // Target says we don't care about script, so always matches.
            return true;
        } else {
            return _script.equals(target._script);
        }
    }
    
    public String getName() {
        if (!_script.isEmpty()) {
            return String.format("%s-%s", _language, _script);
        } else {
            return _language;
        }
    }
    
    public String getISO3LetterName() {
        return _language;
    }

    public final String toString() {
        String languageTag;
        if (!_script.isEmpty()) {
            languageTag = String.format("%s-%s", _language, _script);
        } else {
            languageTag = _language;
        }
        
        String displayLanguage = Locale.forLanguageTag(languageTag).getDisplayLanguage();
        if (TERM_CODE_TO_NAME.containsKey(_language)) {
            // It's a terminological code, so we have to provide the name ourselves.
            displayLanguage = TERM_CODE_TO_NAME.get(_language);
        }
        
        return String.format("%s (%s)", languageTag, displayLanguage);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_language == null) ? 0 : _language.hashCode());
        result = prime * result + ((_script == null) ? 0 : _script.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LanguageLocale other = (LanguageLocale) obj;
        if (_language == null) {
            if (other._language != null)
                return false;
        } else if (!_language.equals(other._language))
            return false;
        if (_script == null) {
            if (other._script != null)
                return false;
        } else if (!_script.equals(other._script))
            return false;
        return true;
    }


    
}
