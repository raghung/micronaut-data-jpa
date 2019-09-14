package com.test

import groovy.transform.CompileStatic
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

import javax.inject.Inject

@Controller("/book")
class BookController {

    @Inject
    BookRepository bookRepository

    @Get("/{title}")
    List<Book> books(String title) {
        bookRepository.findAllByTitle(title)
    }
}