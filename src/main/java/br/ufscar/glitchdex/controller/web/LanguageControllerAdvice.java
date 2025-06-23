package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.config.I18nConfig;
import br.ufscar.glitchdex.dto.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class LanguageControllerAdvice {

    private static final Map<String, String> languageToCountryCode = new HashMap<>();

    static {
        languageToCountryCode.put("en", "us");
        languageToCountryCode.put("pt", "br");
        languageToCountryCode.put("es", "es");
        languageToCountryCode.put("fr", "fr");
        languageToCountryCode.put("it", "it");
        languageToCountryCode.put("de", "de");
        languageToCountryCode.put("zh", "cn");
        languageToCountryCode.put("ja", "jp");
        languageToCountryCode.put("ru", "ru");
    }

    private final MessageSource messageSource;
    private final I18nConfig i18nConfig;

    /**
     * Provides a list of Language objects to the model. The name of each language
     * is resolved using the MessageSource for proper internationalization, ensuring
     * each language is displayed in its native form.
     */
    @ModelAttribute("languages")
    public List<Language> getLanguages() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return i18nConfig.getLanguages().stream()
                .map(code -> {
                    // Create a locale for the specific language code to get its native name
                    Locale nameLocale = new Locale(code);
                    String name = messageSource.getMessage("header.lang." + code, null, code, nameLocale);
                    String countryCode = languageToCountryCode.getOrDefault(code, "us");
                    return new Language(code, name, countryCode);
                })
                .sorted(Comparator.comparing(lang -> !lang.getCode().equals(currentLocale.getLanguage())))
                .collect(Collectors.toList());
    }

    /**
     * Provides the current Language object to the model.
     */
    @ModelAttribute("currentLanguage")
    public Language getCurrentLanguage() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        String currentLangCode = currentLocale.getLanguage();

        // Check if the current language is supported, otherwise default to "en"
        String finalLangCode = i18nConfig.getLanguages().contains(currentLangCode) ? currentLangCode : "en";

        String name = messageSource.getMessage("header.lang." + finalLangCode, null, finalLangCode, new Locale(finalLangCode));
        String countryCode = languageToCountryCode.getOrDefault(finalLangCode, "us");
        return new Language(finalLangCode, name, countryCode);
    }
}