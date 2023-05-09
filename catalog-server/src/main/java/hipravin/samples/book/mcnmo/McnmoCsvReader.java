package hipravin.samples.book.mcnmo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class McnmoCsvReader {
    private static final char MCNMO_CSV_DELIMETER = ';';
    private static String[] HEADER_EXPECTED =
            {"№", "Автор", "Название книги", "Год", "ISBN", "тираж", "стр.", "Обложка", "ст.", "вес ст.", "Цена", "Nid"};

    public List<McnmoBook> readAll(Path booksCsvFile) {
        CSVFormat csvFormat = CSVFormat.EXCEL.builder()
                .setDelimiter(MCNMO_CSV_DELIMETER)
                .build();

        try (Reader reader = Files.newBufferedReader(booksCsvFile)) {
            return mapRecordsToBooks(csvFormat.parse(reader));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<McnmoBook> mapRecordsToBooks(Iterable<CSVRecord> csvRecords) {
        final List<McnmoBook> result = new ArrayList<>();
        String currentCategory = null;

        for (CSVRecord record : csvRecords) {
            String[] values = parseAndSanitize(record);

            if (isSkipRow(values)) {
                continue;
            }

            if (isStopRow(values)) {
                break;
            }

            Optional<String> category = tryParseCategory(values);
            if (category.isPresent()) {
                currentCategory = category.get();
            } else {
                McnmoBook book = mapToBook(values, currentCategory);
                result.add(book);
            }
        }

        return result;
    }

    //[№, Автор, Название книги, Год, ISBN, тираж, стр., Обложка, ст., вес ст., Цена, Nid]
    //[1, , Математическое просвещение. Третья серия. Выпуск 30, 2023, 978-5-4439-1768-9, 600 экз., 248 стр., мягкая, 20, 7500 г., 450 руб., 176275]
    private static McnmoBook mapToBook(String[] values, String category) {
        Long number = Parser.parseNumber(values[0]);
        String authors = values[1];
        String title = values[2];
        Integer year = Parser.parseYear(values[3]);
        String isbn = values[4];
        Integer circulation = Parser.parseCirculation(values[5]);
        Integer pages = Parser.parsePages(values[6]);
        String cover = values[7];
        BigDecimal price = Parser.parsePrice(values[10]);
        String nid = values[11];

        return new McnmoBook(category, number, authors, title, year, isbn, circulation, pages, cover, price, nid);
    }

    private static boolean isHeaderRow(String[] values) {
        return Arrays.equals(values, HEADER_EXPECTED);
    }

    private static boolean isStopRow(String[] values) {
        return Arrays.stream(values).allMatch(Objects::isNull);
    }

    private static boolean isSkipRow(String[] values) {
        return values.length == 0 || isHeaderRow(values);
    }

    private static Optional<String> tryParseCategory(String[] values) {
        if (values[0] != null && Arrays.stream(values).skip(1).allMatch(Objects::isNull)) {
            return Optional.of(values[0]);
        } else if(values[0] == null && values[1] != null && Arrays.stream(values).skip(2).allMatch(Objects::isNull)) {
            return Optional.of(values[1]);
        } else {
            return Optional.empty();
        }
    }

    private static String[] parseAndSanitize(CSVRecord csvRecord) {
        return csvRecord.stream()
                .map(s -> stripBlankToNull(s))
                .toArray(String[]::new);
    }

    private static String stripBlankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }

    static class Parser {
        private static final Pattern pricePattern = Pattern.compile("^([-+]?\\d*\\.?\\d+)\\s+руб\\.?$");
        private static final Pattern circulationPattern = Pattern.compile("^(\\d+)\\s+экз\\.?$");
        private static final Pattern pagesPattern = Pattern.compile("^(\\d+)\\s+стр\\.?$");

        static BigDecimal parsePrice(String price) {
            if (price == null) {
                return null;
            }

            Matcher matcher = pricePattern.matcher(price);
            if (matcher.find()) {
                return new BigDecimal(matcher.group(1));
            } else {
                throw new IllegalArgumentException("Invalid price: " + price);
            }
        }

        static Integer parseCirculation(String circulation) {
            if (circulation == null) {
                return null;
            }

            Matcher matcher = circulationPattern.matcher(circulation);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            } else {
                throw new IllegalArgumentException("Invalid circulation: " + circulation);
            }
        }

        static Integer parsePages(String pages) {
            if (pages == null) {
                return null;
            }

            Matcher matcher = pagesPattern.matcher(pages);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            } else {
                throw new IllegalArgumentException("Invalid pages: " + pages);
            }
        }

        static Long parseNumber(String number) {
            if (number == null) {
                return null;
            }

            return Long.parseLong(number);
        }

        static Integer parseYear(String year) {
            if (year == null) {
                return null;
            }

            return Integer.parseInt(year);
        }


    }
}
