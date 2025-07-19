package com.cinemamanager.util;

import com.cinemamanager.enums.CollectionType;
import com.cinemamanager.exception.DuplicateElementException;
import com.cinemamanager.iface.Identifiable;

import java.util.*;
import java.util.function.Predicate;

public final class StorageManager <ID, E extends Identifiable <ID>> {

    // Attributes:
    private Collection <E> collection;
    private Map <ID, E> map;

    // Constructor:
    public StorageManager (CollectionType collectionType) {
        switch (collectionType) {
            case ARRAY_LIST:
            case LINKED_LIST:
            case VECTOR:
            case HASH_SET:
            case LINKED_HASH_SET:
            case TREE_SET:
                collection = createCollection (collectionType);
                break;
            case HASH_MAP:
            case LINKED_HASH_MAP:
            case TREE_MAP:
                map = createMap (collectionType);
                break;
        }
    }

    // Constructor-based distinction:
    private Collection <E> createCollection (CollectionType collectionType) {
        return switch (collectionType) {
            case ARRAY_LIST -> new ArrayList<>();
            case LINKED_LIST -> new LinkedList<>();
            case VECTOR -> new Vector<>();
            case HASH_SET -> new HashSet<>();
            case LINKED_HASH_SET -> new LinkedHashSet<>();
            case TREE_SET -> new TreeSet<>();
            default -> throw new IllegalArgumentException ("Unsupported collection type: " + collectionType);
        };
    }

    private Map <ID, E> createMap (CollectionType collectionType) {
        return switch (collectionType) {
            case HASH_MAP -> new HashMap<>();
            case LINKED_HASH_MAP -> new LinkedHashMap<>();
            case TREE_MAP -> new TreeMap<>();
            default -> throw new IllegalArgumentException ("Unsupported collection type: " + collectionType);
        };
    }

    // Validating
    private boolean isUsingMap () {
        return map != null;
    }

    // CRUD methods:
    public void add (E element, boolean duplicatesAllowed) throws DuplicateElementException {
        if (!isUsingMap()) {
            addToCollection (element, duplicatesAllowed);
        }
        else {
            addToMap (element);
        }
    }

    public List <E> findBy (Predicate <E> condition) {
        Collection <E> values = isUsingMap() ? map.values() : collection;
        return values.stream()
                .filter(condition)
                .toList();
    }

    public Optional <E> findFirstBy(Predicate <E> condition) {
        Collection <E> values = isUsingMap() ? map.values() : collection;
        return values.stream()
                .filter(condition)
                .findFirst();
    }

    public Optional <E> findById (ID id) {
        return isUsingMap()
                ? Optional.ofNullable(map.get(id))
                : collection.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }

    public List<E> findAll() {
        return new ArrayList<>(isUsingMap() ? map.values() : collection);
    }

    public void update(E element) throws IllegalArgumentException {
        if (!isUsingMap()) {
            if (!collection.contains(element)) {
                throw new IllegalArgumentException("Element not found in collection for update.");
            }
            collection.removeIf(e -> e.equals(element));
            collection.add(element);
        } else {
            ID key = element.getId();
            if (!map.containsKey(key)) {
                throw new IllegalArgumentException("Key not found in map for update.");
            }
            map.put(key, element);
        }
        System.out.println("Element updated successfully!");
    }

    public void delete (ID id) {
        if (isUsingMap()) {
            deleteFromMap(id);
        } else {
            deleteFromCollection(id);
        }
    }

    private void addToCollection (E element, boolean duplicatesAllowed) throws DuplicateElementException {
        boolean exists = collection.contains(element);

        if (!duplicatesAllowed && exists) {
            throw new DuplicateElementException (false);
        }
        if (collection instanceof Set && exists) {
            throw new DuplicateElementException ("Element will not be added because the collection is a Set and does not allow duplicates.");
        }

        collection.add(element);
        System.out.println("Element added successfully!");
    }

    private void addToMap(E element) {
        ID key = element.getId();

        if (map.containsKey(key)) {
            boolean confirm = ConsoleUtil.confirm("Warning: the key already exists in the map. This will overwrite the existing value.");
            if (!confirm) return;
        }

        map.put(key, element);
        System.out.println("Element added successfully!");
    }

    private void deleteFromMap(ID id) {
        E element = map.get(id);
        if (element == null) {
            System.out.println("No element found with the given ID.");
            return;
        }

        String warning = "This operation is irreversible. The following element will be deleted:\n" + element;
        if (ConsoleUtil.confirm(warning)) {
            map.remove(id);
            System.out.println("Element successfully deleted.");
        }
    }

    private void deleteFromCollection(ID id) {
        findById(id).ifPresentOrElse(
                e -> {
                    String warning = "This operation is irreversible. The following element will be deleted:\n" + e;
                    if (ConsoleUtil.confirm(warning)) {
                        collection.remove(e);
                        System.out.println("Element successfully deleted.");
                    }
                },
                () -> System.out.println("No element found with the given ID.")
        );
    }

}
