package com.test

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository

@Repository
interface InterfaceBookRepository extends PageableRepository<Book, Long>{

    Optional<Book> findByTitle(String title)
}
