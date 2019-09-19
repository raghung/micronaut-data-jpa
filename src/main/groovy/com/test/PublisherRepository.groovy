package com.test

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.lucene.search.Sort
import org.hibernate.query.Query
import org.hibernate.search.FullTextSession
import org.hibernate.search.jpa.FullTextEntityManager
import org.hibernate.search.jpa.FullTextQuery
import org.hibernate.search.jpa.Search
import org.hibernate.search.query.dsl.QueryBuilder
import org.hibernate.search.query.dsl.Unit
import org.hibernate.search.query.dsl.sort.SortFieldContext
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.util.GeometricShapeFactory

import javax.annotation.Nullable
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
abstract class PublisherRepository implements CrudRepository<Publisher, Long> {
    @PersistenceContext
    EntityManager entityManager



    PublisherRepository(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager
    }

    @Transactional
    void initializeHibernateSearch() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager)
        fullTextEntityManager.createIndexer().startAndWait() // Indexing on load
    }

//    @Transactional
//    Publisher save(Publisher publisher) {
//        entityManager.persist(publisher)
//        return publisher
//    }

    @Transactional
    List withinRadius(double x, double y, double radius) {
        List<Publisher> lstPublisher = entityManager.createQuery("Select p from Publisher p Where within(p.location, :circle) = true", Publisher)
                                                    .setParameter("circle", createCircle(x, y, radius))
                                                    .getResultList()


        return getPublishersDetail(lstPublisher)
    }

    @Transactional
    List searchPublishers(String searchText, @Nullable String[] fields,
                          Integer offset, Integer max, String sortField, String sortDirection) {
        if (!fields || fields.size() == 0) {
            fields = ["name"]
        }

        getPublishersDetail(
                // Hibernate DSL query
                getFullTextQuery(
                        // Lucene Query
                        getSearchQueryBuilder().keyword().onFields(fields)
                                                         .matching(searchText).createQuery()
                ).setFirstResult(offset)
                 .setMaxResults(max)
                 .setSort(createSort(sortField, sortDirection))
                 .getResultList()
        )
    }

    @Transactional
    List searchWithinRadius(Double longitude, Double latitude, Integer radius,
                            Integer offset, Integer max, String sortField, String sortDirection) {

        getPublishersDetail(
                // Hibernate DSL query
                getFullTextQuery(
                        // Lucene Query
                        getSearchQueryBuilder().spatial().within(radius, Unit.KM)
                                                         .ofLatitude(latitude)
                                                         .andLongitude(longitude).createQuery()
                ).setFirstResult(offset)
                 .setMaxResults(max)
                 .setSort(createSort(sortField, sortDirection))
                 .getResultList()
        )
        // fulltextQuery.getResultSize() gives total count
    }

    private List getPublishersDetail(List<Publisher> lstPublisher) {
        List lstResult = []
        lstPublisher?.each { publisher ->
            JsonSlurper jsonSlurper = new JsonSlurper()
            Map book = (Map)jsonSlurper.parseText(publisher.book)
            lstResult += ["name": publisher.name,
                          "book": book,
                          "location": publisher.location.toString()]
        }

        return lstResult
    }

    private Geometry createCircle(double x, double y, double radius) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory()
        shapeFactory.setNumPoints(32)
        shapeFactory.setCentre(new Coordinate(x, y))
        shapeFactory.setSize(radius * 2)
        return shapeFactory.createCircle()
    }

    private Sort createSort(String sortField, String sortDirection) {
        // Creating Sort Object
        SortFieldContext context =  getSearchQueryBuilder().sort().byField(sortField)
        Sort sort
        if (sortDirection.equalsIgnoreCase("desc")) {
            sort = context.desc().createSort()
        } else {
            sort = context.asc().createSort()
        }

        return sort
    }

    private FullTextQuery getFullTextQuery(org.apache.lucene.search.Query luceneQuery) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager)
        fullTextEntityManager.createFullTextQuery(luceneQuery, Publisher)
    }

    private QueryBuilder getSearchQueryBuilder() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager)
        fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Publisher).get()
    }
}
