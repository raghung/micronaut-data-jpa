package com.test

import groovy.transform.CompileStatic
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.data.annotation.Repository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.PageableRepository
import io.micronaut.spring.tx.annotation.Transactional

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@CompileStatic
@Repository
abstract class BookRepository implements PageableRepository<Book, Long>{

    @PersistenceContext
    EntityManager entityManager

    BookRepository(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager
    }

    @Transactional
    List<Book> findAllByTitle(String title) {
        entityManager.createQuery("FROM Book AS book WHERE book.title = :title", Book)
                    .setParameter("title", title)
                    .getResultList()
    }

    Optional<Book> findByTitle(String title) {}

    @Transactional
    Book batchProcess(Book book) {
        entityManager.persist(book)
        entityManager.flush()
        entityManager.clear()
        return book
    }

    List<Book> listOrderByTitleDesc() {}

}
