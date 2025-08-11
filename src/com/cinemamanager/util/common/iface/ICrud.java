package com.cinemamanager.util.common.iface;

import com.cinemamanager.iface.Identifiable;
import com.cinemamanager.util.common.exception.DuplicateElementException;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface ICrud <E extends Identifiable<ID>, ID> {
    void add (E element, boolean duplicatesAllowed) throws DuplicateElementException;
    Optional <E> findById (ID id);
    Optional <E> findFirstBy (Predicate<E> condition);
    List <E> findBy (Predicate <E> condition);
    List <E> findAll ();
    void update (E element) throws IllegalArgumentException;
    void delete (ID id);
}
