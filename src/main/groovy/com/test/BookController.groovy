package com.test

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put

import javax.inject.Inject

@Slf4j
@Controller("/books")
class BookController {

    @Inject
    BookRepository bookRepository

    @Get("/{title}")
    List<Book> books(String title) {
        bookRepository.findAllByTitle(title)
    }

    @Put("/")
    Book save(String title, int pages) {
        log.info(bookRepository.findByTitle(title))
        bookRepository.save(new Book(title: title, pages: pages))
    }
}