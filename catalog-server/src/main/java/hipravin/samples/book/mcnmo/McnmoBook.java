package hipravin.samples.book.mcnmo;

import java.math.BigDecimal;

public record McnmoBook(
        String category,
        Long number,
        String authors,
        String title,
        Integer year,
        String isbn,
        Integer circulation,
        Integer pages,
        String cover,
//        BigDecimal weight,
        BigDecimal priceRub,
        String nid) {
}

//Новинки;;;;;;;;;;;
//№;"Автор";"Название книги";"Год";"ISBN";"тираж";"стр.";"Обложка";"ст.";"вес ст.";"Цена";"Nid"
//