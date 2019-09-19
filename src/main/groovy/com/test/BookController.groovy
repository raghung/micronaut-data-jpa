package com.test

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.ParseException
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.util.GeometricShapeFactory

import javax.annotation.Nullable
import javax.inject.Inject

@CompileStatic
@Slf4j
@Controller("/books")
class BookController {

    @Inject
    BookRepository bookRepository

    @Inject
    PublisherRepository publisherRepository

    @Get("/{title}")
    List<Book> books(String title) {
        bookRepository.findAllByTitle(title)
    }

    @Post("/pageable")
    Page<Book> findAll(@Body Pageable pageable) {
       /* {
            "number": 1,
            "size": 2,
            "sort": {
                "orderBy": [
                    {
                        "property": "title",
                        "direction": "ASC",
                        "ignoreCase": false
                    }
                ]
            }
        } */
        bookRepository.findAll(pageable)
    }

    @Post("/list")
    Page<Book> list(Integer offset, Integer max, String sortField, String sortDirection) {
        Sort.Order.Direction direction = Sort.Order.Direction.ASC
        if (sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Order.Direction.DESC
        }
        Sort.Order order = new Sort.Order(sortField, direction, false)
        Sort sort = Sort.of([order])
        Pageable pageable = Pageable.from(offset, max, sort)
        bookRepository.findAll(pageable)
        //bookRepository.findAll().asList()
    }

    @Put("/")
    Book save(String title, int pages, String name, Double longitude, Double latitude) {
        Book book = bookRepository.findByTitle(title)?.get()

        if (!book) {
            book = new Book(title: title, pages: pages)
            bookRepository.save(book)
        }

        // Save Publisher
        String bookInfo = JsonOutput.toJson(book).toString()

        Point point = (Point)wktToGeometry("POINT (${latitude} ${longitude})")

        publisherRepository.save(new Publisher(name: name, book: bookInfo,
                                               longitude: longitude, latitude: latitude, location: point))

        return book
    }

    @Get("/publisher/{id}")
    Map publisherInfo(Long id) {
        Publisher publisher = publisherRepository.findById(id)?.get()
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map book = (Map)jsonSlurper.parseText(publisher.book)
        log.info("Book title -> " + book.get("title"))

        return ["name": publisher.name,
                "book": book,
                "location": publisher.location.toString()]
    }

    @Post("/publisher/search/}")
    List searchPublisher(String searchText, @Nullable String[] fields,
                         Integer offset, Integer max, String sortField, String sortDirection) {
        publisherRepository.searchPublishers(searchText, fields, offset, max, sortField, sortDirection)
    }

    @Post("/publisher/geosearch/")
    List searchPublisher(Double longitude, Double latitude, Integer radius,
                         Integer offset, Integer max, String sortField, String sortDirection) {
        publisherRepository.searchWithinRadius(longitude, latitude, radius, offset, max, sortField, sortDirection)
    }

    @Post("/publisher/withinRadius")
    List withinRadius(Double latitude, Double longitude, Double radius) {
        publisherRepository.withinRadius(latitude, longitude, radius)
    }

    private Geometry wktToGeometry(String wellKnownText) throws ParseException {
        new WKTReader().read(wellKnownText)
    }

}