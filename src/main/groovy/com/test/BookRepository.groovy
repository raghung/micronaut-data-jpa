package com.test

import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
abstract class BookRepository implements PageableRepository<Book, Long>{

    @PersistenceContext
    EntityManager entityManager

    BookRepository(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager
    }

    List<Book> findAllByTitle(String title) {
        entityManager.createQuery("FROM Book AS book WHERE book.tittle = :title", Book)
                    .setParameter("title", title)
                    .getResultList()
    }
}
