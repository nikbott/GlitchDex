package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.config.I18nConfig;
import br.ufscar.glitchdex.dto.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@ControllerAdvice(basePackages = "br.ufscar.glitchdex.controller.web")
@RequiredArgsConstructor
public class LanguageControllerAdvice {

    private final MessageSource messageSource;
    private final I18nConfig i18nConfig;

    /**
     * Provides a list of Language objects to the model. The name of each language
     * is resolved using the MessageSource for proper internationalization.
     */
    @ModelAttribute("languages")
    public List<Language> getLanguages() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return i18nConfig.getLanguages().stream()
                .map(code -> {
                    String name = messageSource.getMessage("header.lang." + code, null, code, currentLocale);
                    return new Language(code, name);
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

        String name = messageSource.getMessage("header.lang." + finalLangCode, null, finalLangCode, currentLocale);
        return new Language(finalLangCode, name);
    }
}