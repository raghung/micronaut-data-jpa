package com.test

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
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

    @Get("/list")
    List<Book> list() {
        bookRepository.findAll().asList()
    }

    @Put("/")
    Book save(String title, int pages, String name, Double longitude, Double lattitude) {
        Book book = bookRepository.findByTitle(title)?.get()

        if (!book) {
            book = new Book(title: title, pages: pages)
            bookRepository.save(book)
        }

        // Save Publisher
        String bookInfo = JsonOutput.toJson(book).toString()

        Point point = (Point)wktToGeometry("POINT (${lattitude} ${longitude})")

        publisherRepository.save(new Publisher(name: name, book: bookInfo,
                                               longitude: longitude, lattitude: lattitude, location: point))

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

    @Get("/publisher/search/{searchText}")
    List searchPublisher(String searchText) {
        publisherRepository.searchPublishers(searchText)
    }

    @Post("/publisher/withinRadius")
    List withinRadius(Double latitude, Double longitude, Double radius) {
        publisherRepository.withinRadius(latitude, longitude, radius)
    }

    private Geometry wktToGeometry(String wellKnownText) throws ParseException {
        new WKTReader().read(wellKnownText)
    }

}