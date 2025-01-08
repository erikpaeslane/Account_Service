package account.utils;


import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public LocalDate convert(String source) {
        // Append "01-" to default to the first day of the month
        String formattedDateString = "01-" + source;

        // Parse the date using the formatter for "dd-MM-yyyy"
        DateTimeFormatter completeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(formattedDateString, completeFormatter);
    }
}
