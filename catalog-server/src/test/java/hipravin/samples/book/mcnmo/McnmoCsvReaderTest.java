package hipravin.samples.book.mcnmo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class McnmoCsvReaderTest {
    static Path booksSamplePath;
    static McnmoCsvReader reader = new McnmoCsvReader();

    @BeforeAll
    static void beforeAll() throws URISyntaxException {
        booksSamplePath = Path.of(ClassLoader.getSystemResource("test-data/books.csv").toURI());
    }

    @Test
    void testReadAllBooks() {
        List<McnmoBook> books = reader.readAll(booksSamplePath);
        assertEquals(1398, books.size());

        assertTrue(books.stream().allMatch(b -> b.title() != null));
        assertTrue(books.stream().allMatch(b -> b.category() != null));
    }

    @Test
    void testParsePriceInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            McnmoCsvReader.Parser.parsePrice("safsdg");
        });
        assertNull(McnmoCsvReader.Parser.parsePrice(null));
    }

    @Test
    void testParsePriceFloat() {
        BigDecimal expected = new BigDecimal("123.45");
        BigDecimal actual = McnmoCsvReader.Parser.parsePrice("123.45 руб.");

        assertEquals(0, expected.compareTo(actual));
    }
    @Test
    void testParsePriceInt() {
        BigDecimal expected = new BigDecimal("1234567980");
        BigDecimal actual = McnmoCsvReader.Parser.parsePrice("1234567980 руб.");

        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    void testParseCirculationInvalid() {
        assertNull(McnmoCsvReader.Parser.parseCirculation(null));
        assertThrows(IllegalArgumentException.class, () -> {
            McnmoCsvReader.Parser.parseCirculation("sdfsdf");
        });
        assertThrows(NumberFormatException.class, () -> {
            McnmoCsvReader.Parser.parseCirculation("100000000000000000000 экз.");
        });

    }

    @Test
    void testParseCirculation() {
        assertEquals(123456, McnmoCsvReader.Parser.parseCirculation("123456 экз."));
    }

    @Test
    void testParsePagesInvalid() {
        assertNull(McnmoCsvReader.Parser.parsePages(null));
        assertThrows(IllegalArgumentException.class, () -> {
            McnmoCsvReader.Parser.parsePages("sdfsdf");
        });
        assertThrows(NumberFormatException.class, () -> {
            McnmoCsvReader.Parser.parsePages("100000000000000000000 стр.");
        });

    }

    @Test
    void testParsePages() {
        assertEquals(123456, McnmoCsvReader.Parser.parsePages("123456 стр."));
    }
}