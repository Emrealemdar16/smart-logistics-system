package logistics.repository;

import java.util.List;

/**
 * Verilerin kalici olarak saklanmasi ve yüklenmesi için soyutlama arayüzü (DIP).
 */
public interface DataRepository<T> {
    List<T> findAll();

    void saveAll(List<T> items);
}
