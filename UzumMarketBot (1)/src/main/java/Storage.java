
import java.util.*;

public class Storage {
    public static List<Product> products = List.of(
        new Product("p1", "🍎 Olma", 5000),
        new Product("p2", "🍌 Banan", 7000),
        new Product("p3", "🍊 Apelsin", 8000)
    );

    public static Map<String, List<Product>> cart = new HashMap<>();
}
