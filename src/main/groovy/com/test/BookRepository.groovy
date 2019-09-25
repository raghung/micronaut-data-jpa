package com.test

import groovy.transform.CompileStatic
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.data.annotation.Repository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.PageableRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.hibernate.SessionFactory

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@CompileStatic
@Repository
abstract class BookRepository implements PageableRepository<Book, Long>{

    @PersistenceContext
    EntityManager entityManager

    SessionFactory sessionFactory

    BookRepository(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager
//        sessionFactory = new org.hibernate.cfg.Configuration().configure().buildSessionFactory()
//        sessionFactory.openSession()
//        sessionFactory.close()
    }

    @Transactional
    List<Book> findAllByTitle(String title) {
        entityManager.createQuery("FROM Book AS book WHERE book.title = :title", Book)
                    .setParameter("title", title)
                    .getResultList()
    }

    @Transactional
    Book batchProcess(Book book) {
        entityManager.persist(book)
        entityManager.flush()
        entityManager.clear()
        return book
    }

}
