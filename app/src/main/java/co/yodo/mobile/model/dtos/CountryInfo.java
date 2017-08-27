package co.yodo.mobile.model.dtos;

import android.support.annotation.NonNull;

import java.text.Collator;
import java.util.Locale;

/**
 * Created by hei on 09/08/17.
 * POJO for the country information
 */
public final class CountryInfo implements Comparable<CountryInfo> {
    private final Collator collator;
    public final Locale locale;
    public final int countryCode;

    public CountryInfo(Locale locale, int countryCode) {
        collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.PRIMARY);

        this.locale = locale;
        this.countryCode = countryCode;
    }

    public static String localeToEmoji(Locale locale) {
        String countryCode = locale.getCountry();
        // 0x41 is Letter A
        // 0x1F1E6 is Regional Indicator Symbol Letter A
        // Example :
        // firstLetter U => 20 + 0x1F1E6
        // secondLetter S => 18 + 0x1F1E6
        // See: https://en.wikipedia.org/wiki/Regional_Indicator_Symbol
        int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars
                (secondLetter));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CountryInfo that = (CountryInfo) o;

        return countryCode == that.countryCode
                && (locale != null ? locale.equals(that.locale) : that.locale == null);
    }

    @Override
    public int hashCode() {
        int result = locale != null ? locale.hashCode() : 0;
        result = 31 * result + countryCode;
        return result;
    }

    @Override
    public String toString() {
        return localeToEmoji(locale) + " " + this.locale.getDisplayCountry() + " +" + countryCode;
    }

    @Override
    public int compareTo(@NonNull CountryInfo info) {
        return collator.compare(this.locale.getDisplayCountry(), info.locale.getDisplayCountry());
    }
}
