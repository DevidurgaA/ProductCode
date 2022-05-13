package com.tlc.i18n;

import java.util.Locale;

/**
 * @author Abishek
 * @version 1.0
 */
public interface I18nService
{
    I18nResolver getResolver(Locale locale);
}
