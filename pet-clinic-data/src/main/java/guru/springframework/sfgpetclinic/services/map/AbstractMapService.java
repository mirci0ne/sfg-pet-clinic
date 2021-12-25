package guru.springframework.sfgpetclinic.services.map;

import java.util.*;

public abstract class AbstractMapService<T, ID> {

    protected Map<T, ID> map = new HashMap<T, ID>();

    Set<T> findAll() {
        return new HashSet<T>((Collection<? extends T>) map.values());
    }

    T findById(ID id) {
        return (T) map.get(id);
    }

    T save(ID id, T object) {
        map.put((T) id, (ID) object);

        return object;
    }

    void deleteById(ID id) {
        map.remove((T) id);
    }

    void delete(T object) {
        map.entrySet().removeIf(entry -> entry.getValue().equals(object));
    }
}
